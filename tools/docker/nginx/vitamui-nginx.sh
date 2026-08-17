#!/usr/bin/env bash
#
# Drives the VitamUI dev reverse proxy.
#
#   ./vitamui-nginx.sh up|down|status
#
# Re-exec under bash when invoked via `sh` (dash lacks pipefail and
# associative arrays). This test must stay POSIX: dash still reads it.
if [ -z "${BASH_VERSION:-}" ]; then
  exec bash "$0" "$@"
fi

set -euo pipefail

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$HERE"

RED=$'\033[31m'; GREEN=$'\033[32m'; YELLOW=$'\033[33m'; BOLD=$'\033[1m'; OFF=$'\033[0m'
info()  { printf '%s\n' "$*"; }
ok()    { printf '%s✔%s %s\n' "$GREEN" "$OFF" "$*"; }
warn()  { printf '%s!%s %s\n' "$YELLOW" "$OFF" "$*"; }
fail()  { printf '%s✘%s %s\n' "$RED" "$OFF" "$*" >&2; }
die()   { fail "$*"; exit 1; }

# Angular dev-server ports, from ui/ui-frontend/angular.json.
declare -A NG_PORTS=(
  [portal]=4200
  [identity]=4201
  [referential]=4202
  [ingest]=4208
  [archive-search]=4209
  [collect]=4210
  [design-system]=4242
  [pastis]=4251
)

APPS=(portal identity referential ingest archive-search collect pastis design-system)

# Fall back to the `default` docker context when the current one (typically
# desktop-linux with Docker Desktop stopped) is unreachable.
select_docker_context() {
  local current
  current="$(docker context show 2>/dev/null || echo '?')"

  if docker version >/dev/null 2>&1; then
    # Docker Desktop runs containers in a VM: with network_mode host, the
    # ports open inside the VM, unreachable from this machine. Prefer the
    # system daemon when it is available.
    if docker info --format '{{.OperatingSystem}}' 2>/dev/null | grep -q 'Docker Desktop' \
       && DOCKER_CONTEXT=default docker version >/dev/null 2>&1; then
      export DOCKER_CONTEXT=default
      warn "context \"$current\" is Docker Desktop (host networking unusable), using \"default\""
      warn "  to make this permanent: docker context use default"
    fi
    return 0
  fi

  if DOCKER_CONTEXT=default docker version >/dev/null 2>&1; then
    export DOCKER_CONTEXT=default
    warn "docker context \"$current\" unreachable, falling back to \"default\""
    warn "  to make this permanent: docker context use default"
    return 0
  fi

  die "no reachable docker daemon (current context: $current)"
}

compose() { docker compose "$@"; }

env_value() {
  # Read a variable from .env if present, else return the given default.
  local name="$1" default="$2" value=''
  if [[ -f .env ]]; then
    value="$(sed -n "s/^[[:space:]]*${name}=//p" .env | tail -1)"
  fi
  printf '%s' "${value:-$default}"
}

preflight() {
  local server_name dist_dir certs_dir https_port rc=0
  server_name="$(env_value VITAMUI_SERVER_NAME dev.vitamui.com)"
  dist_dir="$(env_value VITAMUI_DIST_DIR ../../../ui/ui-frontend/dist)"
  certs_dir="$(env_value VITAMUI_CERTS_DIR ../../../dev-deployment/environments/certs/vitamui-services)"
  https_port="$(env_value VITAMUI_HTTPS_PORT 443)"

  info "${BOLD}Prerequisites${OFF}"

  if getent hosts "$server_name" | grep -q '^127\.'; then
    ok "$server_name resolves to the loopback"
  else
    fail "$server_name does not resolve to 127.0.0.1"
    info "    add to /etc/hosts:  127.0.0.1    $server_name"
    rc=1
  fi

  if [[ -d "$certs_dir/servers/reverse" && -d "$certs_dir/clients" ]]; then
    ok "dev certificates present ($certs_dir)"
  else
    fail "certificates not found under $certs_dir"
    info "    generate them:  cd ../../../dev-deployment && ./generate_certs.sh"
    rc=1
  fi

  # Both dist layouts are accepted: flat (as served in prod) and
  # dist/<app>/browser/ (Angular >= 17).
  if [[ -d "$dist_dir" ]]; then
    local missing=() no_config=()
    for app in "${APPS[@]}"; do
      local root=''
      [[ -f "$dist_dir/$app/index.html" ]] && root="$dist_dir/$app"
      [[ -f "$dist_dir/$app/browser/index.html" ]] && root="$dist_dir/$app/browser"

      if [[ -z "$root" ]]; then
        missing+=("$app")
      elif [[ ! -f "$root/assets/config-dev.json" ]]; then
        no_config+=("$app")
      fi
    done

    if ((${#missing[@]} == 0)); then
      ok "all 8 dist/ are present"
    else
      warn "missing dist/: ${missing[*]}"
      info "    these applications will answer 404; nginx starts anyway."
      build_hint
    fi

    # Production builds exclude config-dev.json: the app loads but finds none
    # of its URLs.
    if ((${#no_config[@]} > 0)); then
      warn "dist/ without config-dev.json: ${no_config[*]}"
      info "    built in the production configuration, which excludes that file."
      info "    these applications will render without finding their configuration."
      build_hint
    fi
  else
    fail "dist directory not found: $dist_dir"
    build_hint
    rc=1
  fi

  # Usual culprit: tools/docker/nginx-cas-x509, the repo's other nginx, which
  # also listens on 443 (see the README).
  if ss -ltn 2>/dev/null | grep -qE "[:.]${https_port}[[:space:]]"; then
    if [[ "$(container_state)" == running ]]; then
      ok "port $https_port held by vitamui-nginx"
    elif [[ -n "$(x509_container)" ]]; then
      fail "port $https_port is held by the x509 test bench (nginx-cas-x509)"
      info "    the two nginx are exclusive: this one serves all the frontends,"
      info "    the other serves only CAS and requires a client certificate on every request."
      info "    stop it:  (cd ../nginx-cas-x509 && docker compose down)"
      rc=1
    else
      fail "port $https_port is already used by another process"
      info "    identify it:  ss -ltnp | grep ':$https_port'"
      rc=1
    fi
  else
    ok "port $https_port free"
  fi

  return $rc
}

build_hint() {
  info "    build (development configuration, the only one embedding config-dev.json):"
  info "      cd ../../../ui/ui-frontend"
  info "      npm run build:allModulesDev        # or npm run build:<app>:dev"
  info "    or, without building anything, serve the app live:"
  info "      cd ../../../ui/ui-frontend && npm run start:<app>"
}

container_state() {
  # `docker inspect` on a missing container emits an empty line on stdout;
  # normalize to "absent".
  local state
  state="$(docker inspect -f '{{.State.Status}}' vitamui-nginx 2>/dev/null)" || state=''
  printf '%s' "${state:-absent}"
}

# tools/docker/nginx-cas-x509 container name, if running (derived by docker
# from the directory: nginx-cas-x509-nginx-1).
x509_container() {
  docker ps --filter 'name=nginx-cas-x509' --format '{{.Names}}' 2>/dev/null | head -1
}

cmd_up() {
  preflight || die "prerequisites not met"
  info ""
  compose up -d
  wait_healthy
  info ""
  cmd_status
}

# nginx takes a moment to open its socket after `up`; without this wait the
# following `status` reports the probe unreachable.
wait_healthy() {
  local origin i
  origin="$(env_value VITAMUI_PUBLIC_ORIGIN https://dev.vitamui.com)"
  for i in $(seq 1 20); do
    curl -ksSf --max-time 2 "$origin/__vitamui/health" >/dev/null 2>&1 && return 0
    sleep 0.5
  done
  return 1
}

cmd_down() { compose down; }

cmd_status() {
  local origin state
  origin="$(env_value VITAMUI_PUBLIC_ORIGIN https://dev.vitamui.com)"
  state="$(container_state)"

  info "${BOLD}Container${OFF}: $state"
  [[ "$state" == running ]] || return 0

  if curl -ksSf --max-time 3 "$origin/__vitamui/health" >/dev/null 2>&1; then
    ok "probe $origin/__vitamui/health"
  else
    fail "probe unreachable at $origin/__vitamui/health"
  fi

  info ""
  info "${BOLD}Applications${OFF} (live = served from a running \`npm run start:<app>\`, else dist)"
  printf '  %-16s %-42s %s\n' portal "$origin/" "$(app_mode portal)"
  for app in "${APPS[@]}"; do
    [[ "$app" == portal ]] && continue
    printf '  %-16s %-42s %s\n' "$app" "$origin/$app/" "$(app_mode "$app")"
  done
  info ""
  info "${BOLD}Services${OFF}"
  printf '  %-16s %s\n' cas "$origin/cas/login"
  printf '  %-16s %s\n' apis "$origin/<app>-api/..."
}

# Mode per app: live when its Angular dev-server port answers; otherwise dist
# if one is actually built (either layout), else the app will answer 404.
app_mode() {
  local app="$1" port="${NG_PORTS[$1]}" dist_dir
  if timeout 0.3 bash -c "exec 3<>/dev/tcp/127.0.0.1/$port" 2>/dev/null; then
    printf '%slive%s (:%s)' "$GREEN" "$OFF" "$port"
    return 0
  fi
  dist_dir="$(env_value VITAMUI_DIST_DIR ../../../ui/ui-frontend/dist)"
  if [[ -f "$dist_dir/$app/index.html" || -f "$dist_dir/$app/browser/index.html" ]]; then
    printf 'dist'
  else
    printf '%sno dist (404)%s' "$RED" "$OFF"
  fi
}

usage() {
  cat <<'EOF'
VitamUI dev reverse proxy — single origin for all frontends.

  up        check the prerequisites, then start the container
  down      stop and remove the container
  status    container state, probe, URL table with live/dist mode

An application whose Angular dev server is running (`npm run start:<app>` in
ui/ui-frontend) is served live from it, with hot reload; otherwise its dist/
is served. The switch is automatic, per request — nothing to toggle.

Applications: portal identity referential ingest archive-search collect pastis design-system
EOF
}

main() {
  local cmd="${1:-}"
  shift || true

  case "$cmd" in
    up|down|status) select_docker_context ;;
  esac

  case "$cmd" in
    up)     cmd_up ;;
    down)   cmd_down ;;
    status) cmd_status ;;
    ''|-h|--help|help) usage ;;
    *)      usage; exit 1 ;;
  esac
}

main "$@"
