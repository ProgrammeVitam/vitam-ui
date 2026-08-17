# Reverse proxy de développement — origine unique

Toutes les applications VitamUI sont servies sous **une seule origine, sans port** :

```
https://dev.vitamui.com/                                    portal
https://dev.vitamui.com/identity/...                        identity
https://dev.vitamui.com/referential/...                     referential
https://dev.vitamui.com/ingest/...                          ingest
https://dev.vitamui.com/archive-search/...                  archive-search
https://dev.vitamui.com/collect/...                         collect
https://dev.vitamui.com/pastis/...                          pastis
https://dev.vitamui.com/design-system/...                   design-system

https://dev.vitamui.com/<app>-api/...                       api-gateway (mTLS)
https://dev.vitamui.com/cas/...                             cas-server
```

C'est la forme d'URL des environnements déployés. Par exemple

```
https://itrec-ui.env.programmevitam.fr/archive-search/archive-search/tenant/1
                                      └─ préfixe reverse ┘└─ route Angular ─┘
```

devient en local

```
https://dev.vitamui.com/archive-search/archive-search/tenant/1
```

## Pourquoi

Avant, chaque front écoutait sur son propre port (`:4200`, `:4201`, … `:4251`). Une
application par origine, donc, pour CAS, un `serviceId` par port, une entrée CORS par port,
un `redirectUri` OIDC par port — et autant d'endroits à corriger dès qu'on touchait à un
front. La configuration de dev divergeait en outre de celle de prod, qui a toujours été en
origine unique.

Ce nginx fusionne les deux couches nginx de la prod dans un seul conteneur :

| Couche prod | Rôle | Équivalent ici |
|---|---|---|
| `deployment/roles/reverse` | porte d'entrée 443, routage par chemin | les `location` de `conf/templates/vitamui.conf.template` |
| `deployment/roles/nginx_webapp` | service des `dist/`, proxy `/<app>-api` en mTLS | les mêmes `location`, blocs 1 et 3 à 5 |

### Les deux formes d'appel d'API

Une application peut appeler son API de deux façons, et les deux doivent fonctionner :

```
https://dev.vitamui.com/archive-search-api/security                  forme absolue
https://dev.vitamui.com/archive-search/archive-search-api/security   forme relative
```

La seconde n'est pas une anomalie. Seul le portail déclare une `BASE_URL` absolue
(`'/portal-api'`) — il est servi à la racine. **Toutes les autres applications la déclarent
relative** :

```ts
// projects/archive-search/src/app/core/core.module.ts
{ provide: BASE_URL, useValue: './archive-search-api' }
```

C'est délibéré : le front doit fonctionner sous n'importe quel préfixe de déploiement. Servi
sous `<base href="/archive-search/">`, `./archive-search-api` devient donc
`/archive-search/archive-search-api` — le préfixe apparaît deux fois. La production retire
le premier segment dans `roles/reverse` (`rewrite /archive-search/(.*) /$1 break;`) ; ce
proxy fait de même.

Sans ce retrait, l'appel retombe sur le service des applications, qui répond `index.html` :
le front reçoit du **HTML en 200** là où il attend du JSON, et signale
`HttpErrorResponse { status: 200, ok: false }` — un symptôme qui n'évoque pas sa cause.

## Prérequis

1. **`dev.vitamui.com` résout vers la boucle locale** — dans `/etc/hosts` :

   ```
   127.0.0.1    dev.vitamui.com
   ```

2. **Les certificats de dev sont générés**, sous
   `dev-deployment/environments/certs/vitamui-services/` :
   - `servers/reverse/` pour la terminaison TLS (CN `dev.vitamui.com`, SAN `localhost`) ;
   - `clients/ui-*/` pour s'authentifier auprès de l'api-gateway.

3. **Les fronts sont disponibles**, de l'une des deux façons suivantes.

   **Sans rien construire** — c'est le plus simple, et on garde le rechargement à chaud :

   ```bash
   ./vitamui-nginx.sh ng-serve portal
   cd ../../../ui/ui-frontend && npm run start:portal
   ```

   **Ou en construisant les `dist/`**, en configuration `development` :

   ```bash
   cd ui/ui-frontend
   for app in portal identity referential ingest archive-search collect pastis design-system; do
     npx ng build "$app" --configuration development
   done
   ```

   > **Pas `npm run build:allModules`.** Ces scripts construisent en configuration
   > `production`, qui exclut délibérément `config-dev.json` de ses assets
   > (`"ignore": ["config-dev.json"]` dans `angular.json`) : en production c'est ansible
   > qui dépose un `config.json` généré. Un build production servi en local donne donc une
   > application qui s'affiche mais ne trouve aucune configuration — ni URL des autres
   > applications, ni paramètres OIDC.

   Les deux dispositions de `dist/` sont acceptées : celle d'Angular ≥ 17
   (`dist/<app>/browser/…`, ce que produisent les commandes ci-dessus) et l'arborescence
   plate que sert la production.

4. **Un démon docker joignable.** Si le CLI pointe sur un contexte éteint (typiquement
   `desktop-linux` quand Docker Desktop n'est pas lancé), les scripts basculent seuls sur
   `default`. Pour rendre le choix permanent : `docker context use default`.

## Utilisation

```bash
cd tools/docker/nginx

./vitamui-nginx.sh up        # vérifie les prérequis puis démarre
./vitamui-nginx.sh status    # état, sonde, table des URLs
./vitamui-nginx.sh logs -f
./vitamui-nginx.sh reload    # recharge sans coupure
./vitamui-nginx.sh check     # valide la configuration rendue (nginx -t)
./vitamui-nginx.sh down
```

Pour adapter à son poste : copier `.env.example` en `.env` (ignoré par git).

## Travailler sur un front avec `ng serve`

Par défaut nginx sert les `dist/`. Pour repasser une application sur son serveur de
développement, avec rechargement à chaud :

```bash
./vitamui-nginx.sh ng-serve archive-search      # écrit ng-serve/archive-search.conf, recharge
cd ../../../ui/ui-frontend && npm run start:archive-search
```

L'URL ne change pas : `https://dev.vitamui.com/archive-search/`. Les autres applications
continuent d'être servies depuis leur `dist/`, et `/archive-search-api` reste routé vers
l'api-gateway.

Pour revenir au `dist/` :

```bash
./vitamui-nginx.sh ng-serve --clear archive-search   # ou --clear seul, pour tout retirer
```

Cela repose sur la clé `servePath` de `ui/ui-frontend/angular.json`, qui fait servir le
serveur de dev Angular sous `/<app>/` — le même préfixe que le reverse proxy.

## Tests

```bash
./tests/run-tests.sh
```

La suite démarre une pile isolée — projet compose et ports dédiés (`14443`, `14080`), donc
sans interférence avec une instance de dev en cours — branchée sur des doublures
d'api-gateway, de cas-server et de serveur de dev Angular. Elle couvre :

| Groupe | Ce qui est vérifié |
|---|---|
| `CONFIGURATION` | `nginx -t` sur la configuration effectivement rendue, sonde locale |
| `ROUTAGE` | les 8 applications répondent depuis leur propre `dist/` |
| `ROUTE-PROFONDE` | `/archive-search/archive-search/tenant/1` retombe sur le bon `index.html` |
| `ASSET` | assets servis, 404 sur asset absent, aucun repli croisé entre applications |
| `API` | les 7 préfixes `-api` atteignent la gateway **avec le bon certificat client**, validé en mTLS |
| `CAS` | `/cas/*` atteint cas-server, context-path conservé, assets CAS non capturés |
| `HTTP` | redirection 301 vers HTTPS en conservant le chemin |
| `NG-SERVE` | la surcharge route vers le serveur de dev, n'affecte que l'application visée, et se retire proprement |

Options :

```bash
./tests/run-tests.sh -k API     # ne joue que les tests dont le nom contient API
KEEP_UP=1 ./tests/run-tests.sh  # laisse les conteneurs debout pour inspection
```

## Ce que ce changement implique ailleurs

Le reverse proxy seul ne suffit pas : les URL sont écrites en dur à plusieurs endroits, tous
alignés sur l'origine unique.

| Fichier | Ce qui change |
|---|---|
| `ui/ui-frontend/projects/*/src/assets/config-dev.json` | `UI_URL`, `PORTAL_URL`, `*_URL`, et `OIDC_CONFIG` (`issuer`, `redirectUri`, `postLogoutRedirectUri`) |
| `ui/ui-frontend/angular.json` | `servePath` par application, pour `ng serve` derrière le proxy |
| `tools/docker/mongo/mongo_vars_dev.yml` | `url_prefix`, puis `base_url` et `serviceId` de chaque application — d'où découlent les URL de `iam.applications` et les services CAS |
| `cas/cas-server/src/main/config/application-dev.yml` | `cas.server.prefix`, `login-url` pac4j, `vitamui.portal.url` |

Après modification des variables mongo, il faut rejouer les scripts d'initialisation :

```bash
cd tools/docker/mongo && ./restart_dev.sh
```

## Pourquoi il y a deux nginx dans `tools/docker/`

`tools/docker/` contient deux répertoires nginx. Ils ne font pas la même chose, et **ils ne
peuvent pas tourner en même temps** : tous deux écoutent sur le port 443 en
`network_mode: host`. Le second à démarrer échoue.

| | `tools/docker/nginx` | `tools/docker/nginx-cas-x509` |
|---|---|---|
| À quoi ça sert | travailler, tous les jours | tester **une** fonctionnalité : l'authentification x509 de CAS |
| Ce qui est servi | les 8 fronts, les APIs, CAS | CAS, et rien d'autre |
| Certificat client du navigateur | aucun | **exigé à chaque requête** (`ssl_verify_client on`) |
| PKI utilisée | `dev-deployment/environments/certs/` | la sienne, `nginx-cas-x509/pki/generate_pki.sh` |
| Configuration CAS | telle quelle | demande de renseigner `cas.authn.x509.*`, commenté par défaut |

### Pourquoi ne pas les fusionner

Ce serait techniquement faisable — un `ssl_verify_client optional` sur le vhost principal, et
le transfert de `$ssl_client_escaped_cert` vers CAS. On ne le fait pas parce que **dès que
nginx réclame un certificat client, le navigateur ouvre une fenêtre de sélection de
certificat à chaque visite**, y compris pour un simple rechargement du portail. C'est
inacceptable pour un usage quotidien, alors que l'authentification x509 ne se teste que
ponctuellement.

L'indépendance des PKI est également volontaire : celle de `nginx-cas-x509` simule un
porteur externe — une carte à puce, un certificat d'agent — qui n'a par construction rien à
voir avec les certificats de service de VitamUI. Les mélanger rendrait le test moins fidèle.

### Basculer de l'un à l'autre

```bash
# vers le banc x509
cd tools/docker/nginx        && ./vitamui-nginx.sh down
cd ../nginx-cas-x509         && docker compose up -d
# puis décommenter cas.authn.x509.* dans cas/cas-server/src/main/config/application-dev.yml
# et importer client/client.p12 dans le navigateur (mot de passe : azerty)

# retour au fonctionnement normal
cd tools/docker/nginx-cas-x509 && docker compose down
cd ../nginx                    && ./vitamui-nginx.sh up
```

La marche à suivre complète pour le x509 est dans
[`cas/cas-server/README.md`](../../../cas/cas-server/README.md), section
« Certificate authentication ».

## Dépannage

**`502 Bad Gateway` sur `/<app>-api/`** — l'api-gateway n'écoute pas sur `127.0.0.1:8070`.
Vérifier avec `ss -ltn | grep 8070`, ou pointer ailleurs via `VITAMUI_GATEWAY` dans `.env`.

**`500` sur `/<app>-api/`** — regarder `./vitamui-nginx.sh logs`. Un
`cannot load certificate key ... bad decrypt` signifie que la passphrase de
`conf/proxy_ssl_password` ne correspond plus aux clés de `dev-deployment` (elle vaut
`changeme` partout par défaut).

**`400` renvoyé par la gateway** — elle a refusé le certificat client. Le certificat présenté
dépend du préfixe d'URL, via la table de `conf/snippets/maps.conf` ; il doit être enregistré
dans la collection `security.certificates`.

**L'application s'affiche mais reste vide, ou renvoie vers de mauvaises URL** — elle n'a
pas trouvé sa configuration. Vérifier que `https://dev.vitamui.com/<app>/assets/config-dev.json`
répond 200 ; s'il est en 404, le `dist/` a été construit en configuration `production`, qui
exclut ce fichier. Reconstruire avec `--configuration development` (cf. prérequis 3).

**404 sur les assets d'une application** — le `dist/` a été construit sans base href, ses
assets sont donc référencés depuis la racine. Les commandes du prérequis 3 posent le bon
`<base href="/<app>/">`, via la clé `baseHref` de la configuration `development` dans
`angular.json`.

**nginx ne démarre pas, port 443 occupé** — `ss -ltnp | grep :443`. Sinon, basculer sur un
port non privilégié via `VITAMUI_HTTPS_PORT` et `VITAMUI_PUBLIC_ORIGIN` dans `.env`.

## Arborescence

```
tools/docker/nginx/
├── docker-compose.yml
├── .env.example
├── vitamui-nginx.sh              pilote (up/down/status/check/ng-serve)
├── conf/
│   ├── nginx.conf                configuration globale
│   ├── proxy_ssl_password        passphrase des clés clientes (changeme)
│   ├── disabled-default.conf     neutralise le vhost par défaut de l'image
│   ├── templates/
│   │   └── vitamui.conf.template le vhost, rendu au démarrage par envsubst
│   └── snippets/
│       ├── maps.conf             préfixe d'API -> certificat client
│       ├── ssl.conf              terminaison TLS
│       ├── proxy_params.conf     en-têtes transmis (repris de la prod)
│       ├── spa.conf              en-têtes des applications Angular
│       └── upload.conf           réglages pour les gros versements
├── ng-serve/                     surcharges ng serve, vide par défaut
└── tests/
    ├── run-tests.sh
    ├── docker-compose.test.yml
    └── stubs/upstreams.conf      doublures gateway / cas / ng serve
```
