# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Vitam-UI is a full-stack application for managing digital archives (archives numériques). It consists of:
- **Backend**: Java 21 + Spring Boot 4.0.6 (Maven multi-module)
- **Frontend**: Angular 21 (multi-project workspace) + vitamui-library (shared component library)
- **Authentication**: CAS Server (Apereo CAS)
- **Database**: MongoDB
- **Target**: French government archival system (VITAM project)

## Build Commands

### Backend (Maven)

```bash
# Build all Java modules (no frontend)
mvn clean install

# Build with frontend in dev mode (no optimization, headless Chrome tests)
mvn clean install -Pdev

# Build with frontend for production (optimized)
mvn clean install -Pprod

# Skip tests
mvn clean install -DskipTests

# Run a single backend module (from its directory)
cd api/api-iam/iam && mvn clean spring-boot:run

# Run with a specific profile
mvn clean spring-boot:run -Pdev
```

### Frontend (Angular)

```bash
# Install dependencies
cd ui/ui-frontend && npm install

# Start individual apps (requires SSL certs in dev-deployment/)
npm run start:portal        # Port 4200
npm run start:identity       # Port 4201
npm run start:ingest         # Port 4202
npm run start:archive-search # Port 4203
npm run start:referential    # Port 4204
npm run start:collect        # Port 4205
npm run start:pastis         # Port 4206
npm run start:design-system  # Port 4207

# Build individual apps
npm run build:portal
npm run build:identity
# etc.

# Run tests for a single app
npm run test:portal
npm run test:identity
# etc.

# Lint a single app
npm run lint:portal
npm run lint:identity
# etc.

# Build shared library
npm run build:vitamui-library
```

## Architecture

### Backend Module Structure

Each API module (e.g., `api-iam`, `api-ingest`) follows this pattern:
- `iam/` - Main Spring Boot application (server)
- `iam-client/` - Client library for other services to call this API
- `iam-commons/` - Shared models/DTOs between client and server
- `iam-security/` - Security-specific code (for IAM module)

Root modules:
- `commons/` - Shared libraries (api, rest, mongo, security, vitam, utils, test, logbook)
- `api/` - Backend API services (iam, ingest, archive-search, referential, security, gateway, pastis, collect)
- `cas/` - CAS authentication server
- `bom/` - Bill of Materials (dependency versions)

### Frontend Structure

`ui/ui-frontend/` is an Angular multi-project workspace:
- `projects/portal/` - Main portal app
- `projects/identity/` - User/identity management
- `projects/ingest/` - Archive ingestion
- `projects/archive-search/` - Archive search
- `projects/referential/` - Reference data management
- `projects/collect/` - Collection management
- `projects/pastis/` - PASTIS app
- `projects/design-system/` - Component documentation
- `projects/vitamui-library/` - Shared Angular component library (built separately with `ng build vitamui-library`)

### Key Patterns

- **API clients**: Generated/defined in `*-client` modules using OpenAPI. Clients connect to VITAM backend services.
- **Security**: CAS-based SSO. `api-security` module handles auth. Frontend uses `angular-oauth2-oidc`.
- **MongoDB**: Used for persistence. `commons-mongo` provides base repository/entity classes.
- **VITAM integration**: `commons-vitam` module wraps VITAM API calls. Backend services translate between Vitam-UI domain and VITAM APIs.
- **Frontend proxy**: `proxy.conf.js` in `ui/ui-frontend/` proxies API calls to backend services during development.

### Configuration

- Backend configs: `src/main/resources/application.yml` and `application-dev.yml` in each API module
- Frontend environments: `projects/<app>/src/environments/environment.ts`
- Dev certs: `dev-deployment/environments/certs/` (required for HTTPS in development)

## Development Workflow

### Running Full Stack Locally

1. Start MongoDB: `cd tools/docker/mongo && ./restart_dev.sh`
2. Start SMTP (optional): `cd tools/docker/mail && ./start.sh`
3. Start CAS: `cd cas/cas-server && ./run.sh`
4. Start backend APIs (in order): security → iam → others
5. Start frontend apps: `npm run start:<app>`

### Prerequisites for Dev

- JDK 21
- Maven 3.9+
- Node.js (version in `.nvmrc`: 21)
- MongoDB running locally
- Add `127.0.0.1 dev.vitamui.com` to `/etc/hosts`
- Install SSL certs in browser from `dev-deployment/environments/certs/`

## Testing

### Backend Tests

```bash
# Run all backend tests
mvn clean test

# Run tests for a single module
cd api/api-iam/iam && mvn test

# Integration tests (requires full environment)
mvn clean verify -Pdev-it        # Development environment
mvn clean verify -Pintegration   # Jenkins environment
```

### Frontend Tests

```bash
cd ui/ui-frontend

# Run all tests (headless)
npm test

# Run tests for a specific app
npm run test:portal

# Watch mode for a specific app
npm run test:identity -- --watch
```

## Code Style

- **Java**: Spotless with Prettier Java plugin (120 char line length, 4-space indent). Run `mvn spotless:apply` to auto-format.
- **TypeScript/HTML**: ESLint + Prettier. Run `npm run prettier` to format.
- **License headers**: All Java and TS files require license headers (checked by `license-maven-plugin`).

## Maven Profiles

- `dev` - Full build with frontend, dev optimizations
- `prod` - Full build with frontend, production optimizations
- `vitam` - For internal Vitam developers (uses private Nexus repos)
- `skipTestsRun` - Automatically activated with `-DskipTests`
- `sonar` - Generate SonarQube reports
- `swagger` - Generate Swagger JSON files

## Important Notes

- Frontend apps run on HTTPS with self-signed certs in dev. Browser must trust certs from `dev-deployment/environments/certs/`.
- Backend APIs expect `dev.vitamui.com` hostname (configure in `/etc/hosts`).
- CAS server must be running before other backend services (it handles authentication).
- The `vitamui-library` must be built before running frontend apps that depend on it: `npm run build:vitamui-library`
- Angular apps use `--openssl-legacy-provider` flag for Node.js compatibility.
