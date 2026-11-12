# VitamuiLibrary

Shared Angular library used by all the VITAM-UI front-end applications.

It is never built nor published on its own: the `vitamui-library` and `vitamui-library/testing` path aliases of the root
`tsconfig.json` point to its sources (`src/public-api.ts` and `testing/src/public_api.ts`), which each application
compiles directly. No build or watch of the library is needed before starting or building an application.

Applications must import it only through these public entry points (enforced by ESLint).

## Code scaffolding

Run `ng generate component component-name --project vitamui-library` to generate a new component. You can also use `ng generate directive|pipe|service|class|guard|interface|enum|module --project vitamui-library`.

> Note: Don't forget to add `--project vitamui-library` or else it will be added to the default project in your `angular.json` file.

## Running unit tests

Run `npm run test:vitamui-library` to execute the unit tests.

## Linting

Run `npm run lint:vitamui-library`.
