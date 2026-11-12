#!/usr/bin/env node
// Development-only stand-in for cas-server: a minimal OpenID Connect provider
// that asks for an email, no password, and gets the session token from IAM
// exactly as CAS does, with the cas-server client certificate. NEVER deploy
// it: anyone can log in as anyone.
//
// The same file runs on every maintained branch (9.0 and later): the public
// URL, the keystores and the IAM API flavour are detected. See README.md.
//
// No dependency: Node >= 18 standard library only.

import { createHash, generateKeyPairSync, randomBytes, sign } from 'node:crypto';
import { existsSync, readFileSync } from 'node:fs';
import { createServer, request as httpsRequest } from 'node:https';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

// ---------------------------------------------------------------- config

const repo = resolve(dirname(fileURLToPath(import.meta.url)), '../..');
const env = (name, fallback) => process.env[name] ?? fallback;

/** First existing file, so that one default fits every branch's dev-deployment layout. */
const firstExisting = (...candidates) => candidates.map((c) => resolve(repo, c)).find(existsSync) ?? resolve(repo, candidates[0]);

/**
 * Public CAS URL, as the frontends see it: the OIDC issuer of their dev configuration, minus "/oidc".
 * https://dev.vitamui.com/cas behind the nginx reverse proxy (10.x), https://dev.vitamui.com:8080/cas before (9.x).
 */
function detectPrefix() {
  try {
    const conf = JSON.parse(readFileSync(resolve(repo, 'ui/ui-frontend/projects/portal/src/assets/config-dev.json'), 'utf8'));
    const issuerUrl = conf.OIDC_CONFIG?.issuer;
    if (issuerUrl) return issuerUrl.replace(/\/oidc\/?$/, '');
  } catch {
    // fall through to the default
  }
  return 'https://dev.vitamui.com/cas';
}

const config = {
  host: env('CAS_MOCK_HOST', '127.0.0.1'),
  port: Number(env('CAS_MOCK_PORT', '8080')),
  prefix: env('CAS_MOCK_PREFIX', detectPrefix()).replace(/\/$/, ''),
  iamUrl: env('CAS_MOCK_IAM_URL', 'https://localhost:8083').replace(/\/$/, ''),
  // 9.1 and later: one keystore per role; 9.0: a single PKCS12 for both (its keystores are JKS, unreadable by Node).
  serverKeystore: env(
    'CAS_MOCK_SERVER_KEYSTORE',
    firstExisting(
      'dev-deployment/environments/keystores/vitamui-services/servers/keystore_cas-server.p12',
      'dev-deployment/environments/certs/server/hosts/localhost/cas-server.p12',
    ),
  ),
  clientKeystore: env(
    'CAS_MOCK_CLIENT_KEYSTORE',
    firstExisting(
      'dev-deployment/environments/keystores/vitamui-services/clients/keystore_cas-server.p12',
      'dev-deployment/environments/certs/server/hosts/localhost/cas-server.p12',
    ),
  ),
  keystorePassword: env('CAS_MOCK_KEYSTORE_PASSWORD', 'changeme'),
  // Same values as cas-server's dev configuration (cas/cas-server/src/main/config/).
  casSecretToken: env('CAS_MOCK_SECRET_TOKEN', 'tokcas_ie6UZsEcHIWrfv2x'),
  casTenant: env('CAS_MOCK_TENANT', '-1'),
  casIdentity: env('CAS_MOCK_IDENTITY', 'cas'),
  // Optional default email, prefilled in the login form.
  defaultEmail: env('CAS_MOCK_DEFAULT_EMAIL', ''),
  tokenTtlSeconds: Number(env('CAS_MOCK_TOKEN_TTL', '28800')),
};

const issuer = `${config.prefix}/oidc`;
const basePath = new URL(config.prefix).pathname; // "/cas"
const oidcPath = `${basePath}/oidc`;
const publicOrigin = new URL(config.prefix).origin;
const publicHost = new URL(config.prefix).hostname;
const sessionCookie = 'CASMOCKSESSION';

// ---------------------------------------------------------------- state (in memory)

const signingKey = generateKeyPairSync('rsa', { modulusLength: 2048 });
const kid = randomBytes(8).toString('hex');
const jwk = { ...signingKey.publicKey.export({ format: 'jwk' }), kid, use: 'sig', alg: 'RS256' };

/** sessionId -> { email, customerId, authToken, superUserEmail?, superUserCustomerId?, attributes } */
const sessions = new Map();
/** code -> { sessionId, clientId, redirectUri, nonce, state, codeChallenge, codeChallengeMethod, expiresAt } */
const codes = new Map();
/** pending login id -> authorize parameters, kept while the login form is shown */
const pendingLogins = new Map();

const newId = (prefix) => `${prefix}-${randomBytes(24).toString('base64url')}`;

// ---------------------------------------------------------------- IAM client

const iamAgentOptions = {
  pfx: readFileSync(config.clientKeystore),
  passphrase: config.keystorePassword,
  rejectUnauthorized: false, // dev certificates, hostname "localhost"
};

function iam(method, path, { query, body, username = 'cas-mock' } = {}) {
  const url = new URL(`${config.iamUrl}/iam/v1/cas${path}`);
  Object.entries(query ?? {}).forEach(([k, v]) => v != null && url.searchParams.set(k, v));
  const payload = body === undefined ? undefined : JSON.stringify(body);

  return new Promise((resolvePromise, reject) => {
    const req = httpsRequest(
      url,
      {
        ...iamAgentOptions,
        method,
        headers: {
          Accept: 'application/json',
          // As cas-server (AppConfig overrides IamApiClient's INTERNAL): IAM then resolves the
          // ROLE_CAS_* roles from the client certificate's security context.
          'X-Origin': 'EXTERNAL',
          'X-Tenant-Id': config.casTenant,
          'X-User-Token': config.casSecretToken,
          'X-Application-Id': `cas+${username}`,
          'X-Identity': config.casIdentity,
          'X-Request-Id': randomBytes(12).toString('hex'),
          ...(payload ? { 'Content-Type': 'application/json', 'Content-Length': Buffer.byteLength(payload) } : {}),
        },
      },
      (res) => {
        let data = '';
        res.setEncoding('utf8');
        res.on('data', (chunk) => (data += chunk));
        res.on('end', () => {
          if (res.statusCode >= 200 && res.statusCode < 300) {
            resolvePromise(data ? JSON.parse(data) : null);
          } else {
            const error = new Error(`IAM ${method} ${url.pathname} -> HTTP ${res.statusCode}: ${data.slice(0, 500)}`);
            error.status = res.statusCode;
            reject(error);
          }
        });
      },
    );
    req.on('error', (e) => reject(new Error(`IAM unreachable at ${config.iamUrl}: ${e.message}`)));
    if (payload) req.write(payload);
    req.end();
  });
}

// Two flavours of the IAM authentication API:
// - current (10.0 and later, iam-auth-contract): /hrd, /users/principal-attributes, /subrogations/validate;
// - legacy (9.x): /users?email=, /users/provisioning?embedded=authtoken, /subrogations?superUserEmail=.
// /hrd never answers 404 (an unknown email gets an empty list), so a 404 there means the legacy flavour.
let legacyApi = env('CAS_MOCK_LEGACY_API', '') === 'true' ? true : null;

/** Organisations an email can log in to: [{ customerId, customerName }]. */
async function findCustomers(email) {
  if (!legacyApi) {
    try {
      const entries = (await iam('GET', '/hrd', { query: { email }, username: email })) ?? [];
      legacyApi = false;
      // HRD answers even for unknown emails (no account enumeration): keep only entries tied to a real user.
      const known = entries.filter((e) => e.userStatus);
      return [...new Map((known.length ? known : entries).map((e) => [e.customerId, e])).values()].map((e) => ({
        customerId: e.customerId,
        customerName: e.customerName || e.customerCode || e.customerId,
      }));
    } catch (e) {
      if (e.status !== 404 || legacyApi === false) throw e;
      legacyApi = true;
      console.log('[cas-mock] no /iam/v1/cas/hrd: using the legacy (9.x) IAM API');
    }
  }
  const users = (await iam('GET', '/users', { query: { email }, username: email })) ?? [];
  const customerIds = [...new Set(users.map((u) => u.customerId))];
  if (customerIds.length === 0) return [];
  const customers = (await iam('GET', '/customers', { query: { customerIds: customerIds.join(',') }, username: email })) ?? [];
  const names = new Map(customers.map((c) => [c.id, c.name || c.code]));
  return customerIds.map((customerId) => ({ customerId, customerName: names.get(customerId) || customerId }));
}

/** Refuses (HTTP 404, as IAM) a subrogation the super user is not granted. */
async function checkSubrogation(sub) {
  if (!legacyApi) {
    await iam('POST', '/subrogations/validate', { body: sub, username: sub.superUserEmail });
    return;
  }
  const granted = (await iam('GET', '/subrogations', {
    query: { superUserEmail: sub.superUserEmail, superUserCustomerId: sub.superUserCustomerId },
    username: sub.superUserEmail,
  })) ?? [];
  const match = granted.some(
    (s) => s.surrogate?.toLowerCase() === sub.surrogateEmail.toLowerCase() && s.surrogateCustomerId === sub.surrogateCustomerId,
  );
  if (!match) throw Object.assign(new Error('Subrogation not granted'), { status: 404 });
}

/** User attributes and a fresh IAM authToken, as cas-server builds the principal. */
async function openSession(request) {
  if (!legacyApi) {
    return iam('POST', '/users/principal-attributes', { body: request, username: request.superUserEmail || request.loginEmail });
  }
  return iam('GET', '/users/provisioning', {
    query: {
      loginEmail: request.loginEmail,
      loginCustomerId: request.loginCustomerId,
      embedded: request.superUserEmail ? 'authtoken,surrogation' : 'authtoken',
    },
    username: request.superUserEmail || request.loginEmail,
  });
}

const iamLogout = (session) =>
  iam('GET', '/logout', {
    query: {
      authToken: session.authToken,
      superUser: session.superUserEmail,
      superUserCustomerId: session.superUserCustomerId,
    },
    username: session.superUserEmail || session.email,
  });

// ---------------------------------------------------------------- JWT

const b64url = (value) => Buffer.from(typeof value === 'string' ? value : JSON.stringify(value)).toString('base64url');

function signJwt(claims) {
  const unsigned = `${b64url({ alg: 'RS256', typ: 'JWT', kid })}.${b64url(claims)}`;
  return `${unsigned}.${sign('RSA-SHA256', Buffer.from(unsigned), signingKey.privateKey).toString('base64url')}`;
}

function decodeJwtPayload(token) {
  try {
    return JSON.parse(Buffer.from(token.split('.')[1], 'base64url').toString('utf8'));
  } catch {
    return null;
  }
}

function userClaims(session) {
  const a = session.attributes;
  return {
    sub: session.email,
    email: session.email,
    given_name: a.firstname,
    family_name: a.lastname,
    name: [a.firstname, a.lastname].filter(Boolean).join(' ') || session.email,
    customerId: session.customerId,
    identifier: a.identifier,
    ...(session.superUserEmail ? { superUserEmail: session.superUserEmail, superUserCustomerId: session.superUserCustomerId } : {}),
  };
}

// ---------------------------------------------------------------- HTTP helpers

function readBody(req) {
  return new Promise((resolvePromise, reject) => {
    let data = '';
    req.setEncoding('utf8');
    req.on('data', (chunk) => {
      data += chunk;
      if (data.length > 1e6) reject(new Error('Body too large'));
    });
    req.on('end', () => resolvePromise(new URLSearchParams(data)));
    req.on('error', reject);
  });
}

function cookies(req) {
  return Object.fromEntries(
    (req.headers.cookie ?? '')
      .split(';')
      .map((c) => c.trim().split('='))
      .filter(([k]) => k)
      .map(([k, ...v]) => [k, decodeURIComponent(v.join('='))]),
  );
}

function send(res, status, body, headers = {}) {
  const isJson = typeof body === 'object' && body !== null;
  res.writeHead(status, {
    'Cache-Control': 'no-store',
    'Content-Type': isJson ? 'application/json' : 'text/html; charset=utf-8',
    ...headers,
  });
  res.end(isJson ? JSON.stringify(body) : body);
}

const redirect = (res, location, headers = {}) => send(res, 302, '', { Location: location, ...headers });

const setSessionCookie = (id) => `${sessionCookie}=${id}; Path=${basePath}; HttpOnly; Secure; SameSite=Lax`;
const clearSessionCookie = `${sessionCookie}=; Path=${basePath}; HttpOnly; Secure; SameSite=Lax; Max-Age=0`;

const escapeHtml = (s) =>
  String(s ?? '').replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[c]);

/**
 * Origins of the VitamUI frontends, the only ones the mock redirects to or answers CORS requests from: the public
 * origin (nginx, 10.x), plus each Angular dev server, whose port is read from angular.json (one per app before 10.x),
 * plus CAS_MOCK_ALLOWED_ORIGINS (comma-separated). Keeps the mock from being an open redirector.
 */
function frontendOrigins() {
  const origins = new Set([publicOrigin, `https://${publicHost}`]);
  try {
    const angular = JSON.parse(readFileSync(resolve(repo, 'ui/ui-frontend/angular.json'), 'utf8'));
    for (const project of Object.values(angular.projects ?? {})) {
      const port = project.architect?.serve?.options?.port;
      if (port) ['localhost', publicHost].forEach((host) => origins.add(`https://${host}:${port}`));
    }
  } catch {
    // no angular.json: public origin only
  }
  env('CAS_MOCK_ALLOWED_ORIGINS', '')
    .split(',')
    .map((o) => o.trim())
    .filter(Boolean)
    .forEach((o) => origins.add(new URL(o).origin));
  return [...origins];
}

const allowedOrigins = frontendOrigins();

/** The allowed origin a URL belongs to: one of allowedOrigins (never the input itself), or undefined. */
function allowedOriginOf(uri) {
  try {
    const { origin } = new URL(uri);
    return allowedOrigins.find((allowed) => allowed === origin);
  } catch {
    return undefined;
  }
}

/**
 * The URL to redirect to, rebuilt on its allowed origin, or undefined when the URL is not a frontend's. Only the
 * path, query and fragment come from the input: they cannot change the host the browser is sent to.
 */
function safeRedirectUrl(uri) {
  const origin = allowedOriginOf(uri);
  if (!origin) return undefined;
  const { pathname, search, hash } = new URL(uri);
  // The "/" after the host is part of the fixed prefix: the input only ever follows it.
  return `${origin}/${pathname.replace(/^\/+/, '')}${search}${hash}`;
}

// ---------------------------------------------------------------- pages

function page(title, content) {
  return `<!doctype html>
<html lang="fr"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>${escapeHtml(title)}</title>
<style>
  :root { color-scheme: light dark; --accent: #9c31b5; }
  body { font-family: system-ui, sans-serif; display: grid; place-items: center; min-height: 100vh; margin: 0; background: Canvas; }
  main { width: min(26rem, calc(100% - 2rem)); padding: 2rem; border: 1px solid color-mix(in srgb, CanvasText 15%, transparent); border-radius: .75rem; }
  h1 { font-size: 1.25rem; margin: 0 0 .25rem; }
  .warn { font-size: .85rem; padding: .5rem .75rem; border-radius: .5rem; background: color-mix(in srgb, orange 20%, transparent); margin: 1rem 0; }
  .error { color: #c22a40; font-size: .9rem; }
  label { display: block; font-size: .85rem; margin: 1rem 0 .25rem; }
  input, select, button { box-sizing: border-box; width: 100%; font: inherit; padding: .6rem .75rem; border-radius: .5rem; border: 1px solid color-mix(in srgb, CanvasText 30%, transparent); }
  button { margin-top: 1.25rem; background: var(--accent); color: white; border: 0; cursor: pointer; }
  .muted { font-size: .85rem; opacity: .7; }
</style></head>
<body><main>${content}</main></body></html>`;
}

function loginPage(loginId, { email = '', error = '', customers = null, subrogation = null } = {}) {
  const customerField = customers
    ? `<label for="customerId">Organisation</label>
       <select id="customerId" name="customerId">${customers
         .map((c) => `<option value="${escapeHtml(c.customerId)}">${escapeHtml(c.customerName || c.customerCode || c.customerId)}</option>`)
         .join('')}</select>`
    : '';
  const subrogationInfo = subrogation
    ? `<p class="muted">Subrogation de <strong>${escapeHtml(subrogation.surrogateEmail)}</strong></p>`
    : '';
  return page(
    'Connexion (mock CAS)',
    `<h1>Connexion VitamUI</h1>
     <p class="muted">Mock CAS de développement : aucun mot de passe n'est vérifié.</p>
     ${subrogationInfo}
     ${error ? `<p class="error">${escapeHtml(error)}</p>` : ''}
     <form method="post" action="${oidcPath}/login">
       <input type="hidden" name="loginId" value="${escapeHtml(loginId)}">
       <label for="email">Email</label>
       <input id="email" name="email" type="email" required autofocus value="${escapeHtml(email)}" ${customers ? 'readonly' : ''}>
       ${customerField}
       <button type="submit">Se connecter</button>
     </form>`,
  );
}

// ---------------------------------------------------------------- OIDC flow

function discovery() {
  return {
    issuer,
    authorization_endpoint: `${issuer}/oidcAuthorize`,
    token_endpoint: `${issuer}/accessToken`,
    userinfo_endpoint: `${issuer}/profile`,
    jwks_uri: `${issuer}/jwks`,
    end_session_endpoint: `${issuer}/oidcLogout`,
    revocation_endpoint: `${issuer}/revoke`,
    response_types_supported: ['code'],
    grant_types_supported: ['authorization_code'],
    subject_types_supported: ['public'],
    id_token_signing_alg_values_supported: ['RS256'],
    scopes_supported: ['openid', 'profile', 'email'],
    token_endpoint_auth_methods_supported: ['none', 'client_secret_post', 'client_secret_basic'],
    code_challenge_methods_supported: ['S256', 'plain'],
    claims_supported: ['sub', 'email', 'name', 'given_name', 'family_name', 'customerId', 'identifier'],
  };
}

function issueCode(res, pending, sessionId, extraHeaders = {}) {
  const code = newId('OC');
  codes.set(code, { ...pending, sessionId, expiresAt: Date.now() + 60_000 });
  const target = new URL(safeRedirectUrl(pending.redirectUri));
  target.searchParams.set('code', code);
  if (pending.state) target.searchParams.set('state', pending.state);
  redirect(res, target.toString(), extraHeaders);
}

async function authorize(req, res, url) {
  const p = url.searchParams;
  const redirectUri = p.get('redirect_uri');
  if (!p.get('client_id') || !redirectUri || !safeRedirectUrl(redirectUri)) {
    return send(res, 400, page('Erreur', `<h1>Requête invalide</h1><p>client_id ou redirect_uri manquant ou non autorisé.</p>`));
  }
  if (p.get('response_type') !== 'code') {
    return send(res, 400, page('Erreur', `<h1>Requête invalide</h1><p>Seul response_type=code est supporté.</p>`));
  }

  const pending = {
    clientId: p.get('client_id'),
    redirectUri,
    state: p.get('state'),
    nonce: p.get('nonce'),
    codeChallenge: p.get('code_challenge'),
    codeChallengeMethod: p.get('code_challenge_method') || 'plain',
    subrogation:
      p.get('superUserEmail') && p.get('surrogateEmail')
        ? {
            superUserEmail: p.get('superUserEmail'),
            superUserCustomerId: p.get('superUserCustomerId'),
            surrogateEmail: p.get('surrogateEmail'),
            surrogateCustomerId: p.get('surrogateCustomerId'),
          }
        : null,
  };
  const requestedEmail = p.get('username') || pending.subrogation?.superUserEmail || '';

  // SSO: an existing session answers directly, unless another identity is asked for.
  const sessionId = cookies(req)[sessionCookie];
  const session = sessions.get(sessionId);
  if (session && !pending.subrogation && (!requestedEmail || requestedEmail === session.email) && p.get('prompt') !== 'login') {
    return issueCode(res, pending, sessionId);
  }

  const loginId = newId('LG');
  pendingLogins.set(loginId, { ...pending, expiresAt: Date.now() + 15 * 60_000 });
  send(res, 200, loginPage(loginId, { email: requestedEmail || config.defaultEmail, subrogation: pending.subrogation }));
}

async function login(req, res) {
  const form = await readBody(req);
  const loginId = form.get('loginId');
  const pending = pendingLogins.get(loginId);
  if (!pending || pending.expiresAt < Date.now()) {
    pendingLogins.delete(loginId);
    return send(res, 400, page('Erreur', `<h1>Session de connexion expirée</h1><p>Relancez la connexion depuis l'application.</p>`));
  }

  const email = (form.get('email') || '').trim().toLowerCase();
  const chosenCustomerId = form.get('customerId');

  try {
    // Also run when an organisation was chosen: checks the choice, and detects the IAM API flavour after a restart.
    const customers = await findCustomers(email);
    if (customers.length === 0) {
      return send(res, 200, loginPage(loginId, { email, error: `Aucune organisation trouvée pour ${email}.`, subrogation: pending.subrogation }));
    }
    let customerId = customers.find((c) => c.customerId === chosenCustomerId)?.customerId;
    if (!customerId && customers.length > 1) {
      return send(res, 200, loginPage(loginId, { email, customers, subrogation: pending.subrogation }));
    }
    customerId ??= customers[0].customerId;

    let request = { loginEmail: email, loginCustomerId: customerId };
    const sub = pending.subrogation;
    if (sub) {
      if (email !== sub.superUserEmail.toLowerCase() || customerId !== sub.superUserCustomerId) {
        return send(res, 200, loginPage(loginId, { email, error: `La subrogation doit être faite par ${sub.superUserEmail}.`, subrogation: sub }));
      }
      await checkSubrogation(sub);
      request = {
        loginEmail: sub.surrogateEmail,
        loginCustomerId: sub.surrogateCustomerId,
        superUserEmail: sub.superUserEmail,
        superUserCustomerId: sub.superUserCustomerId,
      };
    }

    const attributes = await openSession(request);
    if (!attributes?.authToken) throw new Error('IAM returned no authToken');
    if (attributes.status && attributes.status !== 'ENABLED') {
      return send(res, 200, loginPage(loginId, { email, error: `Compte ${attributes.status}.`, subrogation: sub }));
    }

    const sessionId = newId('TGT');
    sessions.set(sessionId, {
      email: request.loginEmail,
      customerId: request.loginCustomerId,
      authToken: attributes.authToken,
      superUserEmail: request.superUserEmail,
      superUserCustomerId: request.superUserCustomerId,
      attributes,
    });
    pendingLogins.delete(loginId);
    const subrogatedBy = sub ? ` (subrogated by ${sub.superUserEmail})` : '';
    console.log(`[cas-mock] login ${request.loginEmail}${subrogatedBy} -> ${attributes.authToken}`);
    issueCode(res, pending, sessionId, { 'Set-Cookie': setSessionCookie(sessionId) });
  } catch (e) {
    console.error('[cas-mock]', e.message);
    const message = e.status === 404 ? 'Utilisateur ou subrogation introuvable.' : e.message;
    send(res, 200, loginPage(loginId, { email, error: message, subrogation: pending.subrogation }));
  }
}

function verifyPkce(entry, verifier) {
  if (!entry.codeChallenge) return true;
  if (!verifier) return false;
  const computed = entry.codeChallengeMethod === 'S256' ? createHash('sha256').update(verifier).digest('base64url') : verifier;
  return computed === entry.codeChallenge;
}

async function token(req, res) {
  const form = await readBody(req);
  const tokenError = (error, description) => send(res, 400, { error, error_description: description });

  if (form.get('grant_type') !== 'authorization_code') return tokenError('unsupported_grant_type', 'Only authorization_code');

  const entry = codes.get(form.get('code'));
  codes.delete(form.get('code'));
  if (!entry || entry.expiresAt < Date.now()) return tokenError('invalid_grant', 'Unknown or expired code');
  if (form.get('redirect_uri') !== entry.redirectUri) return tokenError('invalid_grant', 'redirect_uri mismatch');
  if (!verifyPkce(entry, form.get('code_verifier'))) return tokenError('invalid_grant', 'PKCE verification failed');

  const session = sessions.get(entry.sessionId);
  if (!session) return tokenError('invalid_grant', 'Session ended');

  const now = Math.floor(Date.now() / 1000);
  const idToken = signJwt({
    iss: issuer,
    aud: entry.clientId,
    iat: now,
    exp: now + config.tokenTtlSeconds,
    auth_time: now,
    ...(entry.nonce ? { nonce: entry.nonce } : {}),
    // Read back by OidcAuthenticatorService to restore the requested page, as CAS does.
    ...(entry.state ? { state: entry.state } : {}),
    sid: entry.sessionId,
    ...userClaims(session),
  });

  send(res, 200, {
    access_token: session.authToken,
    token_type: 'Bearer',
    expires_in: config.tokenTtlSeconds,
    scope: 'openid',
    id_token: idToken,
  });
}

function profile(req, res) {
  const auth = req.headers.authorization ?? '';
  const accessToken = auth.startsWith('Bearer ') ? auth.slice(7) : null;
  const session = [...sessions.values()].find((s) => s.authToken === accessToken);
  if (!session) return send(res, 401, { error: 'invalid_token' });
  send(res, 200, userClaims(session));
}

async function endSession(sessionId) {
  const session = sessions.get(sessionId);
  if (!session) return;
  sessions.delete(sessionId);
  try {
    await iamLogout(session);
    console.log(`[cas-mock] logout ${session.email} (${session.authToken} revoked in IAM)`);
  } catch (e) {
    console.error('[cas-mock] IAM logout failed:', e.message);
  }
}

async function logout(req, res, url) {
  const p = url.searchParams;
  const hinted = p.get('id_token_hint') && decodeJwtPayload(p.get('id_token_hint'))?.sid;
  const fromCookie = cookies(req)[sessionCookie];
  await Promise.all([...new Set([hinted, fromCookie].filter(Boolean))].map(endSession));

  const target = safeRedirectUrl(p.get('post_logout_redirect_uri') || p.get('service') || '');
  if (target) return redirect(res, target, { 'Set-Cookie': clearSessionCookie });
  send(res, 200, page('Déconnexion', `<h1>Vous êtes déconnecté</h1><p class="muted">Mock CAS de développement.</p>`), {
    'Set-Cookie': clearSessionCookie,
  });
}

// ---------------------------------------------------------------- router

async function handle(req, res) {
  const url = new URL(req.url, config.prefix);
  const path = url.pathname.replace(/\/$/, '') || '/';

  // From 10.0 on the browser reaches the mock through nginx, on the same origin. Before, each app calls it from its
  // own origin (dev server port): CORS for the frontends only.
  const corsOrigin = allowedOrigins.find((allowed) => allowed === req.headers.origin);
  if (corsOrigin) {
    res.setHeader('Access-Control-Allow-Origin', corsOrigin);
    res.setHeader('Vary', 'Origin');
    res.setHeader('Access-Control-Allow-Credentials', 'true');
    res.setHeader('Access-Control-Allow-Headers', 'Authorization, Content-Type');
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  }
  if (req.method === 'OPTIONS') return send(res, 204, '');

  const route = `${req.method} ${path}`;
  switch (route) {
    case `GET ${oidcPath}/.well-known/openid-configuration`:
    case `GET ${oidcPath}/.well-known`:
      return send(res, 200, discovery());
    case `GET ${oidcPath}/jwks`:
      return send(res, 200, { keys: [jwk] });
    case `GET ${oidcPath}/oidcAuthorize`:
    case `GET ${oidcPath}/authorize`:
      return authorize(req, res, url);
    case `POST ${oidcPath}/login`:
      return login(req, res);
    case `POST ${oidcPath}/accessToken`:
    case `POST ${oidcPath}/token`:
      return token(req, res);
    case `GET ${oidcPath}/profile`:
    case `POST ${oidcPath}/profile`:
      return profile(req, res);
    case `POST ${oidcPath}/revoke`:
      // RFC 7009: always 200. The IAM token is revoked at end of session, as with CAS.
      return send(res, 200, {});
    case `GET ${oidcPath}/oidcLogout`:
    case `GET ${basePath}/logout`:
      return logout(req, res, url);
    case `GET ${basePath}/login`:
      return send(res, 200, page('Mock CAS', `<h1>Mock CAS</h1><p class="muted">Ouvrez une application VitamUI pour vous connecter : <a href="${publicOrigin}/">${publicOrigin}/</a></p>`));
    case `GET ${basePath}/actuator/health`:
      return send(res, 200, { status: 'UP' });
    default:
      return send(res, 404, { error: 'not_found' });
  }
}

// Expired codes and abandoned login forms.
setInterval(() => {
  const now = Date.now();
  for (const [k, v] of codes) if (v.expiresAt < now) codes.delete(k);
  for (const [k, v] of pendingLogins) if (v.expiresAt < now) pendingLogins.delete(k);
}, 60_000).unref();

createServer({ pfx: readFileSync(config.serverKeystore), passphrase: config.keystorePassword }, (req, res) => {
  handle(req, res).catch((e) => {
    console.error('[cas-mock]', e);
    if (!res.headersSent) send(res, 500, { error: 'server_error', error_description: e.message });
  });
}).listen(config.port, config.host, () => {
  console.log(`[cas-mock] listening on https://${config.host}:${config.port}${basePath} — issuer ${issuer}`);
  console.log(`[cas-mock] IAM: ${config.iamUrl}  (DEV ONLY: no password is checked)`);
});
