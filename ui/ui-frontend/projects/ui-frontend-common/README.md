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

## Further help

To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI README](https://github.com/angular/angular-cli/blob/master/README.md).

## IDE configuration

### VSCode

#### Recommended Extensions for VSCode

- "ms-vscode.vscode-typescript-tslint-plugin" : for more information: // TsLinst, https://marketplace.visualstudio.com/items?itemName=ms-vscode.
- "esbenp.prettier-vscode" : for more information: // Code formater Prettier, https://marketplace.visualstudio.com/items?itemName=esbenp.prettier-vscode
- "sibiraj-s.vscode-scss-formatter" : for more information: // SCSS Formatter, https://marketplace.visualstudio.com/items?itemName=sibiraj-s.vscode-scss-formatter
- "msjsdiag.debugger-for-chrome": for more information: // Debbuger for Chrome, https://marketplace.visualstudio.com/items?itemName=msjsdiag.debugger-for-chrome
- "firefox-devtools.vscode-firefox-debug" : for more information: // Debbuger for Firefox, https://marketplace.visualstudio.com/items?itemName=firefox-devtools.vscode-firefox-debug

#### VSCode settings

Bellow, an example to manage code format for Visual studio code using `settings.json` file:

```json5
{
  "editor.formatOnSave": true,
  "[typescript]": {
    "editor.codeActionsOnSave": {
      "source.fixAll.tslint": true,
      "source.organizeImports": true
    },
    "editor.defaultFormatter": "esbenp.prettier-vscode",
  },
  "[html]": {
    "editor.defaultFormatter": "esbenp.prettier-vscode",
  },
  "[scss]": {
    "editor.defaultFormatter": "esbenp.prettier-vscode",
  },
  "search.exclude": {
    "**/node_modules": true,
    "**/dist": true,
  },
}
```

### IntelliJ

- Enable "Automatic Prettier configuration", set `**/*.{js,ts,html,json,md,scss}` in "Run for files" and check the "Run on save" checkbox

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

Run `npm version X.X.X` or `npm version patch`

Run `npm run check` to check the lint and tests and build the package.

Run `npm run packagr:tar` to build the package npm.

Run `./install_local.sh` to install the local package to ui-frontend.

Test your new version in all the apps and check that everything is okay. Make changes to the applications if necessary.

Use the right npmrc for common publication to publish in npm-private-release.

Run `npm run publish-dist` to publish on the nexus repository.

Commit the changes to the `ui-frontend-common` folder only then tag that commit like so `git tag ui-frontend-common@X.X.X`.

Run `./install_from_repo.sh <version>` to install your freshly published version from the nexus on the 4 apps.

Make another commit with the changes made to the apps as well as the incremented version number in the `package.json`. 

Delete the older version of ui-frontend-common tgz that is no longer used and add the new generated one.

Push your commits and your tag.

Reset the old npmrc if necessary.


## Standalone Profile

The standalone profile is used to build the ui-frontend-common project ignoring all services that require authentication, replacing the following files:

- api/api-pastis/pastis-standalone/src/main/resources/standalone/startup.service.ts -> src/app/modules/startup.service.ts
- api/api-pastis/pastis-standalone/src/main/resources/standalone/theme.service.ts -> src/app/modules/theme.service.ts
- api/api-pastis/pastis-standalone/src/main/resources/standalone/app.configuration.interface.ts -> src/app/modules/models/app.configuration.interface.ts
