# Clôture Phase 2 — POC Spring Authorization Server (31 juillet 2026)

## Synopsis

Fin de la Phase 2 du remplacement d'Apereo CAS 7.0.10.1 par **Spring Authorization Server 7.0.5** (SAS) sur Spring Boot 4.0.6, dans le module `api/auth-server/`. Les Resource Servers et les 8 SPAs Angular restent inchangés — le contrat `TOK-<UUID>` opaque en Mongo `tokens` et le contrat OIDC (`angular-oauth2-oidc` v21) sont préservés.

Cette page consolide les chantiers livrés depuis Phase 1 et l'état de la dette Phase 3.

## Chantiers Phase 2 livrés

| # | Chantier | Statut | Retex détaillé |
|---|---|---|---|
| 1 | Fédération OIDC + SAML externe (Keycloak) | ✅ | `2026-07-24/retex-federation-oidc-saml.md` |
| 2 | Hardening SAS ↔ IAM (mTLS) | ✅ | `2026-07-31/hardening-sas-iam-mtls.md` |
| 3 | `RegisteredClientRepository` Mongo | ✅ | ce document |
| 4 | Consolidation logout OIDC | ✅ | ce document |
| 5 | Persistance `OAuth2AuthorizationService` | ✅ | ce document |
| 6a | Change password (user connecté) | ✅ | ce document |
| 6b | Password policy exposée au front | ✅ | ce document |
| 6c | Reset password (mot de passe oublié) | ✅ | ce document |
| 6d | Welcome email à la création d'un user | ✅ | ce document |
| 7 | Portage SPA Angular (`auth-ui`) | ⏸ **reporté** | à planifier |

## Nouveautés structurelles (récap)

### DB dédiée `auth-server`

Nouvelle base Mongo `auth-server` sur l'instance dev partagée (port 27018), user `mongod_dbuser_authserver`. Trois collections :
- `registered_clients` — 8 clients OAuth2 (portal, identity, identityadmin, referential, ingest, archive-search, collect, pastis), champs plats + `clientSettings`/`tokenSettings` en JSON strings. Bootstrap idempotent depuis `application.yml` via `CommandLineRunner` avec UUID stables (`nameUUIDFromBytes`).
- `oauth2_authorizations` — persistance SAS (survit aux restarts). Champs plats + 4 blocs de tokens (auth code / access / id / refresh), indexes sparse sur chaque `*Value` pour `findByToken` O(log n).
- `password_reset_tokens` — one-shot nonces avec TTL index Mongo natif (30 min pour reset, 24 h pour welcome).

**Bootstrap pour nouveaux devs** :
- `deployment/scripts/mongod/v10.0/0-02_users-authserver.js.j2` (playbook Ansible)
- `tools/docker/mongo/mongo_vars_dev.yml` (vars dev)
- `api/auth-server/README.md` : one-liner `mongosh` pour les envs pré-existants
- `deployment/environments/group_vars/all/vault-mongodb.yml.example` (bloc `authserver:` pour staging/prod)

### Canal SAS ↔ IAM mTLS

- SAS présente `keystore_auth-server.p12` (CN=`cas-server`, réutilise le slot CAS historique dans la PKI `vitamui-services`).
- IAM reconnaît le peer via `AuthServerSystemAuthenticator` qui promeut CN=`cas-server` en `ROLE_SYSTEM_SAS`.
- Les 8 endpoints IAM `/cas/*` (login, tokens, tokens/invalidate, hrd, subrogations/validate, idp/*, users/provisioning, users/jit, password/change, password/policy, users?email=) sont annotés `@Secured(ROLE_SYSTEM_SAS)` — plus rien dans `getAuthList()` whitelist.
- Le helper `runAsSystem` de `CasService` (bypass total sécu Phase 2 initiale) est **supprimé** — le principal réel de la chaîne mTLS suffit.
- Trois headers automatiques par appel : `X-Origin: INTERNAL`, `X-Application-Id: auth-server`, `X-Request-Id: <uuid>`, `X-Tenant-Id: -1` (marker no-tenant).
- Dev : `directConnection=true` sur l'URI Mongo pour bypass replica-set discovery.

### Persistance SAS

- Deux repos Mongo custom (`MongoRegisteredClientRepository`, `MongoOAuth2AuthorizationService`) alignés sur le pattern JDBC officiel de SAS 7.0.5.
- ObjectMapper avec `SecurityJackson2Modules` + `OAuth2AuthorizationServerJackson2Module` + `BasicPolymorphicTypeValidator` permissif (packages `java.` / `org.springframework.` / `fr.gouv.vitamui.`) — le `AllowlistTypeIdResolver` par défaut ne connaît pas nos types custom.
- Mixins et `@JsonCreator` sur `VitamuiPrincipal`, `CustomerIdAuthenticationDetails`, plus un `JsonDeserializer` custom pour `FactorGrantedAuthority` (factory-methods only).
- Nettoyage : `SubrogationTolerantOidcLogoutAuthenticationProvider` allégé — le fallback JWT decode devient inutile puisque les authorizations survivent aux restarts.

### Cross-app logout

- Nouvel endpoint IAM `POST /cas/tokens/invalidate?userId=X` (`@Secured(ROLE_SYSTEM_SAS)`) qui purge tous les `TOK-<UUID>` d'un user.
- Nouveau `IamRevocationLogoutSuccessHandler` côté SAS : après acceptation OIDC logout, POST vers IAM pour purger les tokens → les autres apps Angular voient un 401 au prochain appel Resource Server → re-auth SSO.
- Wrappé autour du `OidcLogoutAuthenticationSuccessHandler` de SAS pour préserver le redirect vers `post_logout_redirect_uri`.

### Password management (self-service)

- **Change** : endpoint SAS `POST /api/password/change` (authentifié). Re-auth via ancien password avant applique nouveau. Cross-check l'user réel via `iamClient.getUsersByEmail(email)` — immunise le flow aux false-positives HRD.
- **Policy** : endpoint SAS `GET /api/password/policy` (public, cache Caffeine 5 min) qui proxy vers IAM `GET /cas/password/policy`. Rendered sur les écrans change et reset comme liste de règles humaines.
- **Reset** : endpoint SAS `POST /api/password/reset/request` (public, opaque 200, anti-enumeration) + `POST /api/password/reset`. Nonce Base64 32 B, TTL 30 min, one-shot (delete explicite + TTL index Mongo).
- **Welcome** : endpoint SAS `POST /api/password/first-connection` (public, opaque 200, TTL 24 h). IAM `UserEmailService` refactoré pour POST vers ce endpoint via mTLS (bean `authServerClientProperties`), en remplacement de l'appel CAS legacy mort. Le gate `identifierMatchProviderPattern` historique est préservé.
- **Envoi email** via `spring-boot-starter-mail` → MailHog en dev (nouveau `tools/docker/mailhog/`, SMTP 1025, UI 8025). Templates HTML + text inline. Bean `PasswordResetMailer` avec méthodes `send` (reset) et `sendWelcome` (bienvenue).
- **Écrans vanilla** ajoutés : `/change-password`, `/reset-password` (bi-modal email/token+password). Lien « Mot de passe oublié ? » sur la mire login.
- **API HTTP status** : `HttpStatusEntryPoint(401)` sur `/api/**` pour éviter les redirects HTML vers `/login` qui perturbaient les XHR SPA.

## Fichiers touchés (récap fin de Phase 2)

**Nouveaux modules / fichiers importants** :
- `api/auth-server/` — module SAS complet (Phase 1 + toutes extensions Phase 2)
- `api/api-iam/iam-security/.../AuthServerSystemAuthenticator.java`
- `tools/docker/mailhog/` — infra dev SMTP
- `commons/commons-api/.../ServicesData.java` — constante `ROLE_SYSTEM_SAS`
- `deployment/scripts/mongod/v10.0/0-02_users-authserver.js.j2` — script Ansible user Mongo

**Config modifiée** :
- `api/api-iam/iam/src/main/resources/application-dev.yml` — nouveau bloc `auth-server-client:`
- `api/api-iam/iam/src/main/java/fr/gouv/vitamui/iam/server/rest/CasController.java` — 8 endpoints `@Secured(ROLE_SYSTEM_SAS)`
- `api/api-iam/iam/src/main/java/fr/gouv/vitamui/iam/server/security/WebSecurityConfig.java` — whitelist réduite (retrait des 7 chemins `/cas/*`)
- `api/api-iam/iam/src/main/java/fr/gouv/vitamui/iam/server/user/service/UserEmailService.java` — POST vers SAS au lieu de GET CAS legacy
- `api/api-iam/iam/src/main/java/fr/gouv/vitamui/iam/server/cas/service/CasService.java` — `runAsSystem` supprimé, ajout `getPasswordPolicy()`
- Nombreux imports/renommages spotless-conformés

## Dette Phase 3 consolidée

### Sécurité (bloquants avant staging)

1. **Rate-limiting** des endpoints publics `/api/password/reset/request` et `/api/password/first-connection` — un attaquant peut spammer des emails à des cibles arbitraires. À mitiger par IP + par email destinataire (Bucket4j, Redis, ou WAF).
2. **Cert `dev-vitamui.p12` self-signed** — à régénérer signé par la CA `vitamui-services` pour que le mTLS server-side propre. Contournement dev : cert ajouté au truststore commun.
3. **Endpoint `/cas/idp/{id}` renvoie les secrets IdP en clair** — `clientSecret` (OIDC), `keystoreBase64`, `keystorePassword`, `privateKeyPassword` (SAML). À découper (endpoint public sans secrets + endpoint interne). Idéalement, chiffrer au repos (Vault, jasypt, Mongo CSFLE).
4. **Logs TRACE Spring Security** encore actifs dans `application.yml` — à baisser à `WARN` avant staging.
5. **`SameSite=none` sur SAS_JSESSIONID** (imposé par SAML POST binding) — audit CSRF exhaustif requis sur tous les endpoints qui mutent, sans compter sur la protection implicite de Lax.

### Robustesse

6. **HRD IdP-pattern-based** peut false-positive (deux customers avec des patterns qui overlappent sur un même domaine email — cas rencontré en test avec Client2 qui volait `.*@change-it.fr` au system_customer). Nos endpoints password se protègent via `getUsersByEmail`, mais le login initial SPA reste HRD-based. À durcir (matching contre users réels, ou disambiguation UX systématique quand N>1).
7. **`idpMetadata` XML SAML statique** en base — automatiser le refresh périodique via URL descriptor pour survivre à la rotation de clés Keycloak.
8. **Templates email** codés en Java hard — à externaliser (Thymeleaf ou Freemarker) pour i18n et branding.
9. **SAS_JSESSIONID en mémoire** — sessions perdues au restart. À basculer sur Spring Session (Mongo ou Redis) pour continuité de service en cluster.
10. **Migration Jackson 2 → Jackson 3** — Spring Security 7 déprécie `SecurityJackson2Modules` et `OAuth2AuthorizationServerJackson2Module`. Migration reportée en synchro avec Boot lui-même (~SS 8.x / Boot 4.x fin de cycle).

### Fonctionnel

11. **Portage SPA Angular** (chantier #7) — les 3 mini-SPAs vanilla (`login`, `change-password`, `reset-password`) restent en HTML/JS/CSS inline sous `static/`. À porter dans un vrai projet Angular du workspace avec composants VitamUI (design system, i18n, tests).
12. **Écrans manquants** : subrogation refresh, gestion multi-organisation pour reset password (le POC ne supporte que N=1), verrouillage compte après N échecs de reset.

## Vérifications end-to-end validées

Toutes ces scenarii ont été testés manuellement en dev sur cette session ou les précédentes :

- Login mot de passe (N=1 customer, internal IdP)
- Login mot de passe (N>1 customers, écran de choix)
- Login fédéré OIDC (Keycloak realm `vitam-ui-ext`)
- Login fédéré SAML (Keycloak realm `vitam-ui-ext`)
- JIT provisioning au premier login fédéré
- Subrogation (GENERIC)
- Logout OIDC single-app
- Logout OIDC cross-app (portal → identity 401)
- Restart SAS puis logout (authorizations survivent)
- Change password (user connecté)
- Reset password via email (MailHog → clic lien → new password)
- Password policy affichée aux 2 endroits
- Welcome email à création de user
- mTLS SAS→IAM avec 401 sans cert / 200 avec cert
- Endpoint `curl -k` sans cert refusé

## État de qualité connu (redite Phase 2)

- POC opérationnel end-to-end en dev, mais **pas prêt pour staging** tant que la dette Phase 3 §1-4 n'est pas traitée.
- Aucun test automatisé E2E côté SAS — tests unitaires uniquement (`UserEmailServiceTest` remis à niveau, autres inchangés).
- La SPA vanilla est fonctionnelle mais moche par rapport aux SPAs vitam-ui existantes — le portage Angular (#7) est plus une question d'UX que de logique.

## Prochaines étapes

1. **Chantier #7 (SPA Angular auth-ui)** — à planifier dans une session dédiée, ~2 sem estimées.
2. **Traiter la dette Phase 3 §1-4** avant tout staging.
3. **Bench charge SAS** — vérifier que la persistance Mongo tient le trafic prod (issue/consume nonces, save/find authorizations). Voir aussi si le cache Caffeine 60 s sur les IdPs est bien dimensionné.
4. **Décision Fedcm/BFF pattern ?** — le POC est en OAuth2 code + PKCE public. Une revue architecture pour envisager un BFF (Backend-for-Frontend) est possible à moyen terme.

## Historique complet

Voir les retex individuels :
- `2026-07-24/retex-federation-oidc-saml.md` (chantier #1)
- `2026-07-31/hardening-sas-iam-mtls.md` (chantier #2)
- Les chantiers #3 à #6d sont consolidés dans ce document (pas de retex séparé — mieux vaut le condensé).
