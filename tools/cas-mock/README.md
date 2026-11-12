# CAS mock — development only

A minimal OpenID Connect provider that takes cas-server's place in a local
setup: it asks for an **email only, no password**, and logs you in as that
VitamUI user. It saves starting cas-server (and its Mongo service registry,
SMTP, etc.) when you only need to be logged in.

> **Never deploy it.** Anyone reaching it can log in as any user. It only
> works with the dev keystores and the dev `cas_secret_token`.

## Quick start

Prerequisites: MongoDB, **security** and **iam** running (as for cas-server).
cas-server must be stopped: the mock listens on its port (8080). From 10.0 on,
the nginx reverse proxy (`tools/docker/nginx`) as well.

```bash
tools/cas-mock/start.sh
```

Then open the portal (<https://dev.vitamui.com/>, or
<https://dev.vitamui.com:4200/> on 9.x) and log in with any existing user email,
e.g. `admin@change-it.fr`. When the email belongs to several organisations, the
mock asks which one.

From IntelliJ: the **CAS mock** run configuration (`intellij-conf/.run/Shell/`,
see `intellij-conf/README.md`) runs the same script.

No dependency to install: Node ≥ 18 standard library only. `start.sh` loads
nvm when `node` is not on the `PATH` (IDE run configurations).

## Supported versions

The same `server.mjs` runs unchanged on every branch from 9.0 on, so that it
can be cherry-picked as is:

|                   | 10.0 and later                       | 9.1                     | 9.0                                              |
| ----------------- | ------------------------------------ | ----------------------- | ------------------------------------------------ |
| Public CAS URL    | `https://dev.vitamui.com/cas` (nginx) | `https://dev.vitamui.com:8080/cas` | same as 9.1                            |
| Keystores         | `keystores/vitamui-services/{servers,clients}/keystore_cas-server.p12` | same | `certs/server/hosts/localhost/cas-server.p12` |
| IAM API           | current                              | legacy                  | legacy                                           |

All three are detected: the public URL is the OIDC `issuer` of the portal's
`config-dev.json` minus `/oidc`, the keystores are the first existing file,
and the IAM API flavour is chosen on the first login (see below).

## How it works

The browser side is unchanged: the apps run in gateway mode
(`GATEWAY_ENABLED: true`) and speak OIDC (authorization code + PKCE) to the
`issuer` of their `config-dev.json`. From 10.0 on, nginx proxies `/cas/` to
`127.0.0.1:8080` whether cas-server or the mock is listening there.

The backends only accept a token that IAM itself created (`tokens`
collection), so the mock gets it the same way cas-server does:

1. the user's organisation(s): `GET /iam/v1/cas/hrd?email=…` (legacy:
   `GET /iam/v1/cas/users?email=…`, then `GET /iam/v1/cas/customers` for their
   names);
2. user attributes and a fresh IAM `authToken`:
   `POST /iam/v1/cas/users/principal-attributes` (legacy:
   `GET /iam/v1/cas/users/provisioning?…&embedded=authtoken`);
3. that `authToken` is returned as the OIDC `access_token`, along with a signed
   `id_token`.

`/hrd` never answers 404 on a current IAM (an unknown email gets an empty
list), so a 404 there switches the mock to the legacy API.

IAM calls are authenticated like cas-server's: the `cas-server` client
certificate and the CAS secret token in `X-User-Token`, so IAM grants the
`ROLE_CAS_*` roles without any configuration change.

| Endpoint (`/cas/oidc/…`)            | Behaviour                                                     |
| ----------------------------------- | ------------------------------------------------------------- |
| `.well-known/openid-configuration`  | Discovery                                                     |
| `jwks`                              | RSA key, generated at each start                              |
| `oidcAuthorize`                     | Login form, or direct answer when a mock session exists (SSO) |
| `accessToken`                       | Code → `access_token` (IAM token) + `id_token`; PKCE checked  |
| `profile`                           | User claims for a Bearer token                                |
| `revoke`                            | No-op (200), as cas-server: the IAM token outlives it         |
| `oidcLogout`, `/cas/logout`         | Deletes the IAM token (`/iam/v1/cas/logout`), ends the session |

Subrogation works: the `superUserEmail`, `surrogateEmail`… parameters sent by
the apps are validated with `POST /iam/v1/cas/subrogations/validate` (legacy:
the surrogate must be among `GET /iam/v1/cas/subrogations?superUserEmail=…`),
then the IAM token is built for the surrogate.

Sessions are kept in memory: restarting the mock logs everybody out (the apps
then redirect to the login form).

## What it does not do

Everything cas-server checks on login is skipped: password, password policy
and expiry, OTP, failed-attempt blocking, delegation to external identity
providers (OIDC/SAML/X509), login events in the logbook. Use the real
cas-server to work on any of these.

## Configuration

Environment variables, all optional:

| Variable                     | Default                                   |
| ---------------------------- | ----------------------------------------- |
| `CAS_MOCK_PORT`              | `8080`                                    |
| `CAS_MOCK_HOST`              | `127.0.0.1`                               |
| `CAS_MOCK_PREFIX`            | detected — public URL, see above          |
| `CAS_MOCK_IAM_URL`           | `https://localhost:8083`                  |
| `CAS_MOCK_DEFAULT_EMAIL`     | empty — email prefilled in the form       |
| `CAS_MOCK_TOKEN_TTL`         | `28800` (seconds, `id_token` lifetime)    |
| `CAS_MOCK_SECRET_TOKEN`      | dev `cas_secret_token`                    |
| `CAS_MOCK_TENANT`, `CAS_MOCK_IDENTITY` | `-1`, `cas`                     |
| `CAS_MOCK_SERVER_KEYSTORE`, `CAS_MOCK_CLIENT_KEYSTORE`, `CAS_MOCK_KEYSTORE_PASSWORD` | detected dev `cas-server` keystores, `changeme` |
| `CAS_MOCK_ALLOWED_ORIGINS`   | empty — extra frontend origins (comma-separated) allowed as redirect targets and CORS origins, besides the public origin and the dev servers of `angular.json` |
| `CAS_MOCK_LEGACY_API`        | detected — `true` forces the legacy (9.x) IAM API |
