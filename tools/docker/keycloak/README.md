# Keycloak test IdP — OIDC and SAML

A disposable Keycloak instance exposing the same identity twice, once through
OpenID Connect and once through SAML 2, plus the provisioning needed to attach
both to a VitamUI organization.

It complements `tools/docker/external-idp-cas`, which covers OIDC only and runs
on port 8041; the two can run side by side.

## Quick start

```sh
cd tools/docker/keycloak
./setup.sh
```

The script starts the container, imports the `vitamui` realm, aligns both
clients on the CAS URL from `.env`, generates the SAML service provider
keystore, downloads the IdP metadata, and writes the two providers and their
test users into the `iam` database.

Then log in on <https://dev.vitamui.com:8080/cas/login> with:

| Protocol | Login                       | Password       |
| -------- | --------------------------- | -------------- |
| OIDC     | `demo.oidc@keycloak-oidc.fr` | `ChangeIt.2024` |
| SAML     | `demo.saml@keycloak-saml.fr` | `ChangeIt.2024` |

CAS reloads the provider list every minute (`ProvidersService.reloadData`), so
no restart is needed — but a provider whose pac4j client fails to build is
silently dropped, so watch `cas-server` logs on the first attempt.

Keycloak admin console: <http://localhost:8042> — `admin` / `changeme`.

`./setup.sh` is idempotent; re-run it after any change to `.env`. Options:

- `--check` — verify only, change nothing. Use it first whenever a delegation
  stops working.
- `--no-mongo` — stop after rendering `generated/vitamui-providers.js` and print
  the `mongosh` command instead of running it.
- `--recreate` — drop the container and its embedded database first. The realm
  gets a new SAML signing key, so the providers must be re-provisioned (which
  the same run does).

## The one failure mode to know about

The two protocols do not age the same way. The OIDC provider only stores a URL:
CAS refetches the discovery document and the JWKS at every authentication, so it
survives anything happening to Keycloak. The SAML provider stores a **copy** of
the IdP metadata, signing certificate included. Regenerate the realm keys and
that copy is stale: every assertion is then rejected, CAS logs

```
WARN org.apereo.cas.util.function.FunctionUtils - Signature is not trusted
    AbstractSAML2ResponseValidator.validateSignature
```

and the browser gets an opaque "Accès non autorisé". OIDC keeps working, which
makes the diagnosis misleading.

Two defences are in place:

- the container keeps its database in a named volume, so recreating it no longer
  regenerates the realm keys — only `down -v` does, and that is what
  `./teardown.sh` and `./setup.sh --recreate` deliberately use;
- `./setup.sh` compares, after writing, the certificate stored in mongo with the
  one Keycloak currently signs with, and fails if they differ. `--check` runs
  that comparison alone.

The cure is always the same: **re-run `./setup.sh`**, then wait up to a minute —
CAS rebuilds its pac4j clients on a 60 second cycle, so the previous metadata
stays in effect until the next reload.

`./teardown.sh` removes the providers, the test users, the two email domains,
the container and the `generated/` directory.

## What gets created

**In Keycloak** (`realms/vitamui-realm.json`, realm `vitamui`):

- `vitamui-oidc`, a confidential OIDC client, standard flow, RS256 id tokens.
- a SAML client whose clientId is the service provider entityId, with NameID
  format `email` forced, signed responses and assertions, client signature not
  required, and `email` / `firstName` / `lastName` attribute mappers.
- the two test users above.

**In `iam`** — two documents in `providers` plus their two users, attached to
the organization of `VITAMUI_REFERENCE_USER_EMAIL` (`admin@gmail.com`, i.e.
Client1, in the standard dev dataset). Set `VITAMUI_CUSTOMER_ID` and
`VITAMUI_GROUP_ID` to pin another one.

Delegated authentication provisions nothing: CAS looks the address up in
`iam.users` and stops before redirecting if it is unknown. Both sides therefore
need the same email, which is why `setup.sh` writes to both.

## How the two sides are bound together

Everything hinges on `technicalName`, the name pac4j gives the client:

| | value |
| --- | --- |
| SP entityId (SAML) | `<VITAMUI_CAS_URL>/login/<technicalName>` |
| Callback / ACS | `<VITAMUI_CAS_URL>/login?client_name=<technicalName>` |

Both are computed by `Pac4jClientBuilder`: the entityId explicitly, the callback
through pac4j's default `QueryParameterCallbackUrlResolver`. Keycloak must be
declared with exactly these values, which is what the admin-API step of
`setup.sh` guarantees — change `SAML_TECHNICAL_NAME` or `VITAMUI_CAS_URL` in
`.env` and re-run.

Two provider fields are worth understanding rather than copying:

- **OIDC `mailAttribute: "email"`** is mandatory. Keycloak's `sub` is an opaque
  uuid; without this, `UserPrincipalResolver` would look for a VitamUI account
  named after that uuid.
- **SAML `mailAttribute: ""`** relies on the NameID, which the client forces to
  the email address. The realm also exports an `email` attribute, so setting
  `mailAttribute` to `email` exercises the attribute path instead.

## Constraints

**Keycloak must stay on `http://localhost`.** It issues its session cookies with
`Secure; SameSite=None` regardless of `sslRequired`, and a browser only accepts
those over plain HTTP for `localhost`. Moving `KEYCLOAK_BASE_URL` to another
hostname means serving Keycloak over HTTPS *and* adding its certificate to the
CAS truststore, since CAS fetches the discovery document and the metadata
server-side.

**One email domain per provider.** Within an organization,
`IdentityProviderHelper.findByUserIdentifierAndCustomerId` returns the *first*
provider whose pattern matches — overlapping domains would silently shadow one
of the two. `setup.sh` refuses to run when an existing provider of the target
organization already matches one of the two domains.

**Editing `realms/vitamui-realm.json` needs `--recreate`.** The import strategy
is `IGNORE_EXISTING`: as long as the database survives, an existing realm is left
alone. That is the same persistence that keeps the SAML signing key stable, so
the trade-off is deliberate — and `--recreate` re-provisions VitamUI in the same
run.
