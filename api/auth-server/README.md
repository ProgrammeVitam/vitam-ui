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

### 1. Démarrer IAM (port `8083` par défaut vitam-ui)

```bash
mvn -pl api/api-iam/iam spring-boot:run
```

Sanity check :

```bash
curl "http://localhost:8083/iam/v1/cas/hrd?email=admin@vitamui.com"
```

Override si nécessaire : `IAM_BASE_URL=http://localhost:<port>` sur le lancement d'auth-server.

### 2. Démarrer auth-server (port `9443` HTTPS)

```bash
mvn -pl api/auth-server spring-boot:run
```

SSL est **activé par défaut** avec le keystore self-signed livré
(`src/main/resources/certs/dev-vitamui.p12`, CN=dev.vitamui.com, SAN=DNS:dev.vitamui.com,
DNS:localhost, IP:127.0.0.1). Il faut que ton `/etc/hosts` mappe `dev.vitamui.com` sur `127.0.0.1`
(habitude vitam-ui).

**Accepte le cert self-signed dans le navigateur** avant de tester le portal — visite manuellement
<https://dev.vitamui.com:9443/.well-known/openid-configuration> une fois, sinon
`angular-oauth2-oidc` retournera `status: 0` sans jamais atteindre SAS.

Overrides possibles :

- `SSL_ENABLED=false` pour tester en HTTP pur.
- `AUTH_SERVER_ISSUER=…` si tu changes le hostname/port (l'issuer doit correspondre exactement à
  l'URL utilisée par le navigateur).
- `SSL_KEYSTORE_PASSWORD=…` si tu régénères le keystore avec un autre mot de passe.

Pour régénérer le keystore :

```bash
keytool -genkeypair -alias dev-vitamui -keyalg RSA -keysize 2048 -validity 365 \
  -dname "CN=dev.vitamui.com,OU=vitamui-dev,O=Vitam,C=FR" \
  -ext "SAN=DNS:dev.vitamui.com,DNS:localhost,IP:127.0.0.1" \
  -keystore api/auth-server/src/main/resources/certs/dev-vitamui.p12 \
  -storetype PKCS12 -storepass changeit -keypass changeit
```

Sanity check :

```bash
curl -sk https://dev.vitamui.com:9443/.well-known/openid-configuration | jq
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
