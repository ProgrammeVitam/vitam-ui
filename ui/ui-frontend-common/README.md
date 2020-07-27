# Angular Global info

This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 8.3.3.

## Development server

Run `ng serve` for a dev server. Navigate to `http://localhost:4200/`. The app will automatically reload if you change any of the source files.

## Code scaffolding

Run `ng generate component component-name` to generate a new component. You can also use `ng generate directive|pipe|service|class|guard|interface|enum|module`.

## Build

Run `ng build` to build the project. The build artifacts will be stored in the `dist/` directory. Use the `--prod` flag for a production build.

## Running unit tests

Run `ng test` to execute the unit tests via [Karma](https://karma-runner.github.io).

## Running end-to-end tests

Run `ng e2e` to execute the end-to-end tests via [Protractor](http://www.protractortest.org/).

## Further help

To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI README](https://github.com/angular/angular-cli/blob/master/README.md).

# Common lib specific info

## Usage

import variables with `@import "~@vitamui/common/sass/variables"` (contain colors variables)

import mixins with `@import "~@vitamui/common/sass/mixins"` (contain fonts mixins)

import theme with `@import "~@vitamui/common/sass/theme"`

## Developing new features or fixing bugs in angular common

In order to test the modifications you made to the angular-commons library without publishing to the nexus repo, you can run `npm run packagr:tar` to generate a `ui-frontend-common-X.X.X.tgz` tarball.

You can now import your new `ui-frontend-common` package in your application by running `npm install ../ui-frontend-common/ui-frontend-common-X.X.X.tgz` (relative path from ui-frontend folder - path depends on where your app is located)

Once you've tested your library and everything works fine, you can publish the package (see below).

## Package & Publish a new version

Run `npm version X.X.X` or `npm run version patch`

Run `npm run build` to build the package.

Run `npm run packagr:tar` to build the package npm.

Run `./install_local.sh` to install the local package to ui-frontend.

Test your new version in all the apps and check that everything is okay. Make changes to the applications if necessary.

Run `npm publish dist` to publish on the nexus repository.

Commit the changes to the `ui-frontend-common` folder only then tag that commit like so `git tag ui-frontend-commons@X.X.X`.

Run `./install_from_repo.sh <version>` to install your freshly published version from the nexus on the 4 apps.

Make another commit with the changes made to the apps as well as the incremented version number in the `package.json`.

Push your commits and your tag.
