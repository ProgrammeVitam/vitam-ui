#!/usr/bin/env bash
#
# Test suite for the VitamUI dev reverse proxy: starts an isolated nginx
# instance (own compose project and ports) wired to stub upstreams, then
# checks end-to-end routing for the eight applications.
#
#   ./run-tests.sh            run the whole suite
#   ./run-tests.sh -k ROUTING run only the tests whose name contains ROUTING
#   KEEP_UP=1 ./run-tests.sh  keep the containers up for inspection
#
# Re-exec under bash when invoked via `sh` (dash lacks pipefail and
# associative arrays). This test must stay POSIX: dash still reads it.
if [ -z "${BASH_VERSION:-}" ]; then
  exec bash "$0" "$@"
fi

set -uo pipefail

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "$HERE/.." && pwd)"
cd "$HERE"

# Shifted ports, to coexist with a running dev instance.
export TEST_WORK_DIR="$HERE/.work"
export VITAMUI_CERTS_DIR="$(cd "$ROOT/../../../dev-deployment/environments/certs/vitamui-services" && pwd)"

SERVER_NAME="${SERVER_NAME:-dev.vitamui.com}"
HTTPS_PORT="${HTTPS_PORT:-14443}"
HTTP_PORT="${HTTP_PORT:-14080}"
export STUB_GATEWAY_PORT="${STUB_GATEWAY_PORT:-18070}"
export STUB_CAS_PORT="${STUB_CAS_PORT:-18080}"
export STUB_NG_SERVE_PORT="${STUB_NG_SERVE_PORT:-14209}"

# A port nothing listens on: connection refused -> nginx falls back to dist.
DEAD_PORT="${DEAD_PORT:-19999}"

ORIGIN="https://$SERVER_NAME:$HTTPS_PORT"
PROJECT_MAIN=vitamui-nginx-test
PROJECT_STUB=vitamui-nginx-test-upstreams

# Docker container names are machine-global, even across compose projects.
export VITAMUI_CONTAINER_NAME=vitamui-nginx-under-test

APPS=(portal identity referential ingest archive-search collect pastis design-system)
API_APPS=(portal identity referential ingest archive-search collect pastis)

# Expected client certificate per API prefix (cf. conf/snippets/maps.conf).
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

RED=$'\033[31m'; GREEN=$'\033[32m'; DIM=$'\033[2m'; BOLD=$'\033[1m'; OFF=$'\033[0m'
PASSED=0; FAILED=0; SKIPPED=0
FAILURES=()

section() { printf '\n%s%s%s\n' "$BOLD" "$*" "$OFF"; }

check() {
  # check <name> <expected> <actual>
  local name="$1" want="$2" got="$3"

  if [[ -n "$FILTER" && "$name" != *"$FILTER"* ]]; then
    SKIPPED=$((SKIPPED + 1)); return 0
  fi

  if [[ "$got" == "$want" ]]; then
    printf '  %s✔%s %s\n' "$GREEN" "$OFF" "$name"
    PASSED=$((PASSED + 1))
  else
    printf '  %s✘%s %s\n' "$RED" "$OFF" "$name"
    printf '      %sexpected:%s %s\n' "$DIM" "$OFF" "$want"
    printf '      %sactual  :%s %s\n' "$DIM" "$OFF" "$got"
    FAILED=$((FAILED + 1))
    FAILURES+=("$name")
  fi
}

check_contains() {
  # check_contains <name> <expected fragment> <text>
  local name="$1" needle="$2" haystack="$3"

  if [[ -n "$FILTER" && "$name" != *"$FILTER"* ]]; then
    SKIPPED=$((SKIPPED + 1)); return 0
  fi

  if [[ "$haystack" == *"$needle"* ]]; then
    printf '  %s✔%s %s\n' "$GREEN" "$OFF" "$name"
    PASSED=$((PASSED + 1))
  else
    printf '  %s✘%s %s\n' "$RED" "$OFF" "$name"
    printf '      %scontains:%s %s\n' "$DIM" "$OFF" "$needle"
    printf '      %sactual  :%s %s\n' "$DIM" "$OFF" "${haystack:0:400}"
    FAILED=$((FAILED + 1))
    FAILURES+=("$name")
  fi
}

# Dev certificates are self-signed: -k is required client-side.
get_status() { curl -k -s -o /dev/null -w '%{http_code}' --max-time 10 "$@"; }
get_body()   { curl -k -s --max-time 10 "$@"; }
get_header() { curl -k -s -o /dev/null -D - --max-time 10 "$@" | tr -d '\r'; }

compose_main() {
  docker compose -p "$PROJECT_MAIN" -f "$ROOT/docker-compose.yml" --project-directory "$ROOT" "$@"
}
compose_stub() {
  docker compose -p "$PROJECT_STUB" -f "$HERE/docker-compose.test.yml" --project-directory "$HERE" "$@"
}

select_docker_context() {
  if docker version >/dev/null 2>&1; then
    # Docker Desktop's VM breaks network_mode host: prefer the system daemon.
    if docker info --format '{{.OperatingSystem}}' 2>/dev/null | grep -q 'Docker Desktop' \
       && DOCKER_CONTEXT=default docker version >/dev/null 2>&1; then
      export DOCKER_CONTEXT=default
      printf '%s(Docker Desktop context: switched to "default")%s\n' "$DIM" "$OFF"
    fi
    return 0
  fi
  if DOCKER_CONTEXT=default docker version >/dev/null 2>&1; then
    export DOCKER_CONTEXT=default
    printf '%s(docker context switched to "default")%s\n' "$DIM" "$OFF"
    return 0
  fi
  printf '%sno reachable docker daemon%s\n' "$RED" "$OFF" >&2
  exit 1
}

build_fixtures() {
  # Fake dist/ trees: one marker per app, to test routing without running
  # eight Angular builds.
  rm -rf "$TEST_WORK_DIR"
  mkdir -p "$TEST_WORK_DIR/ca"

  cat "$VITAMUI_CERTS_DIR/ca/ca-root.crt" \
      "$VITAMUI_CERTS_DIR/ca/ca-intermediate.crt" > "$TEST_WORK_DIR/ca/ca-chain.crt"

  # `collect` uses the Angular >= 17 browser/ layout, the others are flat;
  # the same assertions must pass on both.
  local nested=collect

  for app in "${APPS[@]}"; do
    local base="$TEST_WORK_DIR/dist/$app"
    [[ "$app" == "$nested" ]] && base="$TEST_WORK_DIR/dist/$app/browser"

    mkdir -p "$base/assets"
    printf '<!doctype html><title>%s</title><body>VITAMUI-DIST-MARKER:%s</body>\n' \
      "$app" "$app" > "$base/index.html"
    printf 'console.log("VITAMUI-ASSET-MARKER:%s");\n' "$app" > "$base/main.js"
    printf '{"MARKER":"%s"}\n' "$app" > "$base/assets/config-dev.json"

    # Only in the portal dist: proves there is no cross-app asset fallback.
    if [[ "$app" == portal ]]; then
      printf 'console.log("VITAMUI-PORTAL-ONLY");\n' > "$base/portal-only.js"
    fi
  done

  export VITAMUI_DIST_DIR="$TEST_WORK_DIR/dist"
}

# The stack is started with every Angular dev-server port pointing at a closed
# port (DEAD_PORT): a real dev server running on the host must not interfere
# with the dist assertions. The LIVE tests restart the stack with some ports
# pointing at the ng-serve stub, via extra VAR=VALUE arguments.
start_stack() {
  compose_stub up -d --quiet-pull >/dev/null

  (
    export \
      VITAMUI_SERVER_NAME="$SERVER_NAME" \
      VITAMUI_PUBLIC_ORIGIN="$ORIGIN" \
      VITAMUI_HTTPS_PORT="$HTTPS_PORT" \
      VITAMUI_HTTP_PORT="$HTTP_PORT" \
      VITAMUI_GATEWAY="127.0.0.1:$STUB_GATEWAY_PORT" \
      VITAMUI_CAS="127.0.0.1:$STUB_CAS_PORT" \
      VITAMUI_DIST_DIR="$VITAMUI_DIST_DIR" \
      VITAMUI_CERTS_DIR="$VITAMUI_CERTS_DIR" \
      VITAMUI_NG_PORT_PORTAL="$DEAD_PORT" \
      VITAMUI_NG_PORT_IDENTITY="$DEAD_PORT" \
      VITAMUI_NG_PORT_REFERENTIAL="$DEAD_PORT" \
      VITAMUI_NG_PORT_INGEST="$DEAD_PORT" \
      VITAMUI_NG_PORT_ARCHIVE_SEARCH="$DEAD_PORT" \
      VITAMUI_NG_PORT_COLLECT="$DEAD_PORT" \
      VITAMUI_NG_PORT_DESIGN_SYSTEM="$DEAD_PORT" \
      VITAMUI_NG_PORT_PASTIS="$DEAD_PORT"
    local override
    for override in "$@"; do export "${override?}"; done
    compose_main up -d --quiet-pull --force-recreate >/dev/null 2>&1
  )

  # Active wait rather than a fixed sleep.
  local i
  for i in $(seq 1 40); do
    [[ "$(get_status "$ORIGIN/__vitamui/health")" == 200 ]] && return 0
    sleep 0.5
  done

  printf '%sthe test stack did not start%s\n' "$RED" "$OFF" >&2
  compose_main logs --tail 40
  compose_stub logs --tail 20
  return 1
}

teardown() {
  local rc=$?
  if [[ "${KEEP_UP:-}" == 1 ]]; then
    printf '\n%sKEEP_UP=1: containers left up (%s)%s\n' "$DIM" "$ORIGIN" "$OFF"
    return $rc
  fi
  VITAMUI_DIST_DIR="${VITAMUI_DIST_DIR:-$TEST_WORK_DIR/dist}" \
  VITAMUI_CERTS_DIR="$VITAMUI_CERTS_DIR" \
    compose_main down --remove-orphans >/dev/null 2>&1
  compose_stub down --remove-orphans >/dev/null 2>&1
  rm -rf "$TEST_WORK_DIR"
  return $rc
}

test_configuration() {
  section "Configuration"

  local out
  out="$(VITAMUI_SERVER_NAME="$SERVER_NAME" VITAMUI_PUBLIC_ORIGIN="$ORIGIN" \
         VITAMUI_HTTPS_PORT="$HTTPS_PORT" VITAMUI_HTTP_PORT="$HTTP_PORT" \
         VITAMUI_GATEWAY="127.0.0.1:$STUB_GATEWAY_PORT" VITAMUI_CAS="127.0.0.1:$STUB_CAS_PORT" \
         VITAMUI_DIST_DIR="$VITAMUI_DIST_DIR" VITAMUI_CERTS_DIR="$VITAMUI_CERTS_DIR" \
         compose_main exec -T nginx nginx -t 2>&1)"
  check_contains "CONFIGURATION nginx syntax is valid" "syntax is ok" "$out"
  check_contains "CONFIGURATION the configuration test passes" "test is successful" "$out"

  check "CONFIGURATION the local probe answers" \
    '{"status":"UP","component":"vitamui-dev-reverse"}' \
    "$(get_body "$ORIGIN/__vitamui/health")"
}

test_application_routing() {
  section "Application routing"

  check "ROUTING portal is served at the root" \
    200 "$(get_status "$ORIGIN/")"
  check_contains "ROUTING the root serves the portal dist" \
    "VITAMUI-DIST-MARKER:portal" "$(get_body "$ORIGIN/")"

  for app in "${APPS[@]}"; do
    [[ "$app" == portal ]] && continue
    check "ROUTING /$app/ answers 200" \
      200 "$(get_status "$ORIGIN/$app/")"
    check_contains "ROUTING /$app/ serves the $app dist" \
      "VITAMUI-DIST-MARKER:$app" "$(get_body "$ORIGIN/$app/")"
  done

  # Without a trailing slash the app is served directly, not redirected.
  check "ROUTING /archive-search without a trailing slash is served" \
    200 "$(get_status "$ORIGIN/archive-search")"
  check_contains "ROUTING /archive-search without a slash serves the right dist" \
    "VITAMUI-DIST-MARKER:archive-search" "$(get_body "$ORIGIN/archive-search")"
}

test_missing_dist() {
  section "Missing application build"

  # Without try_files' =404, a missing dist yields a 500 redirection cycle.
  local saved="$TEST_WORK_DIR/design-system-index.html"
  mv "$TEST_WORK_DIR/dist/design-system/index.html" "$saved"

  check "MISSING-DIST an application without a dist answers 404, not 500" \
    404 "$(get_status "$ORIGIN/design-system/")"
  check "MISSING-DIST a deep route answers 404 as well" \
    404 "$(get_status "$ORIGIN/design-system/some/route")"

  mv "$saved" "$TEST_WORK_DIR/dist/design-system/index.html"

  check "MISSING-DIST the restored dist is served again" \
    200 "$(get_status "$ORIGIN/design-system/")"

  # The portal is served from another root, with its own try_files.
  local saved_portal="$TEST_WORK_DIR/portal-index.html"
  mv "$TEST_WORK_DIR/dist/portal/index.html" "$saved_portal"

  check "MISSING-DIST the portal without a dist answers 404, not 500" \
    404 "$(get_status "$ORIGIN/")"

  mv "$saved_portal" "$TEST_WORK_DIR/dist/portal/index.html"
}

test_deep_routes() {
  section "Deep Angular routes"

  # The exact URL shape of deployed environments.
  local url="$ORIGIN/archive-search/archive-search/tenant/1"
  check "DEEP-ROUTE archive-search/tenant/1 answers 200" \
    200 "$(get_status "$url")"
  check_contains "DEEP-ROUTE archive-search/tenant/1 falls back to its index.html" \
    "VITAMUI-DIST-MARKER:archive-search" "$(get_body "$url")"

  check_contains "DEEP-ROUTE ingest serves its own index.html" \
    "VITAMUI-DIST-MARKER:ingest" "$(get_body "$ORIGIN/ingest/ingest/tenant/2/holding")"

  check_contains "DEEP-ROUTE a portal route stays on the portal" \
    "VITAMUI-DIST-MARKER:portal" "$(get_body "$ORIGIN/tenant/1")"
}

test_assets() {
  section "Assets"

  # collect lives in dist/collect/browser/: the public URL must not show it.
  check_contains "ASSET an application's asset is served from its dist" \
    "VITAMUI-ASSET-MARKER:collect" "$(get_body "$ORIGIN/collect/main.js")"
  check_contains "ASSET the Angular 17+ browser/ layout is served identically" \
    "VITAMUI-DIST-MARKER:collect" "$(get_body "$ORIGIN/collect/some/deep/route")"

  check_contains "ASSET the portal asset is served from the root" \
    "VITAMUI-ASSET-MARKER:portal" "$(get_body "$ORIGIN/main.js")"

  check_contains "ASSET config-dev.json is served" \
    '"MARKER":"pastis"' "$(get_body "$ORIGIN/pastis/assets/config-dev.json")"

  # A missing asset must 404, not fall back to index.html.
  check "ASSET a missing asset answers 404, not index.html" \
    404 "$(get_status "$ORIGIN/archive-search/assets/inexistant.js")"

  # No cross-app fallback to the portal dist.
  check "ASSET no cross fallback to the portal dist" \
    404 "$(get_status "$ORIGIN/archive-search/portal-only.js")"
  check_contains "ASSET the portal-only file is still served at the root" \
    "VITAMUI-PORTAL-ONLY" "$(get_body "$ORIGIN/portal-only.js")"
}

test_apis_mtls() {
  section "APIs and mTLS to the api-gateway"

  for app in "${API_APPS[@]}"; do
    local body
    body="$(get_body "$ORIGIN/$app-api/v1/ping")"

    check_contains "API /$app-api reaches the api-gateway" \
      '"stub":"api-gateway"' "$body"
    check_contains "API /$app-api presents the ${EXPECTED_CN[$app]} certificate" \
      "CN=${EXPECTED_CN[$app]}" "$body"
    check_contains "API /$app-api is validated by the gateway" \
      '"verify":"SUCCESS"' "$body"
    check_contains "API /$app-api forwards the URI unchanged" \
      "\"uri\":\"/$app-api/v1/ping\"" "$body"
  done

  # The -api prefix must win over the app prefix.
  check_contains "API the -api prefix wins over the application prefix" \
    '"stub":"api-gateway"' "$(get_body "$ORIGIN/archive-search-api/foo.json")"
}

test_relative_apis() {
  section "APIs called relative to the base href"

  # Apps declare a relative BASE_URL, so their calls repeat the prefix; the
  # first segment must be stripped, as prod's roles/reverse does.
  for app in identity referential ingest archive-search collect pastis; do
    local body
    body="$(get_body "$ORIGIN/$app/$app-api/v1/ping")"

    check_contains "RELATIVE-API /$app/$app-api reaches the gateway" \
      '"stub":"api-gateway"' "$body"
    check_contains "RELATIVE-API /$app/$app-api has its prefix stripped" \
      "\"uri\":\"/$app-api/v1/ping\"" "$body"
    check_contains "RELATIVE-API /$app/$app-api presents the ${EXPECTED_CN[$app]} certificate" \
      "CN=${EXPECTED_CN[$app]}" "$body"
  done

  # The exact case reported from the browser console.
  check_contains "RELATIVE-API the reported case: archive-search/security" \
    '"uri":"/archive-search-api/security"' \
    "$(get_body "$ORIGIN/archive-search/archive-search-api/security")"

  # Stripping only applies to the app's own API, as in prod.
  check_contains "RELATIVE-API another application's API is not routed" \
    "VITAMUI-DIST-MARKER:archive-search" \
    "$(get_body "$ORIGIN/archive-search/referential-api/v1/ping")"
}

test_cas() {
  section "CAS"

  check_contains "CAS /cas/login reaches cas-server" \
    '"stub":"cas"' "$(get_body "$ORIGIN/cas/login")"
  check_contains "CAS the /cas context-path is preserved" \
    '"uri":"/cas/login"' "$(get_body "$ORIGIN/cas/login")"

  check "CAS /cas redirects to /cas/login" \
    302 "$(get_status "$ORIGIN/cas")"
  check_contains "CAS the redirect points to the single origin" \
    "location: $ORIGIN/cas/login" "$(get_header "$ORIGIN/cas" | tr 'A-Z' 'a-z')"

  # cas-server assets must not be captured by the Angular asset location.
  check_contains "CAS its own assets are not captured by the Angular assets" \
    '"stub":"cas"' "$(get_body "$ORIGIN/cas/css/cas.css")"
}

test_http_redirect() {
  section "HTTP redirect"

  local url="http://$SERVER_NAME:$HTTP_PORT/archive-search/"
  check "HTTP the plain port redirects with 301" \
    301 "$(get_status "$url")"
  check_contains "HTTP the redirect preserves the path" \
    "location: $ORIGIN/archive-search/" "$(get_header "$url" | tr 'A-Z' 'a-z')"
}

# Live mode: the ng-serve stub plays the role of a running dev server for
# archive-search and portal; the other ports stay closed (dist fallback).
test_live_mode() {
  section "Live mode (automatic dev-server / dist switch)"

  start_stack \
    "VITAMUI_NG_PORT_ARCHIVE_SEARCH=$STUB_NG_SERVE_PORT" \
    "VITAMUI_NG_PORT_PORTAL=$STUB_NG_SERVE_PORT" || return 1

  check_contains "LIVE /archive-search/ goes through the dev server" \
    '"stub":"ng-serve"' "$(get_body "$ORIGIN/archive-search/")"
  check_contains "LIVE the full path is forwarded to the dev server" \
    '"uri":"/archive-search/archive-search/tenant/1"' \
    "$(get_body "$ORIGIN/archive-search/archive-search/tenant/1")"
  check_contains "LIVE an asset goes through the dev server too" \
    '"stub":"ng-serve"' "$(get_body "$ORIGIN/archive-search/main.js")"

  # The formerly impossible case: portal served live at the root.
  check_contains "LIVE the portal is served live at the root" \
    '"stub":"ng-serve"' "$(get_body "$ORIGIN/")"
  check_contains "LIVE a portal asset goes through the dev server" \
    '"stub":"ng-serve"' "$(get_body "$ORIGIN/main.js")"

  # Apps without a running dev server keep being served from their dist.
  check_contains "LIVE the other applications stay on their dist" \
    "VITAMUI-DIST-MARKER:ingest" "$(get_body "$ORIGIN/ingest/")"

  # nginx keeps the hand on API calls, live or not: the dev server never
  # sees them, both in absolute and in base-href-relative form.
  check_contains "LIVE the absolute API form stays routed to the gateway" \
    '"stub":"api-gateway"' "$(get_body "$ORIGIN/archive-search-api/v1/ping")"
  check_contains "LIVE the relative API form stays routed to the gateway" \
    '"stub":"api-gateway"' "$(get_body "$ORIGIN/archive-search/archive-search-api/v1/ping")"
  check_contains "LIVE the relative API form still has its prefix stripped" \
    '"uri":"/archive-search-api/v1/ping"' \
    "$(get_body "$ORIGIN/archive-search/archive-search-api/v1/ping")"
  check_contains "LIVE CAS stays routed to cas-server" \
    '"stub":"cas"' "$(get_body "$ORIGIN/cas/login")"

  # Back to a stack with no dev server at all: everything on dist again.
  start_stack || return 1

  check_contains "LIVE stopping the dev server falls back to the dist" \
    "VITAMUI-DIST-MARKER:archive-search" "$(get_body "$ORIGIN/archive-search/")"
  check_contains "LIVE the portal falls back to its dist too" \
    "VITAMUI-DIST-MARKER:portal" "$(get_body "$ORIGIN/")"
}

main() {
  select_docker_context
  build_fixtures
  trap teardown EXIT

  printf '%sVitamUI reverse proxy — test suite%s\n' "$BOLD" "$OFF"
  printf '%sorigin %s · gateway 127.0.0.1:%s · cas 127.0.0.1:%s%s\n' \
    "$DIM" "$ORIGIN" "$STUB_GATEWAY_PORT" "$STUB_CAS_PORT" "$OFF"

  start_stack || exit 1

  test_configuration
  test_application_routing
  test_missing_dist
  test_deep_routes
  test_assets
  test_apis_mtls
  test_relative_apis
  test_cas
  test_http_redirect
  test_live_mode

  printf '\n%s%d passed, %d failed' "$BOLD" "$PASSED" "$FAILED"
  ((SKIPPED)) && printf ', %d skipped' "$SKIPPED"
  printf '%s\n' "$OFF"

  if ((FAILED)); then
    printf '\n%sFailures:%s\n' "$RED" "$OFF"
    printf '  %s\n' "${FAILURES[@]}"
    exit 1
  fi
}

main "$@"
