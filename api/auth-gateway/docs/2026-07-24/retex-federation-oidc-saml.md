# Retex — Fédération externe OIDC + SAML (24 juillet 2026)

## Contexte

Suite du POC `api/auth-server/` (SAS 7.0.5 / Spring Boot 4.0.6). Deux chantiers enchaînés en session :

1. **Fédération OIDC externe** — bouclage du chantier commencé la veille, avec plusieurs bugs runtime révélés au premier login réel.
2. **Fédération SAML externe** — nouveau chantier, IdP Keycloak SAML sur le même realm `vitam-ui-ext` que l'OIDC.

Les deux chantiers sont **validés end-to-end** par test manuel (`john.doe@vitam-external.fr`, provisionné à la volée, `TOK-<UUID>` émis, portal accessible).

## Réalisations validées

### 1. Fédération OIDC (fin de chantier)

- `MongoClientRegistrationRepository` dynamique, cache Caffeine 60 s, mapping `IdentityProviderDto` → `ClientRegistration`.
- `FederatedLoginSuccessHandler` : normalise l'`Authentication` OIDC (`OAuth2AuthenticationToken`) en `ExternalIdentity` (record interne), lookup IdP IAM, résolution user via `resolveExternalUser`, JIT si `autoProvisioningEnabled=true`, sinon `error=user_not_registered`.
- Endpoint IAM `POST /iam/v1/cas/users/jit` + `CasService.jitProvisionUser` : crée un `UserDto` avec `defaultGroupId` de l'IdP, langue du customer, mot de passe aléatoire.
- Redirection SPA login : `/oauth2/authorization/{registrationId}` lorsque `protocoleType=OIDC` dans le résolveur HRD.

### 2. Fédération SAML

- `MongoRelyingPartyRegistrationRepository` (Component Spring) : lookup IAM par `registrationId`, cache Caffeine 60 s. Tolère les deux aliases `SAML` et `SAML_RESPONSE` sur `protocoleType`.
- Parsing IdP metadata XML via `RelyingPartyRegistrations.fromMetadata(...)` sur le champ `idpMetadata` du `IdentityProviderDto`.
- Extraction du credential SP (`Saml2X509Credential` SIGNING+DECRYPTION) depuis le champ `keystoreBase64` : PKCS12 avec fallback JKS, prend la première entrée `isKeyEntry`.
- ACS + entityId auto-templatés (`{baseUrl}/login/saml2/sso/{registrationId}` et `{baseUrl}/saml2/service-provider-metadata/{registrationId}`).
- Config chain SAS : `.saml2Login(...)` (repository, `loginPage=/login`, successHandler, `failureUrl=/login?error=federation`) + `.saml2Metadata(withDefaults())`.
- `FederatedLoginSuccessHandler` refactorisé pour supporter **OIDC et SAML** via le même record `ExternalIdentity` — le handler ne branche pas sur le protocole (excepté pour choisir `FactorGrantedAuthority.SAML_RESPONSE_AUTHORITY` vs `AUTHORIZATION_CODE_AUTHORITY`).
- Pom : ajout `spring-security-saml2-service-provider` + **dépôt Nexus Shibboleth** (`https://build.shibboleth.net/maven/releases/`) car OpenSAML 5.1.6 n'est pas sur Maven Central.

### 3. Redirection SPA login

- `app.js` : ajout branche `SAML`/`SAML_RESPONSE` → `window.location.assign('/saml2/authenticate/' + registrationId)`.

## Points durs rencontrés

### 1. `autoProvisioningEnabled` mal mappé côté IAM

**Symptôme** : premier login OIDC → `error=user_not_registered` alors que l'IdP avait bien `autoProvisioningEnabled=true` en Mongo.

**Cause** : converter DTO oubliait de propager le champ.

**Fix** : ajouter le mapping. Bug trivial mais bloquant.

**Leçon** : les DTO IAM exposés au SAS via `/cas/idp/{id}` doivent être exhaustifs — auditer les converters à chaque nouveau champ d'`IdentityProvider`.

### 2. `identifier must be null` au JIT (VitamUI convention)

**Symptôme** : premier JIT → `IllegalArgumentException: identifier must be null` dans `UserService.beforeCreate`.

**Cause** : `dto.setIdentifier(request.getSubjectId())` violait la convention VitamUI qui **génère** l'identifier via `SequencesConstants.USER_IDENTIFIER` en `beforeCreate`.

**Fix** : retirer la ligne.

**Leçon** : ne jamais setter `identifier` côté client d'un service Create dans VitamUI. Le domaine sait s'en charger.

### 3. `Unable to get the security context. You probably are not authenticated` (JIT en mode non-authentifié)

**Symptôme** : le POST `/cas/users/jit` s'exécute sur un endpoint whitelisté (pas de user connecté) et `UserInternalService.create` réclame un `SecurityContext` pour tracer le logbook (`proofTenantIdentifier`).

**Fix** : helper `runAsSystem(customerId, action)` dans `CasService` — pose un `AuthenticationToken` synthétique (`AuthUserDto` avec `level=""` + `HttpContext` synthétique + `proofTenantIdentifier` récupéré via `iamLogbookService.getProofTenantByCustomerId(...)`), exécute l'action, restaure l'auth précédente en `finally`.

**Leçon (⚠️ dette Phase 3)** : `runAsSystem` avec `level=""` est un **bypass total de sécurité**. Doit être remplacé par un canal SAS↔IAM authentifié (mTLS ou HMAC signé) qui exposerait un `createBypassSecurity` contrôlé côté IAM. Ne surtout pas laisser en staging.

### 4. Utilisateur provisionné mais sans `UserInfo` (portal-api 404 sur `/userinfos/me`)

**Symptôme** : login OIDC OK, `TOK-<UUID>` émis, redirect portal OK, mais `/portal-api/userinfos/me` renvoie 404.

**Cause** : `jitProvisionUser` créait le `User` sans son `UserInfo` associé (langue, préférences).

**Fix** : dans le lambda `runAsSystem` : `dto.setUserInfoId(createUserInfo(customer.getLanguage()).getId())` avant `internalUserService.create(dto)`.

**Leçon** : la création d'un user complet dans VitamUI nécessite systématiquement `UserInfo` en amont. Documenter le chemin de création.

### 5. Build OpenSAML « present but unavailable »

**Symptôme** : `mvn compile` sur `api/auth-server` échoue sur `org.opensaml:opensaml-*:5.1.6` non trouvé.

**Cause** : OpenSAML 5.x est publié uniquement sur le Nexus Shibboleth, pas sur Maven Central.

**Fix** : ajout du repo dans `api/auth-server/pom.xml` :
```xml
<repositories>
  <repository>
    <id>shibboleth</id>
    <url>https://build.shibboleth.net/maven/releases/</url>
  </repository>
</repositories>
```

**Leçon** : `spring-security-saml2-service-provider` nécessite ce repo. Vérifier si Nexus interne d'infra le proxy déjà avant staging (sinon ajouter à la config).

### 6. Spring Security 7 : API `Saml2MetadataConfigurer` réduite

**Symptôme** : `.saml2Metadata(m -> m.relyingPartyRegistrationRepository(repo))` — méthode inexistante dans SS 7.

**Fix** : `.saml2Metadata(Customizer.withDefaults())`. Spring 7 autowire le bean `RelyingPartyRegistrationRepository` directement.

**Leçon** : ne pas se fier aux tutoriels SS 6.x pour SAML — l'API a été simplifiée en 7. Toujours consulter la Javadoc de la version en cours.

### 7. HRD renvoyait `SAML` mais le repo n'acceptait que `SAML_RESPONSE` (404 sur `/saml2/authenticate/{id}`)

**Symptôme** : après validation HRD, redirect vers `/saml2/authenticate/{id}` → 404. Logs IAM montraient `protocoleType=SAML`. Mon repo filtrait sur `SAML_RESPONSE` uniquement.

**Cause** : le legacy CAS stockait `SAML` dans la Mongo, la constante Spring authorities utilise `SAML_RESPONSE` (`FactorGrantedAuthority.SAML_RESPONSE_AUTHORITY = "FACTOR_SAML_RESPONSE"`). Les IdP existants ont l'un OU l'autre.

**Fix** : `SAML_PROTOCOL_ALIASES = Set.of("SAML", "SAML_RESPONSE")` + `.toUpperCase()` avant contains.

**Leçon** : les enums texte legacy CAS ne matchent pas toujours les nomenclatures Spring modernes. Toujours accepter les deux aliases quand on découple.

### 8. Keycloak « Invalid Request » sur AuthnRequest (client ID mismatch)

**Symptôme** : Keycloak « We are sorry — Invalid Request ».

**Cause** : le client SAML côté Keycloak doit avoir un client ID **exactement égal** à l'`entityId` du SP, c'est-à-dire `https://dev.vitamui.com:9443/saml2/service-provider-metadata/{registrationId}`. J'avais mis un client ID court par erreur.

**Fix** : renommer le client Keycloak.

**Leçon** : SAML n'est pas OIDC — pas de client-id « logique » séparé, c'est l'entityId qui identifie le SP. Documenter en procédure onboarding IdP SAML.

### 9. `error=federation` — Assertion non signée

**Symptôme** : callback SAML → `error=federation`. Logs SS TRACE : « Assertion is not signed ».

**Cause** : côté Keycloak client SAML, l'option **« Sign Assertions »** était OFF (seul « Sign Documents » était ON — c'est la Response qui était signée, pas l'Assertion incluse).

**Fix** : activer « Sign Assertions » ON côté Keycloak.

**Leçon** : sécurité SAML par défaut de Spring exige la signature d'assertion (pas seulement de la Response). Auditer les IdP existants sur ce point avant migration.

### 10. `invalid_in_response_to` — cookie de session strippé sur le POST cross-site (**bloqueur SAML majeur**)

**Symptôme** : après signature OK côté Keycloak, callback POST vers `/login/saml2/sso/{id}` → `Saml2AuthenticationException: invalid_in_response_to`. Spring ne retrouvait pas l'`AuthnRequest` originale.

**Cause** : SAML POST binding = le browser fait un `POST` cross-site (`keycloak.dev.vitamui.com:9443` → `dev.vitamui.com:9443`, ou même origin en dev mais quand même un POST cross-context). Le cookie `SAS_JSESSIONID` était en `SameSite=Lax` → strippé sur le POST → session perdue → `AuthnRequest` corrélée introuvable.

**Fix** : `SAS_JSESSIONID` passé en `SameSite=none`. Requiert `Secure=true` (OK, HTTPS 9443).

**Leçon** : le POST binding SAML est **fondamentalement incompatible avec `SameSite=Lax`**. Choix :
- soit passer en `SameSite=none` (avec les précautions Secure + CSRF ailleurs),
- soit utiliser le binding Redirect côté IdP (mais alors artifacts en URL, autres compromis).

À documenter dans les prérequis de config prod.

### 11. `error=idp_missing` — `registrationId` null dans `Saml2AssertionAuthentication`

**Symptôme** : callback OK, mais le handler recevait `registrationId=null` du principal → 404 lookup IdP.

**Cause** : dans Spring Security 7, `Saml2AuthenticatedPrincipal.getRelyingPartyRegistrationId()` retourne `null` avec certaines topologies (`Saml2AssertionAuthentication` remonté par le nouveau filtre). Régression ou changement d'API par rapport à SS 6.

**Fix** : fallback dans le handler — parser le dernier segment de `request.getRequestURI()` (le filtre matche exactement `/login/saml2/sso/{id}`, donc le path segment est fiable).

**Leçon** : ne pas dépendre du principal SAML pour retrouver le registrationId — le path est une source plus stable. Idéalement remonter un bug/PR upstream Spring.

### 12. `group and user customerId must be equals` au JIT

**Symptôme** : JIT échoue avec cette erreur.

**Cause** : le `defaultGroupId` configuré sur l'IdP pointait vers un groupe d'un **autre customer** que celui de l'IdP. L'invariant VitamUI exige que user + group partagent le customer.

**Fix** : corriger le `defaultGroupId` en Mongo. Pas de code (mais à documenter).

**Leçon (observation métier)** : à chaque création d'organisation, VitamUI recopie un jeu de groupes canoniques. Le mapping IdP externe → groupe local doit choisir un groupe **du bon customer**. À automatiser via une convention de naming.

### 13. Second login SAML « User already exists » (chemin provisioning au lieu de lookup local)

**Symptôme** : second login (user déjà provisionné) → 500 `User already exists for email X in customer Y`.

**Cause** : `CasService.getUser` appelait `provisionUser` en amont, qui levait `NotFoundException` (aucun `ProvisioningClient` externe configuré pour cet IdP). L'exception remontait, court-circuitant le chemin de lookup local classique. Sur l'OIDC on avait la même bombe latente.

**Fix** : dans `CasService.getUser`, catcher `NotFoundException` du `provisionUser` et **continuer** vers `getUserByEmailAndCustomerId`. Le provisioning externe est optionnel, pas terminal.

**Leçon** : bénéficie aux deux protocoles (OIDC et SAML). Un chemin de code partagé qui traite une absence de service externe comme fatale est un bug systémique — toujours faire de la dégradation gracieuse.

## Chantiers restants (Phase 2+)

Mise à jour de la liste de la veille (chantiers 1 fait à 100% : OIDC ET SAML) :

| # | Chantier | Estim. | Note |
|---|---|---|---|
| 1 | ~~Fédération OIDC/SAML externe~~ | ~~3-4 sem~~ | **✅ FAIT** (OIDC + SAML validés end-to-end sur Keycloak). |
| 2 | Hardening SAS ↔ IAM | 1 sem | **URGENT** — le plus bloquant avant staging. `runAsSystem` doit disparaître, endpoints IAM re-sécurisés (mTLS OU HMAC signé). |
| 3 | `RegisteredClientRepository` Mongo | 1 sem | |
| 4 | Consolidation logout OIDC | 1-2 sem | |
| 5 | Persistance `OAuth2AuthorizationService` | 1 sem | |
| 6 | Password management | 1-2 sem | |
| 7 | Vraie SPA Angular `auth-ui` | 2 sem | |

## Dette Phase 3 accumulée (à traiter avant staging)

Récap consolidé des dettes de sécurité introduites par le POC — à traquer en tickets :

1. **Endpoints IAM whitelisted sans authent** : `/cas/{login,tokens,hrd,subrogations/validate,idp/*,users/provisioning,users/jit}` — à re-sécuriser via mTLS ou HMAC signé.
2. **`runAsSystem` avec `level=""`** = bypass total sécurité côté IAM — à remplacer par un canal authentifié qui exposerait un `createBypassSecurity` contrôlé.
3. **Secrets IdP en clair Mongo** : `clientSecret` (OIDC), `keystoreBase64`, `keystorePassword`, `privateKeyPassword` (SAML) — chiffrement au repos (Vault, jasypt, Mongo CSFLE…).
4. **`GET /cas/idp/{id}` renvoie les secrets en clair** — à découper (endpoint public sans secrets + endpoint interne authent avec).
5. **`idpMetadata` XML statique en base** — automatiser le refresh via URL descriptor pour survivre à la rotation de clés IdP.
6. **Logs TRACE `org.springframework.security.saml2`** encore actifs dans `application.yml` — à baisser à `WARN` avant staging.
7. **`SameSite=none`** exige un audit CSRF exhaustif sur les autres endpoints — sans cookie `SameSite=Lax` par défaut, plus de protection implicite.

## Fichiers touchés

- **Nouveaux** :
  - `api/auth-server/src/main/java/fr/gouv/vitamui/authserver/security/MongoRelyingPartyRegistrationRepository.java`
- **Modifiés** :
  - `api/auth-server/pom.xml` (dep SAML + repo Shibboleth)
  - `api/auth-server/src/main/java/fr/gouv/vitamui/authserver/config/AuthorizationServerConfig.java` (wiring saml2Login + saml2Metadata + permitAll SAML paths + CSRF ignore ACS)
  - `api/auth-server/src/main/java/fr/gouv/vitamui/authserver/security/FederatedLoginSuccessHandler.java` (support SAML via record `ExternalIdentity`, fallback path parsing, choix `FactorGrantedAuthority`)
  - `api/auth-server/src/main/resources/application.yml` (`SAS_JSESSIONID` en `SameSite=none`)
  - `api/auth-server/src/main/resources/static/login/app.js` (branche SAML redirect)
  - `api/api-iam/iam/src/main/java/fr/gouv/vitamui/iam/server/cas/service/CasService.java` (helper `runAsSystem`, fix identifier, ajout `UserInfoId`, catch `NotFoundException` sur provisioning path)
