# POC Spring Authorization Server — Phase 2 (en cours)

## Contexte

Apereo CAS 7.0.10.1 est déployé en overlay WAR dans le module `cas/cas-server/` — coûteux à maintenir (22 exclusions Maven, webflow XML custom, upgrades récurrents). Le POC valide un remplacement par Spring Authorization Server (SAS 7.0.5 / Spring Boot 4.0.6) dans un nouveau module `api/auth-server/` en préservant :

- Le contrat token opaque `TOK-<UUID>` persisté dans la collection Mongo `tokens` (Resource Servers inchangés).
- Le contrat OIDC vu par les 8 frontends Angular (`angular-oauth2-oidc` v21.0.3 continue de tourner).

## Statut d'avancement

### Phase 1 — Terminée ✅

Le happy path fonctionnel a été validé. Livrables :

- Module `api/auth-server/` bootstrap (JAR SB 4.0.6, HTTPS 9443, keystore self-signed `certs/dev-vitamui.p12`).
- `AuthorizationServerConfig` : 2 filter chains + `JWKSource` RSA (réutilise `VitamJwtKeyConfig`) + composite `OAuth2TokenGenerator` (opaque `TOK-<UUID>` + JWT id_token + refresh).
- `RegisteredClientsConfig` : `InMemoryRegisteredClientRepository`.
- `LoginController` : endpoints JSON `/api/login/resolve` et `/api/login/authenticate`.
- SPA vanilla HTML/JS servie sur `/login` (le vrai projet Angular `auth-ui` est reporté).
- Endpoints IAM ajoutés (`POST /iam/v1/cas/tokens`, `GET /iam/v1/cas/hrd`, `POST /iam/v1/cas/login` — whitelistés dans `WebSecurityConfig.getAuthList()`).
- Refactor `CasService.generateAndAddAuthToken` en `createAuthToken(userId, surrogation, api)` public.
- Bascule des configs Angular via `config-dev-sas.json` puis application directe dans `config-dev.json`.

### Phase 2 — En cours

**Chantiers réalisés** :

| Chantier | Statut | Fichiers clés |
|---|---|---|
| HRD multi-organisation (N>1) | ✅ | `LoginController.resolve` retourne `{needsCustomerSelection, entries}` ; SPA affiche écran choix org avec `customerName`+`identityProviderName` ; `CasService.resolveHrdEntries` batch-loookup les customers |
| Subrogation | ✅ (test manuel en attente) | Endpoint IAM `POST /iam/v1/cas/subrogations/validate` + `CasService.validateSubrogation` + SAS `LoginController.authenticateSubrogation` + `SubrogationConstants.SUBROGATED_AUTHORITY` + `OpaqueVitamTokenGenerator` détecte le flag et propage `surrogation=true` |
| Multi-clients OIDC | ✅ | `AuthServerProperties.Client` + liste dans `application.yml` (portal, identity, identityadmin, referential, ingest, archive-search, collect, pastis) avec redirect URIs réels |
| HTTPS activé par défaut | ✅ | `server.ssl.enabled` = `true` avec keystore auto-chargé |
| CORS large | ✅ | `https://dev.vitamui.com:*` + `http://localhost:*` via `setAllowedOriginPatterns` |
| JWT id_token `sub` propre | ✅ | `OAuth2TokenCustomizer<JwtEncodingContext>` — écrit `sub=user.id`, ajoute claims `email` + `surrogation` |
| Revoke pour public client | ✅ | `PublicClientRevocationAuthenticationConverter` + `PublicClientRevocationAuthenticationProvider` — accepte `POST /oauth2/revoke` avec `client_id` sans secret |
| Bean shadowing token generator | ✅ | `OpaqueVitamTokenGenerator` retiré du component-scan pour éviter le fallback SAS par défaut |
| Fix `authenticationTime` id_token | ✅ | `FactorGrantedAuthority.PASSWORD_AUTHORITY` ajouté aux authorities |

**Blocage en cours — SSO cross-app entre :4200 et :4201**

Symptôme : après login sur portal (:4200), l'ouverture d'identity (:4201) devrait déclencher `/oauth2/authorize?client_id=identity` avec réponse `302 → :4201/user?code=…` (SSO OK). Aujourd'hui : `302 → /login` (nouvelle authent demandée), alors que le browser envoie bien le cookie `JSESSIONID` de :9443.

Un premier fix a été appliqué (bean `SecurityContextRepository` unique partagé entre les 2 SecurityFilterChain + `sessionFixation.none()` + `sessionCreationPolicy.IF_REQUIRED`) sans effet.

**Cause probable identifiée** : `HttpSecurityConfiguration` de Spring Security 7 ajoute par défaut un `LogoutFilter` sur `/logout` et SAS 7 pose un `OidcLogoutEndpointFilter` sur `/connect/logout`. Un appel de `revokeTokenAndLogout()` côté `angular-oauth2-oidc` (par exemple lors d'un refresh silent, d'un event tab-visibility, ou d'une navigation) invalide la `HttpSession` entre les deux `/oauth2/authorize`.

**Démarche à venir — diagnostic d'abord, patch ensuite**

1. **Activer des logs TRACE** dans `api/auth-server/src/main/resources/application.yml` :
   ```yaml
   logging.level:
     org.springframework.security.web.context.HttpSessionSecurityContextRepository: TRACE
     org.springframework.security.web.access: DEBUG
     org.springframework.security.web.authentication.logout: DEBUG
     org.springframework.security.oauth2.server.authorization.web: DEBUG
   ```

2. **Rejouer le scénario** portal → identity et lire les logs SAS dans l'ordre :
   - `"Retrieved SecurityContextImpl ..."` sur la 2e `/oauth2/authorize` → la session porte l'auth ; problème ailleurs (voir hypothèse #3 du rapport).
   - `"No SecurityContext was available from the HttpSession"` → l'attribut a été perdu ; regarder les logs `LogoutFilter` juste avant.
   - Aucun log de la 2e requête → la Chain 1 ne matche pas (peu probable).

3. **Test isolé en curl** (garde le cookie via `-c /tmp/j.txt -b /tmp/j.txt`) :
   ```bash
   # 1. Auth
   curl -kc /tmp/j.txt -b /tmp/j.txt -X POST -H 'Content-Type: application/json' \
     -d '{"email":"…","password":"…","customerId":"…"}' \
     https://dev.vitamui.com:9443/api/login/authenticate

   # 2. Rejouer /oauth2/authorize avec le même cookie, mais client_id=identity
   curl -kv -b /tmp/j.txt -o /dev/null \
     "https://dev.vitamui.com:9443/oauth2/authorize?response_type=code&client_id=identity&…"
   ```
   Si `Location: /oauth2/authorize?…&code=…` : SSO côté serveur OK, le problème est côté portal (probable revoke intempestif). Si `Location: /login` : la session a bien été effacée entre les 2.

4. **Fix ciblé selon le diagnostic** — 2 options exclusives à basculer dans `AuthorizationServerConfig` :
   - **Si LogoutFilter par défaut est responsable** : sur la Chain 2, ajouter `http.logout(l -> l.disable())`.
   - **Si `OidcLogoutEndpointFilter` de SAS est responsable** : sur la Chain 1, override le logout handler :
     ```java
     authorizationServer.oidc(oidc ->
       oidc.logoutEndpoint(logout ->
         logout.logoutHandler((req, res, auth) -> { /* no-op ou logout côté IAM seulement */ })
       )
     );
     ```

**Chantiers Phase 2 restants après résolution SSO** (par priorité) :

| # | Chantier | Estim. | Note |
|---|---|---|---|
| 1 | Test manuel subrogation | 0,5 j | En attente depuis le SSO fix ; débloquable dès que le flow logout/re-auth de subrogation ne perturbe plus la session |
| 2 | Fédération OIDC/SAML externe | 3-4 sem | `spring-security-oauth2-client` + `spring-security-saml2-service-provider` avec repositories Mongo dynamiques (mapping `IdentityProviderDto` → `ClientRegistration` / `RelyingPartyRegistration`) |
| 3 | Hardening SAS ↔ IAM | 1 sem | Remettre `@Secured` sur les 3 endpoints IAM avec mTLS OU HMAC signé — indispensable avant staging |
| 4 | Logout + end-session OIDC | 1 sem | `LogoutSuccessHandler` custom qui appelle IAM pour supprimer le `TOK-<UUID>` en Mongo — au lieu de compter sur `/oauth2/revoke` browser |
| 5 | Password management | 1-2 sem | Reset (email) + change + policy |
| 6 | `RegisteredClientRepository` Mongo | 1 sem | Remplacer `InMemoryRegisteredClientRepository` — sans ça, ajouter un client OIDC en prod = redéploiement SAS |
| 7 | Vraie SPA Angular `auth-ui` | 2 sem | Remplacer la SPA vanilla par un projet Angular dans le workspace, avec les composants réutilisables de `vitamui-library` |

## Fichiers critiques

**Modifiés récemment pour ce blocage** (à ré-examiner avec logs TRACE) :
- `/home/danrad/git/vitam-ui/api/auth-server/src/main/java/fr/gouv/vitamui/authserver/config/AuthorizationServerConfig.java` — Chain 1 et Chain 2, `SecurityContextRepository` bean, `sessionFixation.none()`.
- `/home/danrad/git/vitam-ui/api/auth-server/src/main/java/fr/gouv/vitamui/authserver/api/LoginController.java` — `authenticate()` et `authenticateSubrogation()` persistent le `SecurityContext` via le bean partagé.
- `/home/danrad/git/vitam-ui/api/auth-server/src/main/resources/application.yml` — ajouter la section `logging.level` pour le diagnostic.

**Réutilisés (ne pas modifier)** :
- `VitamJwtKeyConfig` (dupliqué depuis auth-gateway) — `JWKSource` RSA.
- `SubrogationConstants.SUBROGATED_AUTHORITY` — marqueur transmis via authorities jusqu'au token generator.
- `iam-security/RequestHeadersAuthenticationFilter` — inchangé côté Resource Servers.

## Vérification end-to-end

1. **Diagnostic logs** : ajouter la section `logging.level`, redémarrer SAS, rejouer le scénario portal → identity avec DevTools ouvert, capturer :
   - Le contenu de la session côté SAS (via logs TRACE).
   - Les status des `/logout`, `/connect/logout`, `/oauth2/revoke` déclenchés par le portal (via DevTools Network).

2. **Test SSO isolé en curl** (voir bloc au-dessus).

3. **Test SSO browser** (après fix) :
   - Vider cookies + storage des 2 apps.
   - Login sur portal.
   - Ouvrir identity : ne doit **pas** montrer `/login`, doit charger directement le dashboard.

4. **Non-régression subrogation** : dérouler le scénario complet (portal → modal subrogation → surrogate ACCEPTED → logout + re-login SAS → subrogated dashboard identity) et vérifier en Mongo :
   ```bash
   mongosh --eval 'use cas; db.tokens.find({surrogation:true}).sort({createdDate:-1}).limit(1)'
   ```
   → doit contenir le nouveau `TOK-<UUID>` avec `refId = <surrogate.userId>`.

5. **Non-régression clients OIDC** : ouvrir successivement portal, identity, referential, ingest, archive-search, collect, pastis via `/oauth2/authorize?client_id=<X>` — chacun doit émettre son propre `TOK-<UUID>` sans re-login (SSO).
