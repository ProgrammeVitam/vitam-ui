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
  echo "node not found (install Node >= 18)" >&2
  exit 1
fi
major=$(node -p 'process.versions.node.split(".")[0]')
if [[ "$major" -lt 18 ]]; then
  echo "Node >= 18 required, found $(node -v)" >&2
  exit 1
fi

# server.mjs reports a port already in use, on the address it actually binds (CAS_MOCK_HOST).
exec node server.mjs "$@"
