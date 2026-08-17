# Development reverse proxy

All VitamUI applications under a single origin, as in deployed environments:
portal at `https://dev.vitamui.com/`, the other apps under `/<app>/`, the APIs
under `/<app>-api/` (api-gateway, mTLS), CAS under `/cas/`.

## Prerequisites

1. `127.0.0.1    dev.vitamui.com` in `/etc/hosts`.
2. Dev certificates generated: `cd dev-deployment && ./generate_certs.sh`.
3. api-gateway on `127.0.0.1:8070` and cas-server on `127.0.0.1:8080`.
4. Frontends available: dev server started, or dists built — see the two
   sections below.

## Commands

```bash
cd tools/docker/nginx

./vitamui-nginx.sh up        # checks prerequisites, starts
./vitamui-nginx.sh status    # state, probe, URL table with live/dist per app
./vitamui-nginx.sh down
```

Overrides: copy `.env.example` to `.env` (git-ignored).

## Building the frontends

```bash
cd ui/ui-frontend
npm run build:allModulesDev      # ✔ all apps — or one: npm run build:identity:dev
npm run build:allModules         # ✘ NOT for local use (production build)
```

A production build (`build:allModules`, `build:<app>`) excludes
`config-dev.json` by design (ansible provides the configuration on deployed
environments). Served locally it fails like this:

```
GET https://dev.vitamui.com/<app>/assets/config.json      404
GET https://dev.vitamui.com/<app>/<app>-api/...           401
```

If you see this, rebuild with the `:dev` script. `./vitamui-nginx.sh up` also
warns about it (`dist/ without config-dev.json`).

## Live / dist

Each application can be served from two sources, and nginx picks one
automatically on every request:

- **live** — the app's Angular dev server (`npm run start:<app>`) is running:
  nginx proxies to it, with hot reload;
- **dist** — no dev server on the app's port: nginx serves the built files
  from `ui/ui-frontend/dist/<app>/`.

Nothing to configure or toggle — start a dev server and the app goes live at
the next page load, stop it (Ctrl+C) and it falls back to its dist:

```bash
cd ui/ui-frontend && npm run start:archive-search   # archive-search goes live
```

The URL never changes, other apps are unaffected, and API calls are always
routed by nginx to the api-gateway (the dev server never sees them).
`./vitamui-nginx.sh status` shows the current mode of each app: `live (:port)`,
`dist`, or `no dist (404)` when the app has neither.

## Tests

Non-regression tests, to run when modifying the nginx configuration:

```bash
./tests/run-tests.sh            # isolated stack + stub upstreams, ~104 checks
```
