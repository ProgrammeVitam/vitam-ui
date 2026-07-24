# Journal de session — 24 juillet 2026

Récit chronologique du travail de la journée. Reconstruction narrative destinée à faire mémoire du raisonnement de conception et des diagnostics, pas verbatim de la conversation.

## Contexte de départ

- La Phase 1 du plan est validée : POC minimal SAS avec émission de `TOK-<UUID>` opaque, SPA login vanilla, un client `portal`.
- Phase 2 déjà attaquée sur les jours précédents : HRD N>1 fait, subrogation câblée côté SAS/IAM, multi-clients OIDC configurés.
- La session démarre sur le blocage restant : **le SSO cross-app entre portal (`:4200`) et identity (`:4201`) ne fonctionne pas** — user retombe sur la mire login SAS au lieu d'être authentifié via SSO.

## Investigation SSO cross-app

### Diagnostic initial

- Le user constate que `GET /oauth2/authorize?client_id=identity` renvoie une 302 vers `/login` alors qu'il vient juste de logger sur portal.
- Confirmation : le cookie `JSESSIONID` est bien présent côté navigateur (DevTools → Application).

### Piste #1 : session-fixation & shared SecurityContextRepository

Hypothèse : les 2 `SecurityFilterChain` (Chain 1 OAuth2 + Chain 2 SPA login) utilisaient chacune leur propre `HttpSessionSecurityContextRepository` implicite, donc le SecurityContext écrit par le login SPA (Chain 2) n'était pas lu par `/oauth2/authorize` (Chain 1).

Fix appliqué :
- Bean `SecurityContextRepository` unique injecté dans les deux chains via `http.securityContext(sc -> sc.securityContextRepository(bean))`.
- Ajout de `sessionCreationPolicy(IF_REQUIRED)` + `sessionFixation(none)` pour empêcher la régénération de `JSESSIONID` pendant le flow OAuth2.

Résultat : **inchangé**. La 302 vers `/login` persiste.

### Piste #2 : cookie shadowing entre origines même-host

Lecture des logs TRACE `HttpSessionSecurityContextRepository` :

```
No HttpSession currently exists
Did not find SecurityContext in HttpSession C57357A1F560D959… using the SPRING_SECURITY_CONTEXT session attribute
```

La session existe côté serveur mais **n'a jamais eu de SecurityContext**. Le `Set-Cookie: JSESSIONID=…` réapparaît sur la requête `/oauth2/authorize` → SAS crée une **nouvelle** session à chaque fois.

Après analyse d'un HAR : le browser envoie bien un `JSESSIONID`, mais ce n'est pas celui du login SAS. C'est le `JSESSIONID` du dev-server `ng serve` du portal (`:4200`) qui écrase celui de SAS (`:9443`) — les cookies HTTP n'incluent pas le port dans leur scope, seul le hostname compte (RFC 6265).

Fix appliqué :
- Renommage du cookie de session SAS en `SAS_JSESSIONID` via `server.servlet.session.cookie.name`.
- Attributs explicites : `SameSite=Lax`, `Secure=true`, `HttpOnly=true`.

Résultat : **SSO cross-app fonctionne** ✅. Portal → identity sans re-login.

## Retour sur la subrogation

Le user relance le test complet subrogation. La modal fonctionne, mais dès validation de la subrogation, il est éjecté vers la mire login.

### Diagnostic : setTimeout négatif dans le SubrogationBannerComponent

Analyse du HAR :
- Une vieille `Subrogation` `ACCEPTED` datée d'hier (`date: 2026-07-23T19:38:37.670Z`) est encore en base — le TTL Mongo n'a pas purgé.
- Le `SubrogationBannerComponent` calcule `subrogationTTL = endDate.getTime() - now.getTime()` = **négatif**.
- `setTimeout(logoutFn, -X)` s'exécute immédiatement → `revokeTokenAndLogout()` → retour à la mire.

Fix :
- Nettoyage manuel : `db.subrogations.deleteMany({date:{$lt:new Date()}})`.
- Ajout d'un check server-side dans `CasService.validateSubrogation` : rejette une subrogation dont `date < now`, même si le TTL Mongo ne l'a pas encore purgée.

Résultat : la subrogation aboutit, l'user est bien identifié comme surrogate sur identity ✅.

## Nouveau symptôme : navigate vers portal après subrogation → redevient super-admin

Analyse : chaque frontend a son propre `TOK` isolé dans son `localStorage`. Le portal (:4200) a encore le TOK super-admin de son login initial. Ce TOK est encore valide en Mongo `tokens` → le portal continue à voir super-admin.

Fix appliqué :
- Nouveau `TokenRepository.deleteByRefId(String)` (méthode Spring Data auto-générée).
- Dans `CasService.validateSubrogation` : après validation OK, **suppression en cascade de tous les TOK Mongo du super-user**.

Effet : lorsque le portal fait un prochain appel API, IAM renvoie 401 (TOK plus en Mongo) → portal déclenche `revokeTokenAndLogout` + `/oauth2/authorize` → SAS voit la session subrogée → nouveau TOK surrogate.

Résultat : la subrogation se propage aux apps ouvertes ✅.

## Nouveau symptôme : whitelabel 400 sur `/connect/logout` post-subrogation

Après la navigation portal ↔ identity subrogée, un `/connect/logout` porté par le portal (avec l'ancien `id_token_hint` super-admin) renvoie une erreur 400 whitelabel.

### Diagnostic #1 : `sub` du JWT id_token invalide

Décodage du id_token en cause : le claim `sub` porte **le dump complet de `UserDto.toString()`** — soit ~500 caractères contenant des `AnalyticsDto@1bd121ef` (adresses mémoire).

Cause : `Authentication.getName()` de Spring Security fallback vers `principal.toString()` quand le principal n'est ni `UserDetails` ni `AuthenticatedPrincipal`. Notre `UserDto` Lombok n'implémente rien de tout ça.

Fix appliqué :
- Ajout d'un `OAuth2TokenCustomizer<JwtEncodingContext>` : force `sub = user.id`, ajoute `email`, ajoute `surrogation=true` sur les tokens subrogés.
- Création d'un `VitamuiPrincipal implements AuthenticatedPrincipal` : wrappe le `UserDto`, expose `getName() = user.id`. Utilisé pour `IamAuthenticationProvider` et `LoginController.authenticateSubrogation`.

Résultat : les nouveaux id_tokens ont un `sub` court et stable ✅. Mais le 400 persiste sur les vieux tokens en cache.

### Diagnostic #2 : `OidcLogoutAuthenticationProvider` check `sub`

Après purge du cache portal, retest → le 400 persiste sur les nouveaux tokens.

Lecture de la source SAS 7.0.5 `OidcLogoutAuthenticationProvider.java` : ligne 144-150, comparaison stricte `currentUserPrincipal.getName() == authorizedUserPrincipal.getName()`. En cas de subrogation, current = surrogate id, authorized = super-user id → mismatch → `INVALID_TOKEN sub` → 400.

Fix appliqué : création du `SubrogationTolerantOidcLogoutAuthenticationProvider` qui skip le check `sub` strict.

**Faux départ initial** : mon provider était ajouté via `logout.authenticationProvider(...)` mais le provider par défaut prenait quand même le relais (ordre d'enregistrement non garanti). Après investigation via `OidcLogoutEndpointConfigurer.init()` + logs TRACE : il fallait passer par `logout.authenticationProviders(providers -> {…})` pour **retirer explicitement** le default et ajouter le mien à l'index 0.

### Diagnostic #3 : `NullPointerException` sur `authorizationService`

Après le déploiement du provider tolérant : NPE `this.authorizationService is null`. Cause : `http.getSharedObject(OAuth2AuthorizationService.class)` retourne `null` au moment où ma lambda de config est exécutée — SAS n'a pas encore posé son shared object.

Fix : passer un `Supplier<OAuth2AuthorizationService>` évalué au premier `authenticate()` (mis en cache dans un `volatile` field).

### Diagnostic #4 : `authorization` introuvable après restart SAS

Nouveau symptôme : `INVALID_TOKEN id_token_hint` — `authorizationService.findByToken(idTokenHint, ID_TOKEN_TOKEN_TYPE)` retourne `null`.

Cause : chaque restart SAS purge le storage `InMemoryOAuth2AuthorizationService`. Les frontends conservent leurs id_tokens en `localStorage` et les présentent au logout suivant — SAS ne peut plus les valider via son store.

Fix : mode fallback dans le provider tolérant :
- Décode le JWT via `JwtDecoder` (vérifie signature RSA via JWKS + expiration).
- Résout le `RegisteredClient` via le claim `aud`.
- Laisse passer sans passer par le store.

### Diagnostic #5 : `JwtDecoder` non exposé en shared object

Le fallback échoue avec `JwtDecoder is not yet available`. Contrairement à `OAuth2AuthorizationService`, SAS n'expose pas `JwtDecoder` en shared object.

Fix : injecter le bean `JwtDecoder` directement en paramètre de la méthode `@Bean authorizationServerSecurityFilterChain(HttpSecurity, …, JwtDecoder jwtDecoder)`, le capturer dans le lambda de config du provider.

## Validation subrogation

Après ces fixes cumulés, le user confirme :

- La subrogation se déroule (mire subrogation, saisie password super-user, authentification via IAM).
- Le retour dans l'app identity présente bien le user surrogué.
- Reste des cas edge à peaufiner (logout OIDC en fallback JWT, provider tolérant), mais **la faisabilité de la forced auth pour la subrogation est validée**.

Le user valide l'étape et commit les travaux. Passage à l'archivage documentaire.

## Fichiers touchés dans la session

**Nouveaux** :
- `api/auth-server/src/main/java/fr/gouv/vitamui/authserver/security/VitamuiPrincipal.java`
- `api/auth-server/src/main/java/fr/gouv/vitamui/authserver/security/SubrogationTolerantOidcLogoutAuthenticationProvider.java`
- `api/auth-server/src/main/java/fr/gouv/vitamui/authserver/security/TolerantPostLogoutRedirectUriValidator.java`
- `api/auth-server/src/main/java/fr/gouv/vitamui/authserver/security/SubrogationConstants.java`
- `api/api-iam/iam-commons/src/main/java/fr/gouv/vitamui/iam/common/dto/cas/SubrogationValidateRequestDto.java`
- `api/api-iam/iam-commons/src/main/java/fr/gouv/vitamui/iam/common/dto/cas/SubrogationValidateResponseDto.java`

**Modifiés notables** :
- `api/auth-server/src/main/java/fr/gouv/vitamui/authserver/config/AuthorizationServerConfig.java` — shared repo, session cookie renaming, custom logout provider, jwt customizer, JwtDecoder injection.
- `api/auth-server/src/main/java/fr/gouv/vitamui/authserver/api/LoginController.java` — endpoints subrogation, `VitamuiPrincipal` wrapping.
- `api/auth-server/src/main/java/fr/gouv/vitamui/authserver/security/IamAuthenticationProvider.java` — `VitamuiPrincipal` wrapping.
- `api/auth-server/src/main/java/fr/gouv/vitamui/authserver/security/OpaqueVitamTokenGenerator.java` — détection SUBROGATED, propagation `surrogation=true`.
- `api/auth-server/src/main/resources/application.yml` — SAS_JSESSIONID cookie, TRACE logs, CORS large, liste des 8 clients OIDC.
- `api/auth-server/src/main/resources/static/login/` — écran step-customer + step-subrogation.
- `api/api-iam/iam/src/main/java/fr/gouv/vitamui/iam/server/cas/service/CasService.java` — `validateSubrogation`, invalidation en cascade des TOK.
- `api/api-iam/iam/src/main/java/fr/gouv/vitamui/iam/server/rest/CasController.java` — endpoint `/cas/subrogations/validate`.
- `api/api-iam/iam/src/main/java/fr/gouv/vitamui/iam/server/security/WebSecurityConfig.java` — whitelist des endpoints CAS.
- `api/api-iam/iam/src/main/java/fr/gouv/vitamui/iam/server/token/dao/TokenRepository.java` — `deleteByRefId`.
- `ui/ui-frontend/projects/*/src/assets/config-dev.json` — issuer bascule sur `https://dev.vitamui.com:9443`.
