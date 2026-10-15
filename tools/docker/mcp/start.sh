#!/bin/bash
# Starts the MCP gateway shared by all Claude Code sessions (see README.md).
set -e
cd "$(dirname "$0")"

WORKSPACE="$(cd ../../.. && pwd -P)"
SETTINGS="$WORKSPACE/.claude/settings.local.json"
PORT="${MCP_GATEWAY_PORT:-8811}"

# Secrets are read from the "env" block of the Claude Code local settings (git-ignored),
# which also provides them to .mcp.json and to the MCP servers started by Claude Code
setting() {
  [ -f "$SETTINGS" ] && python3 -c 'import json, sys; print(json.load(open(sys.argv[1])).get("env", {}).get(sys.argv[2], ""))' "$SETTINGS" "$1"
}
SONARQUBE_TOKEN="$(setting SONARQUBE_TOKEN)"
MCP_GATEWAY_AUTH_TOKEN="$(setting MCP_GATEWAY_AUTH_TOKEN)"
if [ -z "$SONARQUBE_TOKEN" ] || [ -z "$MCP_GATEWAY_AUTH_TOKEN" ]; then
  echo "Set SONARQUBE_TOKEN and MCP_GATEWAY_AUTH_TOKEN in the \"env\" block of $SETTINGS (see README.md)," >&2
  echo "for instance MCP_GATEWAY_AUTH_TOKEN=$(openssl rand -hex 24), then run this script again." >&2
  exit 1
fi

# Generated files: gateway configuration and secrets
mkdir -p runtime
printf 'angular-cli:\n  workspace: %s\n  user: "%s"\n' "$WORKSPACE" "$(id -u):$(id -g)" > runtime/config.yaml
(umask 077 && printf 'sonarcloud.token=%s\n' "$SONARQUBE_TOKEN" > runtime/secrets.env)

docker rm -f vitamui-mcp-gateway > /dev/null 2>&1 || true
docker pull -q mcp/sonarqube > /dev/null
docker pull -q node:22-slim > /dev/null
docker run --name vitamui-mcp-gateway -d --restart unless-stopped \
  -p "127.0.0.1:$PORT:8811" \
  -e MCP_GATEWAY_AUTH_TOKEN="$MCP_GATEWAY_AUTH_TOKEN" \
  -e MCP_GATEWAY_DOCKER_BIND_ALLOW_WRITABLE_PATHS="$WORKSPACE" \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v "$(pwd)/catalog.yaml:/root/.docker/mcp/catalogs/vitam-ui.yaml:ro" \
  -v "$(pwd)/runtime/config.yaml:/root/.docker/mcp/config.yaml:ro" \
  -v "$(pwd)/runtime/secrets.env:/run/secrets/mcp.env:ro" \
  docker/mcp-gateway:latest \
  --transport streaming --port 8811 \
  --catalog vitam-ui.yaml --config config.yaml --secrets /run/secrets/mcp.env \
  --servers sonarcloud,angular-cli \
  --verify-signatures=false --block-secrets=false > /dev/null

echo "vitamui-mcp-gateway is started on http://localhost:$PORT/mcp"
