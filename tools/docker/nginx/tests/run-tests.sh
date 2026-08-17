#!/usr/bin/env bash
#
# Suite de tests du reverse proxy de developpement VitamUI.
#
# Elle demarre une instance nginx isolee (projet compose et ports dedies, donc
# sans interference avec le proxy de dev eventuellement en cours), branchee sur
# des doublures d'api-gateway, de cas-server et de serveur de dev Angular, puis
# verifie le routage de bout en bout pour les huit applications.
#
#   ./run-tests.sh            joue toute la suite
#   ./run-tests.sh -k ROUTAGE ne joue que les tests dont le nom contient ROUTAGE
#   KEEP_UP=1 ./run-tests.sh  laisse les conteneurs debout pour inspection
#
# Invoque via `sh run-tests.sh`, l'interpreteur est dash sur Debian et Ubuntu :
# il ne connait ni `set -o pipefail` ni les tableaux associatifs. On se relance
# donc sous bash. Ce test doit rester en syntaxe POSIX, c'est encore dash qui le lit.
if [ -z "${BASH_VERSION:-}" ]; then
  exec bash "$0" "$@"
fi

set -uo pipefail

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "$HERE/.." && pwd)"
cd "$HERE"

# ---------------------------------------------------------------------------
# Parametres — ports decales pour cohabiter avec une instance de dev.
# ---------------------------------------------------------------------------
export TEST_WORK_DIR="$HERE/.work"
export VITAMUI_CERTS_DIR="$(cd "$ROOT/../../../dev-deployment/environments/certs/vitamui-services" && pwd)"

SERVER_NAME="${SERVER_NAME:-dev.vitamui.com}"
HTTPS_PORT="${HTTPS_PORT:-14443}"
HTTP_PORT="${HTTP_PORT:-14080}"
export STUB_GATEWAY_PORT="${STUB_GATEWAY_PORT:-18070}"
export STUB_CAS_PORT="${STUB_CAS_PORT:-18080}"
export STUB_NG_SERVE_PORT="${STUB_NG_SERVE_PORT:-14209}"

ORIGIN="https://$SERVER_NAME:$HTTPS_PORT"
PROJECT_MAIN=vitamui-nginx-test
PROJECT_STUB=vitamui-nginx-test-upstreams

## Nom de conteneur distinct de celui de l'instance de dev : docker impose des
## noms uniques sur toute la machine, meme entre projets compose differents.
export VITAMUI_CONTAINER_NAME=vitamui-nginx-under-test

APPS=(portal identity referential ingest archive-search collect pastis design-system)
API_APPS=(portal identity referential ingest archive-search collect pastis)

## Certificat client attendu pour chaque prefixe d'API (cf. conf/snippets/maps.conf).
declare -A EXPECTED_CN=(
  [portal]=ui-portal
  [identity]=ui-identity-admin
  [referential]=ui-referential
  [ingest]=ui-ingest
  [archive-search]=ui-archive-search
  [collect]=ui-collect
  [pastis]=ui-pastis
)

FILTER="${FILTER:-}"
[[ "${1:-}" == "-k" ]] && FILTER="${2:-}"

# ---------------------------------------------------------------------------
# Sortie
# ---------------------------------------------------------------------------
RED=$'\033[31m'; GREEN=$'\033[32m'; DIM=$'\033[2m'; BOLD=$'\033[1m'; OFF=$'\033[0m'
PASSED=0; FAILED=0; SKIPPED=0
FAILURES=()

section() { printf '\n%s%s%s\n' "$BOLD" "$*" "$OFF"; }

check() {
  ## check <intitule> <attendu> <obtenu>
  local name="$1" want="$2" got="$3"

  if [[ -n "$FILTER" && "$name" != *"$FILTER"* ]]; then
    SKIPPED=$((SKIPPED + 1)); return 0
  fi

  if [[ "$got" == "$want" ]]; then
    printf '  %s✔%s %s\n' "$GREEN" "$OFF" "$name"
    PASSED=$((PASSED + 1))
  else
    printf '  %s✘%s %s\n' "$RED" "$OFF" "$name"
    printf '      %sattendu :%s %s\n' "$DIM" "$OFF" "$want"
    printf '      %sobtenu  :%s %s\n' "$DIM" "$OFF" "$got"
    FAILED=$((FAILED + 1))
    FAILURES+=("$name")
  fi
}

check_contains() {
  ## check_contains <intitule> <fragment attendu> <texte>
  local name="$1" needle="$2" haystack="$3"

  if [[ -n "$FILTER" && "$name" != *"$FILTER"* ]]; then
    SKIPPED=$((SKIPPED + 1)); return 0
  fi

  if [[ "$haystack" == *"$needle"* ]]; then
    printf '  %s✔%s %s\n' "$GREEN" "$OFF" "$name"
    PASSED=$((PASSED + 1))
  else
    printf '  %s✘%s %s\n' "$RED" "$OFF" "$name"
    printf '      %scontient :%s %s\n' "$DIM" "$OFF" "$needle"
    printf '      %sobtenu   :%s %s\n' "$DIM" "$OFF" "${haystack:0:400}"
    FAILED=$((FAILED + 1))
    FAILURES+=("$name")
  fi
}

# ---------------------------------------------------------------------------
# Requetes
# ---------------------------------------------------------------------------
## Les certificats de dev sont auto-signes : -k est indispensable cote client.
get_status() { curl -k -s -o /dev/null -w '%{http_code}' --max-time 10 "$@"; }
get_body()   { curl -k -s --max-time 10 "$@"; }
get_header() { curl -k -s -o /dev/null -D - --max-time 10 "$@" | tr -d '\r'; }

# ---------------------------------------------------------------------------
# Mise en place
# ---------------------------------------------------------------------------
compose_main() {
  docker compose -p "$PROJECT_MAIN" -f "$ROOT/docker-compose.yml" --project-directory "$ROOT" "$@"
}
compose_stub() {
  docker compose -p "$PROJECT_STUB" -f "$HERE/docker-compose.test.yml" --project-directory "$HERE" "$@"
}

select_docker_context() {
  docker version >/dev/null 2>&1 && return 0
  if DOCKER_CONTEXT=default docker version >/dev/null 2>&1; then
    export DOCKER_CONTEXT=default
    printf '%s(contexte docker bascule sur « default »)%s\n' "$DIM" "$OFF"
    return 0
  fi
  printf '%saucun demon docker joignable%s\n' "$RED" "$OFF" >&2
  exit 1
}

build_fixtures() {
  ## dist/ factices : un index.html porteur d'un marqueur par application, plus
  ## les assets qu'un vrai build produit, pour tester le routage sans avoir a
  ## lancer huit builds Angular.
  rm -rf "$TEST_WORK_DIR"
  mkdir -p "$TEST_WORK_DIR/ca"

  cat "$VITAMUI_CERTS_DIR/ca/ca-root.crt" \
      "$VITAMUI_CERTS_DIR/ca/ca-intermediate.crt" > "$TEST_WORK_DIR/ca/ca-chain.crt"

  ## `collect` est pose en disposition Angular >= 17 (dist/<app>/browser/...),
  ## les autres a plat comme le sert la prod. Les memes assertions doivent passer
  ## sur les deux : c'est tout l'interet du repli browser/ dans try_files.
  local nested=collect

  for app in "${APPS[@]}"; do
    local base="$TEST_WORK_DIR/dist/$app"
    [[ "$app" == "$nested" ]] && base="$TEST_WORK_DIR/dist/$app/browser"

    mkdir -p "$base/assets"
    printf '<!doctype html><title>%s</title><body>VITAMUI-DIST-MARKER:%s</body>\n' \
      "$app" "$app" > "$base/index.html"
    printf 'console.log("VITAMUI-ASSET-MARKER:%s");\n' "$app" > "$base/main.js"
    printf '{"MARKER":"%s"}\n' "$app" > "$base/assets/config-dev.json"

    ## Present dans le seul dist du portail : sert a verifier qu'aucune
    ## application ne se voit servir les assets d'une autre.
    if [[ "$app" == portal ]]; then
      printf 'console.log("VITAMUI-PORTAL-ONLY");\n' > "$base/portal-only.js"
    fi
  done

  ## Le portail est construit sans --base-href : ses assets sont references
  ## depuis la racine, pas depuis /portal/.
  export VITAMUI_DIST_DIR="$TEST_WORK_DIR/dist"
}

start_stack() {
  compose_stub up -d --quiet-pull >/dev/null

  VITAMUI_SERVER_NAME="$SERVER_NAME" \
  VITAMUI_PUBLIC_ORIGIN="$ORIGIN" \
  VITAMUI_HTTPS_PORT="$HTTPS_PORT" \
  VITAMUI_HTTP_PORT="$HTTP_PORT" \
  VITAMUI_GATEWAY="127.0.0.1:$STUB_GATEWAY_PORT" \
  VITAMUI_CAS="127.0.0.1:$STUB_CAS_PORT" \
  VITAMUI_DIST_DIR="$VITAMUI_DIST_DIR" \
  VITAMUI_CERTS_DIR="$VITAMUI_CERTS_DIR" \
    compose_main up -d --quiet-pull --force-recreate >/dev/null

  ## Attente active plutot qu'un sleep fixe.
  local i
  for i in $(seq 1 40); do
    [[ "$(get_status "$ORIGIN/__vitamui/health")" == 200 ]] && return 0
    sleep 0.5
  done

  printf '%sla pile de test n a pas demarre%s\n' "$RED" "$OFF" >&2
  compose_main logs --tail 40
  compose_stub logs --tail 20
  return 1
}

teardown() {
  local rc=$?
  if [[ "${KEEP_UP:-}" == 1 ]]; then
    printf '\n%sKEEP_UP=1 : conteneurs laisses debout (%s)%s\n' "$DIM" "$ORIGIN" "$OFF"
    return $rc
  fi
  VITAMUI_DIST_DIR="${VITAMUI_DIST_DIR:-$TEST_WORK_DIR/dist}" \
  VITAMUI_CERTS_DIR="$VITAMUI_CERTS_DIR" \
    compose_main down --remove-orphans >/dev/null 2>&1
  compose_stub down --remove-orphans >/dev/null 2>&1
  rm -rf "$TEST_WORK_DIR"
  return $rc
}

# ===========================================================================
# Tests
# ===========================================================================
test_configuration() {
  section "Configuration"

  local out
  out="$(VITAMUI_SERVER_NAME="$SERVER_NAME" VITAMUI_PUBLIC_ORIGIN="$ORIGIN" \
         VITAMUI_HTTPS_PORT="$HTTPS_PORT" VITAMUI_HTTP_PORT="$HTTP_PORT" \
         VITAMUI_GATEWAY="127.0.0.1:$STUB_GATEWAY_PORT" VITAMUI_CAS="127.0.0.1:$STUB_CAS_PORT" \
         VITAMUI_DIST_DIR="$VITAMUI_DIST_DIR" VITAMUI_CERTS_DIR="$VITAMUI_CERTS_DIR" \
         compose_main exec -T nginx nginx -t 2>&1)"
  check_contains "CONFIGURATION la syntaxe nginx est valide" "syntax is ok" "$out"
  check_contains "CONFIGURATION le test de configuration passe" "test is successful" "$out"

  check "CONFIGURATION la sonde locale repond" \
    '{"status":"UP","component":"vitamui-dev-reverse"}' \
    "$(get_body "$ORIGIN/__vitamui/health")"
}

test_routage_applications() {
  section "Routage des applications"

  check "ROUTAGE portal est servi a la racine" \
    200 "$(get_status "$ORIGIN/")"
  check_contains "ROUTAGE la racine sert bien le dist du portail" \
    "VITAMUI-DIST-MARKER:portal" "$(get_body "$ORIGIN/")"

  for app in "${APPS[@]}"; do
    [[ "$app" == portal ]] && continue
    check "ROUTAGE /$app/ repond 200" \
      200 "$(get_status "$ORIGIN/$app/")"
    check_contains "ROUTAGE /$app/ sert le dist de $app" \
      "VITAMUI-DIST-MARKER:$app" "$(get_body "$ORIGIN/$app/")"
  done

  ## Sans slash final, l'application est servie directement plutot que redirigee :
  ## le <base href="/archive-search/"> du build resout les assets correctement.
  check "ROUTAGE /archive-search sans slash final est servi" \
    200 "$(get_status "$ORIGIN/archive-search")"
  check_contains "ROUTAGE /archive-search sans slash sert le bon dist" \
    "VITAMUI-DIST-MARKER:archive-search" "$(get_body "$ORIGIN/archive-search")"
}

test_dist_absent() {
  section "Application non construite"

  ## Cas tres frequent : on demarre le proxy avant d'avoir lance
  ## `npm run build:allModules`. Sans le `=404` de try_files, nginx repond 500
  ## « rewrite or internal redirection cycle », qui n'evoque rien pour celui qui
  ## cherche pourquoi son application ne s'affiche pas.
  local saved="$TEST_WORK_DIR/design-system-index.html"
  mv "$TEST_WORK_DIR/dist/design-system/index.html" "$saved"

  check "DIST-ABSENT une application sans dist repond 404 et non 500" \
    404 "$(get_status "$ORIGIN/design-system/")"
  check "DIST-ABSENT une route profonde repond 404 elle aussi" \
    404 "$(get_status "$ORIGIN/design-system/une/route")"

  mv "$saved" "$TEST_WORK_DIR/dist/design-system/index.html"

  check "DIST-ABSENT le dist restaure est de nouveau servi" \
    200 "$(get_status "$ORIGIN/design-system/")"

  ## Le portail est servi depuis une autre racine : il a son propre try_files.
  local saved_portal="$TEST_WORK_DIR/portal-index.html"
  mv "$TEST_WORK_DIR/dist/portal/index.html" "$saved_portal"

  check "DIST-ABSENT le portail sans dist repond 404 et non 500" \
    404 "$(get_status "$ORIGIN/")"

  mv "$saved_portal" "$TEST_WORK_DIR/dist/portal/index.html"
}

test_routes_profondes() {
  section "Routes Angular profondes"

  ## La forme exacte des environnements deployes :
  ## https://<host>/archive-search/archive-search/tenant/1
  local url="$ORIGIN/archive-search/archive-search/tenant/1"
  check "ROUTE-PROFONDE archive-search/tenant/1 repond 200" \
    200 "$(get_status "$url")"
  check_contains "ROUTE-PROFONDE archive-search/tenant/1 retombe sur son index.html" \
    "VITAMUI-DIST-MARKER:archive-search" "$(get_body "$url")"

  check_contains "ROUTE-PROFONDE ingest sert son propre index.html" \
    "VITAMUI-DIST-MARKER:ingest" "$(get_body "$ORIGIN/ingest/ingest/tenant/2/holding")"

  check_contains "ROUTE-PROFONDE une route du portail reste sur le portail" \
    "VITAMUI-DIST-MARKER:portal" "$(get_body "$ORIGIN/tenant/1")"
}

test_assets() {
  section "Assets"

  ## collect est pose en dist/collect/browser/ : l'URL publique ne doit pas s'en
  ## apercevoir.
  check_contains "ASSET l asset d une application est servi depuis son dist" \
    "VITAMUI-ASSET-MARKER:collect" "$(get_body "$ORIGIN/collect/main.js")"
  check_contains "ASSET la disposition browser/ d Angular 17+ est servie a l identique" \
    "VITAMUI-DIST-MARKER:collect" "$(get_body "$ORIGIN/collect/une/route/profonde")"

  check_contains "ASSET l asset du portail est servi depuis la racine" \
    "VITAMUI-ASSET-MARKER:portal" "$(get_body "$ORIGIN/main.js")"

  check_contains "ASSET config-dev.json est servi" \
    '"MARKER":"pastis"' "$(get_body "$ORIGIN/pastis/assets/config-dev.json")"

  ## Sans la location dediee aux assets, nginx repondrait 200 + index.html, ce
  ## qui se manifeste par une erreur de parsing incomprehensible cote navigateur.
  check "ASSET un asset absent repond 404 et non index.html" \
    404 "$(get_status "$ORIGIN/archive-search/assets/inexistant.js")"

  ## Un front construit sans --deploy-url=/<app>/ doit echouer franchement,
  ## pas se voir servir les fichiers du portail.
  check "ASSET aucun repli croise vers le dist du portail" \
    404 "$(get_status "$ORIGIN/archive-search/portal-only.js")"
  check_contains "ASSET le fichier propre au portail reste servi a la racine" \
    "VITAMUI-PORTAL-ONLY" "$(get_body "$ORIGIN/portal-only.js")"
}

test_apis_mtls() {
  section "APIs et mTLS vers l api-gateway"

  for app in "${API_APPS[@]}"; do
    local body
    body="$(get_body "$ORIGIN/$app-api/v1/ping")"

    check_contains "API /$app-api atteint l api-gateway" \
      '"stub":"api-gateway"' "$body"
    check_contains "API /$app-api presente le certificat ${EXPECTED_CN[$app]}" \
      "CN=${EXPECTED_CN[$app]}" "$body"
    check_contains "API /$app-api est validee par la gateway" \
      '"verify":"SUCCESS"' "$body"
    check_contains "API /$app-api transmet l URI telle quelle" \
      "\"uri\":\"/$app-api/v1/ping\"" "$body"
  done

  ## Le prefixe d API doit primer sur celui de l application, sinon
  ## /archive-search-api serait servi comme un fichier statique.
  check_contains "API le prefixe -api prime sur le prefixe applicatif" \
    '"stub":"api-gateway"' "$(get_body "$ORIGIN/archive-search-api/foo.json")"
}

test_apis_relatives() {
  section "APIs appelees relativement au base href"

  ## Toutes les applications sauf le portail declarent une BASE_URL relative
  ## (`useValue: './archive-search-api'`). Servies sous <base href="/<app>/">,
  ## leurs appels portent donc le prefixe deux fois. La prod retire le premier
  ## segment dans roles/reverse ; on verifie que c'est aussi le cas ici.
  ##
  ## Sans ce retrait, l'appel retombe sur le service des applications et le front
  ## recoit du HTML en 200 la ou il attend du JSON.
  for app in identity referential ingest archive-search collect pastis; do
    local body
    body="$(get_body "$ORIGIN/$app/$app-api/v1/ping")"

    check_contains "API-RELATIVE /$app/$app-api atteint la gateway" \
      '"stub":"api-gateway"' "$body"
    check_contains "API-RELATIVE /$app/$app-api voit son prefixe retire" \
      "\"uri\":\"/$app-api/v1/ping\"" "$body"
    check_contains "API-RELATIVE /$app/$app-api presente le certificat ${EXPECTED_CN[$app]}" \
      "CN=${EXPECTED_CN[$app]}" "$body"
  done

  ## Le cas exact remonte depuis la console du navigateur.
  check_contains "API-RELATIVE le cas signale : archive-search/security" \
    '"uri":"/archive-search-api/security"' \
    "$(get_body "$ORIGIN/archive-search/archive-search-api/security")"

  ## Le retrait ne doit valoir que pour l API de la meme application : le
  ## nginx_webapp d une application n expose que son propre prefixe.
  check_contains "API-RELATIVE l API d une autre application n est pas routee" \
    "VITAMUI-DIST-MARKER:archive-search" \
    "$(get_body "$ORIGIN/archive-search/referential-api/v1/ping")"
}

test_cas() {
  section "CAS"

  check_contains "CAS /cas/login atteint cas-server" \
    '"stub":"cas"' "$(get_body "$ORIGIN/cas/login")"
  check_contains "CAS le context-path /cas est conserve" \
    '"uri":"/cas/login"' "$(get_body "$ORIGIN/cas/login")"

  check "CAS /cas redirige vers /cas/login" \
    302 "$(get_status "$ORIGIN/cas")"
  check_contains "CAS la redirection pointe sur l origine unique" \
    "location: $ORIGIN/cas/login" "$(get_header "$ORIGIN/cas" | tr 'A-Z' 'a-z')"

  ## Les assets de cas-server ne doivent pas etre captures par la location
  ## des assets Angular.
  check_contains "CAS ses propres assets ne sont pas captures par les assets Angular" \
    '"stub":"cas"' "$(get_body "$ORIGIN/cas/css/cas.css")"
}

test_redirection_http() {
  section "Redirection HTTP"

  local url="http://$SERVER_NAME:$HTTP_PORT/archive-search/"
  check "HTTP le port en clair redirige en 301" \
    301 "$(get_status "$url")"
  check_contains "HTTP la redirection preserve le chemin" \
    "location: $ORIGIN/archive-search/" "$(get_header "$url" | tr 'A-Z' 'a-z')"
}

test_ng_serve() {
  section "Surcharge ng serve"

  local conf="$ROOT/ng-serve/archive-search.conf"

  cat > "$conf" <<EOF
location ^~ /archive-search/ {
    proxy_pass https://$SERVER_NAME:$STUB_NG_SERVE_PORT;
    proxy_ssl_verify      off;
    proxy_ssl_server_name on;
    proxy_http_version 1.1;
    proxy_set_header   Upgrade \$http_upgrade;
    proxy_set_header   Connection \$vitamui_connection_upgrade;
    proxy_read_timeout 86400s;
    include /etc/nginx/snippets/proxy_params.conf;
}
EOF

  docker exec "$(compose_main ps -q nginx)" nginx -s reload >/dev/null 2>&1
  sleep 1

  check_contains "NG-SERVE /archive-search/ passe par le serveur de dev" \
    '"stub":"ng-serve"' "$(get_body "$ORIGIN/archive-search/")"
  check_contains "NG-SERVE le chemin complet est transmis au serveur de dev" \
    '"uri":"/archive-search/archive-search/tenant/1"' \
    "$(get_body "$ORIGIN/archive-search/archive-search/tenant/1")"

  ## La surcharge ne doit toucher que l application visee.
  check_contains "NG-SERVE les autres applications restent sur leur dist" \
    "VITAMUI-DIST-MARKER:ingest" "$(get_body "$ORIGIN/ingest/")"
  check_contains "NG-SERVE l API de l application reste routee vers la gateway" \
    '"stub":"api-gateway"' "$(get_body "$ORIGIN/archive-search-api/v1/ping")"

  rm -f "$conf"
  docker exec "$(compose_main ps -q nginx)" nginx -s reload >/dev/null 2>&1
  sleep 1

  check_contains "NG-SERVE le retrait de la surcharge rebascule sur le dist" \
    "VITAMUI-DIST-MARKER:archive-search" "$(get_body "$ORIGIN/archive-search/")"
}

# ===========================================================================
main() {
  select_docker_context
  build_fixtures
  trap teardown EXIT

  printf '%sReverse proxy VitamUI — suite de tests%s\n' "$BOLD" "$OFF"
  printf '%sorigine %s · gateway 127.0.0.1:%s · cas 127.0.0.1:%s%s\n' \
    "$DIM" "$ORIGIN" "$STUB_GATEWAY_PORT" "$STUB_CAS_PORT" "$OFF"

  start_stack || exit 1

  test_configuration
  test_routage_applications
  test_dist_absent
  test_routes_profondes
  test_assets
  test_apis_mtls
  test_apis_relatives
  test_cas
  test_redirection_http
  test_ng_serve

  printf '\n%s%d reussis, %d echoues' "$BOLD" "$PASSED" "$FAILED"
  ((SKIPPED)) && printf ', %d ignores' "$SKIPPED"
  printf '%s\n' "$OFF"

  if ((FAILED)); then
    printf '\n%sEchecs :%s\n' "$RED" "$OFF"
    printf '  %s\n' "${FAILURES[@]}"
    exit 1
  fi
}

main "$@"
