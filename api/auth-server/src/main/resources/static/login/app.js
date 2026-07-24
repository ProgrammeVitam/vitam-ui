/*
 * Minimal login SPA for the SAS POC (Phase 1).
 * Two-step flow: email → mini HRD (resolve customerId + providerType) → password → OAuth2 redirect.
 * The vanilla JS is a Phase 1 shortcut; the target Phase 2 replaces this with an Angular project
 * inside ui/ui-frontend/projects/auth-ui/.
 */

const state = {
  email: null,
  customerId: null,
  providerType: null,
  providerId: null,
  subrogation: null,
};

const $ = (id) => document.getElementById(id);

function showError(msg) {
  const err = $('error');
  err.textContent = msg;
  err.hidden = false;
}

function clearError() {
  $('error').hidden = true;
}

async function postJson(url, body) {
  const response = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify(body),
  });
  return response;
}

async function fetchContext() {
  try {
    const response = await fetch('/api/login/context', { credentials: 'include' });
    if (!response.ok) return null;
    return await response.json();
  } catch (e) {
    console.error('fetchContext error', e);
    return null;
  }
}

async function initFlow() {
  const context = await fetchContext();
  if (context && context.mode === 'subrogation') {
    state.subrogation = context;
    $('subrogation-super-email').value = context.superUserEmail;
    $('subrogation-surrogate-email').value = context.surrogateEmail;
    $('step-email').hidden = true;
    $('step-subrogation').hidden = false;
    $('subrogation-password').focus();
  }
}

async function authenticateSubrogation() {
  clearError();
  const password = $('subrogation-password').value;
  if (!password) return showError('Mot de passe requis.');

  let response;
  try {
    response = await postJson('/api/login/authenticate-subrogation', { password });
  } catch (e) {
    console.error('authenticate-subrogation network error', e);
    return showError('Erreur réseau vers /api/login/authenticate-subrogation : ' + e.message);
  }
  if (response.status === 401) {
    return showError('Mot de passe invalide ou subrogation refusée.');
  }
  if (!response.ok) {
    return showError('Erreur d\'authentification subrogée (' + response.status + ').');
  }
  const data = await response.json();
  if (data.redirectUrl) {
    window.location.assign(data.redirectUrl);
  } else {
    showError('Aucune URL de redirection retournée par le serveur.');
  }
}

async function resolve() {
  clearError();
  const email = $('email').value.trim();
  if (!email) return showError('Adresse email requise.');

  let response;
  try {
    response = await postJson('/api/login/resolve', { email });
  } catch (e) {
    console.error('resolve network error', e);
    return showError('Erreur réseau vers /api/login/resolve : ' + e.message);
  }
  if (response.status === 404) {
    return showError("Aucune organisation associée à cet email.");
  }
  if (!response.ok) {
    return showError('Erreur de résolution HRD (' + response.status + '). IAM est-il démarré ?');
  }
  const data = await response.json();
  state.email = email;

  if (data.needsCustomerSelection) {
    showCustomerChoice(data.entries || []);
    return;
  }

  applySelectedEntry({
    customerId: data.customerId,
    identityProviderId: data.identityProviderId,
    providerType: data.providerType,
    customerName: data.customerName,
    identityProviderName: data.identityProviderName,
    internal: data.providerType === 'internal',
  });
}

function showCustomerChoice(entries) {
  const container = $('customer-list');
  container.replaceChildren();
  entries.forEach((entry) => {
    const btn = document.createElement('button');
    btn.type = 'button';
    btn.className = 'customer-choice';
    const providerBadgeClass = entry.internal ? 'badge' : 'badge external';
    const providerLabel = entry.internal ? 'interne' : 'externe';
    btn.innerHTML =
      '<span class="name">' + escapeHtml(entry.customerName || entry.customerId) + '</span>' +
      '<span class="provider">via ' + escapeHtml(entry.identityProviderName || entry.identityProviderId) +
      ' <span class="' + providerBadgeClass + '">' + providerLabel + '</span></span>';
    btn.addEventListener('click', () => applySelectedEntry(entry));
    container.appendChild(btn);
  });
  $('step-email').hidden = true;
  $('step-customer').hidden = false;
}

function applySelectedEntry(entry) {
  state.customerId = entry.customerId;
  state.providerId = entry.identityProviderId;
  state.providerType = entry.providerType || (entry.internal ? 'internal' : 'external');

  if (state.providerType !== 'internal') {
    return showError(
      "Fournisseur d'identité externe (" + (entry.identityProviderName || entry.identityProviderId) +
        ') non supporté par le POC pour l\'instant.'
    );
  }

  $('step-email').hidden = true;
  $('step-customer').hidden = true;
  $('step-password').hidden = false;
  $('email-readonly').value = state.email;
  $('password').focus();
}

function escapeHtml(str) {
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

async function authenticate() {
  clearError();
  const password = $('password').value;
  if (!password) return showError('Mot de passe requis.');

  let response;
  try {
    response = await postJson('/api/login/authenticate', {
      email: state.email,
      password,
      customerId: state.customerId,
    });
  } catch (e) {
    console.error('authenticate network error', e);
    return showError('Erreur réseau vers /api/login/authenticate : ' + e.message);
  }
  if (response.status === 401) {
    return showError('Identifiants invalides.');
  }
  if (!response.ok) {
    return showError('Erreur d\'authentification (' + response.status + ').');
  }
  const data = await response.json();
  if (data.redirectUrl) {
    window.location.assign(data.redirectUrl);
  } else {
    showError('Aucune URL de redirection retournée par le serveur.');
  }
}

function back() {
  clearError();
  $('step-password').hidden = true;
  $('step-customer').hidden = true;
  $('step-email').hidden = false;
  $('password').value = '';
}

function bindHandlers() {
  $('btn-resolve').addEventListener('click', resolve);
  $('btn-authenticate').addEventListener('click', authenticate);
  $('btn-back').addEventListener('click', back);
  $('btn-back-customer').addEventListener('click', back);
  $('btn-subrogation-authenticate').addEventListener('click', authenticateSubrogation);
  $('email').addEventListener('keydown', (e) => {
    if (e.key === 'Enter') resolve();
  });
  $('password').addEventListener('keydown', (e) => {
    if (e.key === 'Enter') authenticate();
  });
  $('subrogation-password').addEventListener('keydown', (e) => {
    if (e.key === 'Enter') authenticateSubrogation();
  });
  initFlow();
}

// Script is loaded at the end of <body>; DOMContentLoaded may already have fired.
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', bindHandlers);
} else {
  bindHandlers();
}
