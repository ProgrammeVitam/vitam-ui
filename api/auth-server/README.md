# auth-server — POC Spring Authorization Server

Phase 1 du remplacement d'Apereo CAS par Spring Authorization Server (SAS). Le but est de valider
qu'un SAS peut émettre un `access_token` opaque au format `TOK-<UUID>` persisté dans la collection
Mongo `tokens` existante — laissant les 8 frontends Angular et les Resource Servers inchangés.

## Ce que fait ce module en Phase 1

- Expose un endpoint OIDC discovery (`/.well-known/openid-configuration`), `/oauth2/authorize`,
  `/oauth2/token`, `/oauth2/jwks`, `/userinfo`, `/connect/logout`.
- Enregistre un unique client OIDC en mémoire (`portal`, PKCE public, redirect vers
  `https://dev.vitamui.com:4200/`).
- Sert une SPA minimale de login sur `/login` (email → mini HRD via IAM → password).
- Délègue l'authentification email/password à IAM via l'endpoint existant `POST /iam/v1/cas/login`.
- Crée un token opaque `TOK-<UUID>` en Mongo via le nouvel endpoint IAM `POST /iam/v1/cas/tokens`
  et le retourne comme `access_token`.
- L'`id_token` OIDC reste un JWT RSA signé (nécessaire pour Discovery + `scope=openid`).

## Périmètre hors scope Phase 1

- Multi-customer HRD (N>1 organisations pour un email).
- IdP externes OIDC/SAML (`internal=false`).
- Subrogation.
- Password management (reset/change).
- Logout end-session avancé.
- Migration des 7 autres clients OIDC.
- Portage de la SPA login vers un vrai projet Angular sous `ui/ui-frontend/projects/auth-ui/`
  (Phase 1 se contente d'une SPA vanilla HTML/JS sous `src/main/resources/static/login/`).

## Comment lancer le POC en local

### 1. Démarrer IAM (port `9000` par défaut)

```bash
mvn -pl api/api-iam/iam spring-boot:run
```

Sanity check :

```bash
curl "http://localhost:9000/iam/v1/cas/hrd?email=admin@vitamui.com"
```

### 2. Démarrer auth-server (port `9443`)

```bash
mvn -pl api/auth-server spring-boot:run
```

Sanity check :

```bash
curl -s http://localhost:9443/.well-known/openid-configuration | jq
```

### 3. Basculer la config portal sur SAS

```bash
cp ui/ui-frontend/projects/portal/src/assets/config-dev-sas.json \
   ui/ui-frontend/projects/portal/src/assets/config-dev.json
npm --prefix ui/ui-frontend start portal
```

Ouvrir <https://dev.vitamui.com:4200/> — portal doit rediriger vers <http://localhost:9443/login>,
présenter le formulaire email, puis password, puis revenir sur portal avec une session valide.

### 4. Vérifier en base

```bash
mongosh
> use cas
> db.tokens.find({}).sort({createdDate: -1}).limit(1)
# doit contenir { _id: "TOK-<UUID>", refId: "<userId>", surrogation: false, ... }
```

## Sécurité inter-services SAS ↔ IAM

Phase 1 ne signe pas les appels de `auth-server` vers IAM sur les deux nouveaux endpoints
(`/iam/v1/cas/tokens`, `/iam/v1/cas/hrd`). Ces endpoints ne portent pas `@Secured` pour permettre au
POC de tourner sans wiring d'auth interne. En Phase 2, sécuriser via mTLS ou signature HMAC de
header inter-services (même modèle qu'un token de service).
