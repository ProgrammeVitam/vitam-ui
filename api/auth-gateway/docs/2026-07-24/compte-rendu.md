# Compte-rendu — 24 juillet 2026

## Contexte

Session dédiée à la validation opérationnelle du POC **Spring Authorization Server (SAS 7.0.5 / Spring Boot 4.0.6)** comme substitut d'Apereo CAS 7.0.10.1, dans le module `api/auth-server/`. Objectif : préserver le contrat token opaque `TOK-<UUID>` en base Mongo (Resource Servers inchangés) et le contrat OIDC (`angular-oauth2-oidc` v21) vu par les 8 frontends Angular.

L'objectif de la journée a été de compléter la **Phase 2** du plan : brancher les scénarios réels (subrogation, SSO cross-app, multi-clients OIDC) en aval du POC minimal validé précédemment.

## Réalisations validées ✅

### 1. HRD multi-organisation (N>1)

- Enrichissement `HrdEntryDto` avec `customerName` et `identityProviderName` (lookup batch `customerRepository.findAllById()` dans `CasService.resolveHrdEntries`).
- `LoginController.resolve` renvoie désormais `{needsCustomerSelection, entries}` (200 uniforme, plus de 409).
- SPA login : nouvel écran `step-customer` avec liste des orgs, badge interne/externe, click-to-select.

### 2. Subrogation

- Endpoint IAM `POST /iam/v1/cas/subrogations/validate` (+ `CasService.validateSubrogation`) : vérifie qu'une `Subrogation` `ACCEPTED` existe entre super-user et surrogate, rejette les entrées expirées (`date < now`) même si le TTL Mongo ne les a pas purgées.
- Endpoint SAS `POST /api/login/authenticate-subrogation` : valide le password super-user via IAM, valide la subrogation via IAM, crée une `Authentication` avec principal = surrogate + marker `SUBROGATED`.
- `GET /api/login/context` inspecte la `SavedRequest` pour détecter les params surrogate et faire commuter la SPA.
- `OpaqueVitamTokenGenerator` détecte l'autorité `SUBROGATED` et émet un `TOK` avec `surrogation=true`.
- Invalidation en cascade : la validation subrogation supprime tous les `TOK` actifs du super-user en Mongo (`TokenRepository.deleteByRefId`) pour propager la bascule aux autres apps ouvertes (portal, ingest…) via 401 → re-auth SSO.

### 3. SSO cross-app entre :4200 et :4201

- Bean `SecurityContextRepository` unique partagé entre Chain 1 (OAuth2 endpoints) et Chain 2 (SPA login).
- `sessionCreationPolicy(IF_REQUIRED)` + `sessionFixation(none)` sur les deux chaînes.
- **Renommage du cookie de session** : `SAS_JSESSIONID` (avec `SameSite=Lax`, `Secure`, `HttpOnly`) pour éviter la collision avec les `JSESSIONID` émis par les dev-servers `ng serve` des frontends sur le même hostname `dev.vitamui.com`.

### 4. Multi-clients OIDC

- `AuthServerProperties.Client` (list de `{clientId, redirectUris, postLogoutRedirectUris}`) + provisionnement `application.yml` des 8 clients (portal, identity, identityadmin, referential, ingest, archive-search, collect, pastis) avec redirect URIs réels (`/user`, `/ingest-contract`, `/customer`, etc.).
- Configs Angular des 8 apps mises à jour (issuer `https://dev.vitamui.com:9443` directement dans `config-dev.json`).
- CORS élargi via `setAllowedOriginPatterns` sur `https://dev.vitamui.com:*` + `http://localhost:*`.

### 5. Fix cascade sur le JWT id_token

- `OAuth2TokenCustomizer<JwtEncodingContext>` : force `sub = user.id`, ajoute `email`, ajoute `surrogation=true` sur les tokens subrogés. Résout le bug où `Authentication.getName()` fallback vers `UserDto.toString()` (Lombok, avec `AnalyticsDto@1bd121ef` non stable).
- Nouveau `VitamuiPrincipal implements AuthenticatedPrincipal` : wrappe le `UserDto`, expose `getName() = user.id` (stable inter-instances). Utilisé pour l'auth normale et la subrogation.

### 6. Revoke pour public client

- `PublicClientRevocationAuthenticationConverter` + `PublicClientRevocationAuthenticationProvider` : autorisent `POST /oauth2/revoke` avec juste `client_id` (RFC 7009 permet ça pour public clients, SAS le refuse par défaut).

### 7. Provider de logout OIDC tolérant

- Nouveau `SubrogationTolerantOidcLogoutAuthenticationProvider` : remplace le provider par défaut de SAS sur `/connect/logout`, **saute le check `sub` strict** légitimement mis en défaut par la transition de subrogation.
- Mode fallback : si l'`OAuth2Authorization` associée au id_token n'est plus dans le store in-memory (après restart SAS, tokens cachés côté portal), décode le JWT via `JwtDecoder` (signature RSA + expiration via JWKS), résout le `RegisteredClient` via `aud`, laisse passer.
- Validator `TolerantPostLogoutRedirectUriValidator` : accepte les URIs enregistrées avec query params extra (pattern angular-oauth2-oidc appendant `?isSubrogation=true&…`).

## Points durs rencontrés

### 1. Cookie de session partagé entre origines même-host (bloqueur SSO)

**Symptôme** : après login sur portal (:4200), ouverture d'identity (:4201) → redirect vers `/login` de SAS au lieu d'un SSO propre. Le browser envoyait pourtant bien un `JSESSIONID`.

**Cause** : les cookies HTTP **n'incluent pas le port** dans leur scope (RFC 6265). Le `JSESSIONID` émis par le `ng serve` du portal (:4200) écrasait celui du SAS (:9443) côté navigateur puisque même hostname `dev.vitamui.com` et même nom de cookie. La session SAS était alors introuvable côté serveur — les logs TRACE `HttpSessionSecurityContextRepository` montraient une session ID existante mais sans attribut `SPRING_SECURITY_CONTEXT`.

**Fix** : renommer le cookie de session SAS en `SAS_JSESSIONID`.

**Leçon** : quand plusieurs apps servlet cohabitent sur le même hostname en dev (ou en prod derrière un reverse-proxy sans path-based scoping), **chaque service doit avoir un nom de cookie de session distinct**.

### 2. Principal name non stable dans les authorizations OAuth2

**Symptôme** : sur `/connect/logout` post-subrogation, 400 `INVALID_TOKEN sub` du provider par défaut de SAS. Puis, avant le fix, un `id_token` OIDC avec un `sub` géant contenant le dump complet de `UserDto.toString()` (`"UserDto(super=CustomerIdDto(...), analytics=AnalyticsDto@1bd121ef, ...)"`).

**Cause** : `UsernamePasswordAuthenticationToken.getName()` fallback vers `principal.toString()` quand le principal n'implémente ni `UserDetails` ni `AuthenticatedPrincipal`. Le `UserDto` Lombok inclut des références mémoire de sous-objets qui varient entre sérialisation/désérialisation de la `HttpSession`.

**Fix** : wrapper `VitamuiPrincipal implements AuthenticatedPrincipal` avec `getName() = user.id`.

**Leçon** : Spring Security 7 + SAS 7 comparent des `Authentication.getName()` à plusieurs endroits (sub check, session registry, back-channel logout). Un `getName()` non stable propage silencieusement des bugs de validation. Tout principal métier doit implémenter `AuthenticatedPrincipal` ou `UserDetails`.

### 3. Ordre des `AuthenticationProvider` sur le logout endpoint

**Symptôme** : mon `SubrogationTolerantOidcLogoutAuthenticationProvider` ajouté via `logout.authenticationProvider(...)` n'était jamais appelé — c'était toujours le provider par défaut qui traitait et rejetait.

**Cause** : le comportement documenté (« provider custom en tête ») dépend d'un ordre d'enregistrement dans le `AuthenticationManager` global qui n'est pas garanti selon la version.

**Fix** : passer par `logout.authenticationProviders(providers -> …)` pour **retirer explicitement** le default `OidcLogoutAuthenticationProvider` de la liste et y ajouter le mien en tête.

**Leçon** : pour surcharger un provider SAS dont on ne veut pas cohabiter, préférer l'API `authenticationProviders(Consumer<List<...>>)` qui donne un contrôle exact sur la liste finale.

### 4. Storage in-memory des `OAuth2Authorization` volatile

**Symptôme** : après restart auth-server, tous les logouts en cours échouent car `authorizationService.findByToken(idTokenHint, ID_TOKEN_TOKEN_TYPE)` retourne `null` — les tokens émis avant le restart ne sont plus indexés.

**Cause** : `InMemoryOAuth2AuthorizationService` est vidé à chaque restart. Les frontends conservent leurs `id_token` en `localStorage` et les présentent au logout.

**Fix Phase 2** : mode fallback dans le provider tolérant — décode le JWT localement via `JwtDecoder` + résout le client via le claim `aud`.

**Phase 3** : passer à un `OAuth2AuthorizationService` persistant (Mongo custom ou JDBC).

### 5. Injection tardive du `JwtDecoder` dans les lambdas configurers

**Symptôme** : `http.getSharedObject(JwtDecoder.class)` retourne `null` dans une lambda de config exécutée avant que SAS ne pose son shared object.

**Fix** : injecter le bean `JwtDecoder` directement en paramètre de la méthode `@Bean` de la chain, le capturer dans le lambda.

**Leçon** : `getSharedObject` n'est pas fiable pour tout ce que SAS pose dynamiquement. Préférer l'injection Spring standard chaque fois que possible.

### 6. Renouvellement automatique de subrogation par le portal

**Symptôme** : dès la mire subrogation validée, le portail exécutait immédiatement `revokeTokenAndLogout` puis affichait la mire de login — sans que la subrogation ait le temps de s'établir.

**Cause** : `SubrogationBannerComponent.ngOnInit` (`vitamui-library`) calcule un timeout `endDate - now` puis appelle `logoutAndRedirectToUiForUser` en fin de vie. Une vieille `Subrogation` ACCEPTED de la veille traînait en base Mongo (le TTL index `expireAfterSeconds:0` n'avait pas encore été déclenché ou était mal appliqué), donc `subrogationTTL` était **négatif** → `setTimeout(fn, -X)` s'exécute immédiatement.

**Fix** : nettoyage Mongo (`db.subrogations.deleteMany({date:{$lt:new Date()}})`) et check server-side dans `CasService.validateSubrogation` pour rejeter les subro périmées.

**Leçon** : ne pas faire confiance au TTL Mongo pour l'invariant applicatif — toujours valider la date côté serveur en plus.

### 7. `angular-oauth2-oidc` n'URL-encode pas le `post_logout_redirect_uri`

**Symptôme** : quand le portal appelle `/connect/logout` avec un `postLogoutRedirectUri` contenant lui-même une query string (`?isSubrogation=true&superUserEmail=…`), les params ne sont pas encodés → SAS parse `post_logout_redirect_uri` jusqu'au premier `&`, les params suivants deviennent des params de `/connect/logout`.

**Fix** : `TolerantPostLogoutRedirectUriValidator` — compare `scheme + host + port + path` uniquement (ignore query/fragment).

**Leçon** : les patterns cross-window OIDC (subrogation via `postLogoutRedirectUri` enrichi) nécessitent une validation tolérante côté AS.

## Chantiers restants (Phase 2+)

Classés par valeur/urgence :

| # | Chantier | Estim. | Note |
|---|---|---|---|
| 1 | Fédération OIDC/SAML externe | 3-4 sem | Bloquant sortie prod. `spring-security-oauth2-client` + `spring-security-saml2-service-provider` avec repositories Mongo dynamiques (mapping `IdentityProviderDto` → `ClientRegistration` / `RelyingPartyRegistration`) — le plus incertain. |
| 2 | Hardening SAS ↔ IAM | 1 sem | Remettre `@Secured` sur les 3 endpoints IAM (`/cas/login`, `/cas/tokens`, `/cas/hrd`, `/cas/subrogations/validate`) avec mTLS OU header HMAC signé. Indispensable avant staging. |
| 3 | `RegisteredClientRepository` Mongo | 1 sem | Sans ça, chaque ajout de client OIDC = redéploiement SAS. |
| 4 | Consolidation logout OIDC | 1-2 sem | Robustifier le provider tolérant (tests), gérer proprement le back-channel logout pour propager fin de session multi-clients. |
| 5 | Persistance `OAuth2AuthorizationService` | 1 sem | Éviter la perte du storage à chaque restart (voir point dur #4). Mongo ou JDBC. |
| 6 | Password management | 1-2 sem | Reset (email) + change + policy. |
| 7 | Vraie SPA Angular `auth-ui` | 2 sem | Remplacer la SPA vanilla HTML/JS actuelle par un projet Angular dans le workspace. |

## État de qualité connu

Ce qui fonctionne pour un dev/POC mais devra être durci avant staging :

- Les endpoints IAM `/iam/v1/cas/{login,tokens,hrd,subrogations/validate}` sont **whitelisted sans authent** (bypass `@Secured` + `getAuthList()`) — voir chantier #2.
- Le `SubrogationTolerantOidcLogoutAuthenticationProvider` **saute le check `sub`** — c'est légitime dans la transition de subrogation mais élargit la surface d'attaque en cas d'id_token volé. Documenter le compromis + audit sécu.
- Le storage `InMemoryOAuth2AuthorizationService` est **volatile** — voir chantier #5.
- Les logs `TRACE` sur les packages Spring Security sont **encore actifs** dans `application.yml` — à baisser à `WARN` avant staging.
