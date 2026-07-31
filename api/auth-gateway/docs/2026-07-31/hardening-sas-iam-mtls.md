# Hardening du canal SAS ↔ IAM — mTLS (31 juillet 2026)

## Contexte

Suite au POC OIDC/SAML validé le 24 juillet, les 7 endpoints IAM appelés par l'auth-server étaient **whitelistés sans authentification** (`WebSecurityConfig.getAuthList`), ouvrant plusieurs risques critiques : création de user via `/users/jit`, minting de `TOK-<UUID>` via `/tokens`, exposition des secrets IdP en clair via `/idp/{id}`, etc. Le helper `runAsSystem` posé dans `CasService` était par ailleurs un bypass total du framework de sécurité.

Ce chantier remplace ces bypass par un canal mTLS de bout en bout, en réutilisant l'infrastructure X509 déjà présente dans VitamUI (`X509AuthenticationFilter`, `RequestHeadersAuthenticationFilter`, truststore commun).

## Approche retenue

**mTLS plutôt que HMAC** : l'infra IAM écoute déjà en `client-auth: want` avec truststore commun ; les autres services internes (`iam-external`, `referential`, etc.) utilisent le même pattern ; pas de secret partagé à rotationner ; cohérent avec l'existant.

**Certificat SAS** : réutilisation directe du keystore `keystore_cas-server.p12` (CN=`cas-server`) déjà signé par la CA `vitamui-services` — l'auth-server prenant précisément la place du slot CAS historique. Zéro nouvelle infra PKI en dev.

## Réalisations

### 1. SAS présente son certificat client mTLS

- `IamClient` refactorisé pour construire son `RestClient` autour d'un `HttpComponentsClientHttpRequestFactory` alimenté par un `SslBundle` Spring Boot 4 (bundle `iam-mtls`).
- `AuthServerProperties.Iam` étendu avec `sslBundle` (nom du bundle) et `disableHostnameVerification` (dev only : les certs ont CN=`iam` alors que la connexion vise `localhost`).
- Intercepteur `RestClient` ajouté sur toutes les requêtes IAM : positionne `X-Origin: INTERNAL`, `X-Application-Id: auth-server`, `X-Request-Id: <uuid>` — sans ces headers, le filtre pré-auth IAM lève `BadRequestException("Missing request origin header")`.
- `application.yml` : bloc `spring.ssl.bundle.jks.iam-mtls` + prop `vitamui.auth-server.iam.ssl-bundle: iam-mtls`. `trust-all-certs` reste comme escape hatch dev mais est off par défaut.
- Keystore et truststore copiés dans `api/auth-server/src/main/resources/certs/`.

### 2. IAM reconnaît le peer SAS via son CN

- Nouveau `AuthServerSystemAuthenticator` (`@Service` dans `iam-security`) : à partir du `X509Certificate` extrait par le filter, compare le CN au set configuré (`iam.auth-server.accepted-cns`, défaut `cas-server`). Si match → retourne un `AuthenticationToken` avec un `AuthUserDto` synthétique (`level=""`, `identifier=system-sas`) et l'autorité `ROLE_SYSTEM_SAS`.
- `ApiAuthenticationProvider` interroge ce provider **avant** le dispatch external/internal habituel — si le cert match, on court-circuite immédiatement ; sinon, la chaîne d'auth normale prend le relais.
- Constante `ServicesData.ROLE_SYSTEM_SAS` ajoutée dans `commons-api`.

### 3. Retrait complet du whitelist et sécurisation par annotation

- `WebSecurityConfig.getAuthList()` : retrait des 7 chemins CAS. Les endpoints repassent désormais dans la chaîne `ApiWebSecurityConfig` standard qui exige un principal authentifié.
- Chaque méthode `CasController` correspondante annotée `@Secured(ServicesData.ROLE_SYSTEM_SAS)` : `login`, `getUser` (provisioning), `createAuthToken`, `resolveHrd`, `jitProvisionUser`, `getIdentityProvider`, `validateSubrogation`.

### 4. Suppression du helper `runAsSystem`

Le principal posé par `AuthServerSystemAuthenticator` porte déjà `level=""`, ce qui satisfait `SecurityService.isLevelAllowed(...)` pour toute création. Le proof-tenant pour le logbook est déjà résolu via `getProofTenantIdentifierByCustomerId(user.getCustomerId())` en aval — pas besoin de le poser sur le principal.

Résultat : `CasService.runAsSystem(...)` supprimé (une trentaine de lignes), l'appel dans `jitProvisionUser` devient un simple `userService.create(dto)`. Imports orphelins nettoyés (`AuthenticationToken`, `HttpContext`, `SecurityContextHolder`, `Authentication`).

## Fichiers touchés

**Nouveaux** :
- `api/api-iam/iam-security/src/main/java/fr/gouv/vitamui/iam/security/provider/AuthServerSystemAuthenticator.java`
- `api/auth-server/src/main/resources/certs/keystore_auth-server.p12` (copie du `keystore_cas-server.p12`)
- `api/auth-server/src/main/resources/certs/truststore_vitamui-services.p12` (copie)

**Modifiés** :
- `commons/commons-api/src/main/java/fr/gouv/vitamui/commons/api/domain/ServicesData.java` (constante `ROLE_SYSTEM_SAS`)
- `api/api-iam/iam-security/src/main/java/fr/gouv/vitamui/iam/security/provider/ApiAuthenticationProvider.java` (interception SAS en amont)
- `api/api-iam/iam/src/main/java/fr/gouv/vitamui/iam/server/config/ApiIamServerConfig.java` (injection du nouvel authenticator)
- `api/api-iam/iam/src/main/java/fr/gouv/vitamui/iam/server/security/WebSecurityConfig.java` (retrait whitelist 7 endpoints)
- `api/api-iam/iam/src/main/java/fr/gouv/vitamui/iam/server/rest/CasController.java` (7 x `@Secured(ROLE_SYSTEM_SAS)`)
- `api/api-iam/iam/src/main/java/fr/gouv/vitamui/iam/server/cas/service/CasService.java` (suppression `runAsSystem`, Javadocs)
- `api/auth-server/src/main/java/fr/gouv/vitamui/authserver/security/IamClient.java` (SslBundle + intercepteur headers)
- `api/auth-server/src/main/java/fr/gouv/vitamui/authserver/config/AuthServerProperties.java` (`sslBundle`, `disableHostnameVerification`)
- `api/auth-server/src/main/resources/application.yml` (bloc `spring.ssl.bundle.jks.iam-mtls` + prop `ssl-bundle`)

## Vérification

À rejouer en manuel avec IAM + auth-server + Keycloak up :

1. **Rejet sans cert** : `curl -k https://localhost:8083/iam/v1/cas/hrd?email=x@y.z` → 401 (avant : 200 avec liste vide).
2. **Acceptation avec cert** :
   ```bash
   curl --cacert dev-deployment/pki/ca/vitamui-services/ca-intermediate.crt \
        --cert-type P12 \
        --cert dev-deployment/environments/keystores/vitamui-services/clients/keystore_cas-server.p12:changeme \
        -H "X-Origin: INTERNAL" -H "X-Application-Id: auth-server" -H "X-Request-Id: t1" \
        "https://localhost:8083/iam/v1/cas/hrd?email=john.doe@vitam-external.fr"
   ```
   → réponse HRD nominale.
3. **E2E OIDC** : login portal (`admin@vitamui-recette.fr` puis `john.doe@vitam-external.fr` fédéré) → user provisionné → `TOK-<UUID>` → portal accessible.
4. **E2E SAML** : `john.doe@vitam-ext.fr` → mire Keycloak SAML → callback → user provisionné → portal accessible.
5. **E2E subrogation** : mire de subrogation → validation → nouveau `TOK` subrogé.
6. **Logs** : vérifier `AuthServerSystemAuthenticator` logue « Authenticated auth-server system peer (CN=cas-server) » à chaque appel.

## Dette Phase 3 mise à jour

Items 1 et 2 de la dette accumulée par le POC sont désormais **fermés** :

| # | Dette | Statut |
|---|---|---|
| 1 | Endpoints IAM whitelistés sans authent | ✅ Fermé (mTLS + `@Secured(ROLE_SYSTEM_SAS)`) |
| 2 | `runAsSystem` avec `level=""` = bypass total | ✅ Fermé (principal réel posé par le framework, helper supprimé) |
| 3 | Secrets IdP en clair Mongo (`clientSecret`, `keystoreBase64`, passwords) | ⏳ ouvert |
| 4 | `GET /cas/idp/{id}` renvoie les secrets en clair | ⏳ ouvert (mais désormais mTLS-only) |
| 5 | `idpMetadata` XML statique en base | ⏳ ouvert |
| 6 | Logs TRACE `org.springframework.security.saml2` actifs | ⏳ ouvert |
| 7 | Audit CSRF exhaustif après `SameSite=none` | ⏳ ouvert |

## Chantiers restants (Phase 2+)

| # | Chantier | Estim. | Note |
|---|---|---|---|
| 1 | ~~Fédération OIDC/SAML externe~~ | | ✅ FAIT (24/07) |
| 2 | ~~Hardening SAS ↔ IAM~~ | | ✅ FAIT (31/07) |
| 3 | `RegisteredClientRepository` Mongo | 1 sem | |
| 4 | Consolidation logout OIDC | 1-2 sem | |
| 5 | Persistance `OAuth2AuthorizationService` | 1 sem | |
| 6 | Password management | 1-2 sem | |
| 7 | Vraie SPA Angular `auth-ui` | 2 sem | |
