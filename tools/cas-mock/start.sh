#!/bin/bash
# Starts the development CAS mock in place of cas-server (port 8080).
# DEV ONLY: no password is checked. See README.md.
set -euo pipefail
cd "$(dirname "$0")"

# IDE run configurations start a non-login shell, without nvm's PATH: load it.
if ! command -v node >/dev/null 2>&1 && [[ -s "${NVM_DIR:-$HOME/.nvm}/nvm.sh" ]]; then
  # shellcheck disable=SC1091
  . "${NVM_DIR:-$HOME/.nvm}/nvm.sh"
fi
if ! command -v node >/dev/null 2>&1; then
  echo "node not found (Node >= 18 required, e.g. 'nvm use' in ui/ui-frontend)" >&2
  exit 1
fi
major=$(node -p 'process.versions.node.split(".")[0]')
if [[ "$major" -lt 18 ]]; then
  echo "Node >= 18 required, found $(node -v)" >&2
  exit 1
fi

port="${CAS_MOCK_PORT:-8080}"
if (exec 3<>"/dev/tcp/127.0.0.1/$port") 2>/dev/null; then
  echo "Port $port already in use: stop cas-server (or whatever listens there) first." >&2
  exit 1
fi

exec node server.mjs "$@"
