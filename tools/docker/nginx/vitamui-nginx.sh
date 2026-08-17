#!/usr/bin/env bash
#
# Pilote le reverse proxy de developpement VitamUI.
#
#   ./vitamui-nginx.sh up|down|restart|reload|status|logs|check
#   ./vitamui-nginx.sh ng-serve <app> [port]
#   ./vitamui-nginx.sh ng-serve --clear [app]
#
# Invoque via `sh vitamui-nginx.sh`, l'interpreteur est dash sur Debian et Ubuntu :
# il ne connait ni `set -o pipefail` ni les tableaux associatifs, et echoue sur une
# erreur de syntaxe sans rapport avec la cause. On se relance donc sous bash.
# Ce test doit rester en syntaxe POSIX, c'est encore dash qui le lit.
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

## Ports du serveur de dev Angular, repris de ui/ui-frontend/angular.json.
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

# ---------------------------------------------------------------------------
# Contexte docker
#
# Le CLI peut pointer vers un contexte dont le demon est eteint (typiquement
# `desktop-linux` quand Docker Desktop n'est pas lance) alors que le demon
# systeme, lui, tourne. On bascule alors sur `default` plutot que d'echouer avec
# un message obscur sur une socket absente.
# ---------------------------------------------------------------------------
select_docker_context() {
  if docker version >/dev/null 2>&1; then
    return 0
  fi

  local current
  current="$(docker context show 2>/dev/null || echo '?')"

  if DOCKER_CONTEXT=default docker version >/dev/null 2>&1; then
    export DOCKER_CONTEXT=default
    warn "contexte docker « $current » injoignable, bascule sur « default »"
    warn "  pour rendre ce choix permanent : docker context use default"
    return 0
  fi

  die "aucun demon docker joignable (contexte courant : $current)"
}

compose() { docker compose "$@"; }

# ---------------------------------------------------------------------------
# Prerequis
# ---------------------------------------------------------------------------
env_value() {
  # Lit une variable depuis .env si presente, sinon renvoie le defaut fourni.
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

  info "${BOLD}Prerequis${OFF}"

  if getent hosts "$server_name" | grep -q '^127\.'; then
    ok "$server_name resout vers la boucle locale"
  else
    fail "$server_name ne resout pas vers 127.0.0.1"
    info "    ajouter dans /etc/hosts :  127.0.0.1    $server_name"
    rc=1
  fi

  if [[ -d "$certs_dir/servers/reverse" && -d "$certs_dir/clients" ]]; then
    ok "certificats de dev presents ($certs_dir)"
  else
    fail "certificats introuvables sous $certs_dir"
    info "    les generer :  cd ../../../dev-deployment && ./generate_certs.sh"
    rc=1
  fi

  # Les deux dispositions sont acceptees : dist/<app>/index.html (arborescence
  # plate, celle que sert la prod) et dist/<app>/browser/index.html (Angular >= 17).
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
      ok "les 8 dist/ sont presents"
    else
      warn "dist/ manquants : ${missing[*]}"
      info "    ces applications repondront 404 ; nginx demarre quand meme."
      build_hint
    fi

    # Symptome deroutant : l'application s'affiche mais ne trouve aucune de ses
    # URL. C'est un build en configuration production, qui exclut config-dev.json.
    if ((${#no_config[@]} > 0)); then
      warn "dist/ sans config-dev.json : ${no_config[*]}"
      info "    construits en configuration production, qui exclut ce fichier."
      info "    ces applications s'afficheront sans trouver leur configuration."
      build_hint
    fi
  else
    fail "repertoire dist introuvable : $dist_dir"
    build_hint
    rc=1
  fi

  # Le suspect numero un est tools/docker/nginx-cas-x509 : c'est l'autre nginx
  # du depot, il ecoute lui aussi sur 443 et les deux sont donc exclusifs.
  # Cf. la section « Pourquoi il y a deux nginx » du README.
  if ss -ltn 2>/dev/null | grep -qE "[:.]${https_port}[[:space:]]"; then
    if [[ "$(container_state)" == running ]]; then
      ok "port $https_port tenu par vitamui-nginx"
    elif [[ -n "$(x509_container)" ]]; then
      fail "le port $https_port est tenu par le banc de test x509 (nginx-cas-x509)"
      info "    les deux nginx sont exclusifs : celui-ci sert tous les fronts,"
      info "    l'autre ne sert que CAS et exige un certificat client a chaque requete."
      info "    l'arreter :  (cd ../nginx-cas-x509 && docker compose down)"
      rc=1
    else
      fail "le port $https_port est deja occupe par un autre processus"
      info "    l'identifier :  ss -ltnp | grep ':$https_port'"
      rc=1
    fi
  else
    ok "port $https_port libre"
  fi

  return $rc
}

build_hint() {
  info "    construire (configuration development, seule a embarquer config-dev.json) :"
  info "      cd ../../../ui/ui-frontend"
  info "      npx ng build <app> --configuration development"
  info "    ou, sans rien construire :  $0 ng-serve <app>"
}

container_state() {
  # `docker inspect` sur un conteneur absent echoue en emettant une ligne vide sur
  # sa sortie standard : sans la substitution ci-dessous, l'etat vaudrait un saut
  # de ligne suivi de « absent ».
  local state
  state="$(docker inspect -f '{{.State.Status}}' vitamui-nginx 2>/dev/null)" || state=''
  printf '%s' "${state:-absent}"
}

# Nom du conteneur de tools/docker/nginx-cas-x509 s'il tourne. Ce compose ne fixe
# pas de container_name, docker le derive donc du repertoire : nginx-cas-x509-nginx-1.
x509_container() {
  docker ps --filter 'name=nginx-cas-x509' --format '{{.Names}}' 2>/dev/null | head -1
}

# ---------------------------------------------------------------------------
# Commandes
# ---------------------------------------------------------------------------
cmd_check() {
  info "${BOLD}Validation de la configuration${OFF}"
  # `compose run` monte les memes volumes et applique le meme envsubst que le
  # service reel : on valide la configuration effectivement rendue.
  if compose run --rm --no-deps --entrypoint '' nginx \
       sh -c '/docker-entrypoint.sh nginx -t 2>&1'; then
    ok "configuration valide"
  else
    die "configuration invalide"
  fi
}

cmd_up() {
  preflight || die "prerequis non satisfaits"
  info ""
  compose up -d
  wait_healthy
  info ""
  cmd_status
}

# nginx met une fraction de seconde a ouvrir son socket apres le `up`. Sans cette
# attente, le `status` qui suit signale la sonde injoignable alors que tout va bien.
wait_healthy() {
  local origin i
  origin="$(env_value VITAMUI_PUBLIC_ORIGIN https://dev.vitamui.com)"
  for i in $(seq 1 20); do
    curl -ksSf --max-time 2 "$origin/__vitamui/health" >/dev/null 2>&1 && return 0
    sleep 0.5
  done
  return 1
}

cmd_down()    { compose down; }
cmd_restart() { compose restart; cmd_status; }
cmd_logs()    { compose logs "${@:-}" ; }

cmd_reload() {
  docker exec vitamui-nginx nginx -s reload
  ok "configuration rechargee"
}

cmd_status() {
  local origin state
  origin="$(env_value VITAMUI_PUBLIC_ORIGIN https://dev.vitamui.com)"
  state="$(container_state)"

  info "${BOLD}Conteneur${OFF} : $state"
  [[ "$state" == running ]] || return 0

  if curl -ksSf --max-time 3 "$origin/__vitamui/health" >/dev/null 2>&1; then
    ok "sonde $origin/__vitamui/health"
  else
    fail "sonde injoignable sur $origin/__vitamui/health"
  fi

  info ""
  info "${BOLD}Applications${OFF}"
  printf '  %-16s %s\n' portal "$origin/"
  for app in "${APPS[@]}"; do
    [[ "$app" == portal ]] && continue
    printf '  %-16s %s\n' "$app" "$origin/$app/"
  done
  info ""
  info "${BOLD}Services${OFF}"
  printf '  %-16s %s\n' cas "$origin/cas/login"
  printf '  %-16s %s\n' apis "$origin/<app>-api/..."

  local overrides
  overrides="$(find ng-serve -name '*.conf' -printf '%f\n' 2>/dev/null | sed 's/\.conf$//' | paste -sd' ' -)"
  if [[ -n "$overrides" ]]; then
    info ""
    warn "surcharges ng serve actives : $overrides"
  fi
}

cmd_ng_serve() {
  local app="${1:-}"

  if [[ "$app" == "--clear" ]]; then
    local target="${2:-}"
    if [[ -n "$target" ]]; then
      rm -f "ng-serve/$target.conf"
      ok "surcharge ng serve retiree pour $target"
    else
      rm -f ng-serve/*.conf
      ok "toutes les surcharges ng serve retirees"
    fi
    [[ "$(container_state)" == running ]] && cmd_reload
    return 0
  fi

  [[ -n "$app" ]] || die "usage : $0 ng-serve <app> [port]  |  $0 ng-serve --clear [app]"
  [[ -n "${NG_PORTS[$app]:-}" ]] || die "application inconnue : $app (attendu : ${!NG_PORTS[*]})"

  local port="${2:-${NG_PORTS[$app]}}"
  local server_name path
  server_name="$(env_value VITAMUI_SERVER_NAME dev.vitamui.com)"
  path="/$app/"
  [[ "$app" == portal ]] && path="/"

  cat > "ng-serve/$app.conf" <<EOF
## Genere par vitamui-nginx.sh — ne pas editer a la main.
##
## Route $path vers le serveur de dev Angular de « $app » au lieu du dist/.
## Le modificateur ^~ court-circuite les locations regex de vitamui.conf.
##
## Prerequis cote Angular : le serveur de dev doit servir sous $path, ce que
## garantit la cle "servePath" de ui/ui-frontend/angular.json.
##
##   cd ui/ui-frontend && npm run start:$app
location ^~ $path {
    proxy_pass https://$server_name:$port;

    proxy_ssl_verify      off;
    proxy_ssl_server_name on;

    ## Rechargement a chaud : le client vite ouvre un websocket sous $path.
    proxy_http_version 1.1;
    proxy_set_header   Upgrade \$http_upgrade;
    proxy_set_header   Connection \$vitamui_connection_upgrade;
    proxy_read_timeout 86400s;

    include /etc/nginx/snippets/proxy_params.conf;
}
EOF

  ok "surcharge ng serve ecrite : $path -> https://$server_name:$port"
  if [[ "$(container_state)" == running ]]; then
    cmd_reload
  else
    info "    (nginx n'est pas demarre — elle sera prise en compte au prochain up)"
  fi
}

usage() {
  cat <<'EOF'
Reverse proxy de developpement VitamUI — origine unique pour tous les fronts.

  up                      verifie les prerequis puis demarre le conteneur
  down                    arrete et supprime le conteneur
  restart                 redemarre le conteneur
  reload                  recharge la configuration sans coupure
  status                  etat du conteneur, sonde, table des URLs
  logs [-f]               journaux du conteneur
  check                   valide la configuration rendue (nginx -t)

  ng-serve <app> [port]   route /<app>/ vers un `ng serve` au lieu du dist/
  ng-serve --clear [app]  retire la ou les surcharges ng serve

Applications : portal identity referential ingest archive-search collect pastis design-system
EOF
}

main() {
  local cmd="${1:-}"
  shift || true

  case "$cmd" in
    up|down|restart|reload|status|logs|check|ng-serve) select_docker_context ;;
  esac

  case "$cmd" in
    up)       cmd_up ;;
    down)     cmd_down ;;
    restart)  cmd_restart ;;
    reload)   cmd_reload ;;
    status)   cmd_status ;;
    logs)     cmd_logs "$@" ;;
    check)    cmd_check ;;
    ng-serve) cmd_ng_serve "$@" ;;
    ''|-h|--help|help) usage ;;
    *)        usage; exit 1 ;;
  esac
}

main "$@"
