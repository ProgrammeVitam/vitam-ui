# Flow App

This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 1.7.2.

## Development server

Run `ng serve` for a dev server. Navigate to `http://localhost:4204/`. The app will automatically reload if you change any of the source files.

## Code scaffolding

Run `ng generate component component-name` to generate a new component. You can also use `ng generate directive|pipe|service|class|guard|interface|enum|module`.

## Build

Run `ng build` to build the project. The build artifacts will be stored in the `dist/` directory. Use the `-prod` flag for a production build.

## Running unit tests

Run `ng test` to execute the unit tests via [Karma](https://karma-runner.github.io).

## Running end-to-end tests

Run `ng e2e` to execute the end-to-end tests via [Protractor](http://www.protractortest.org/).

## Further help

To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI README](https://github.com/angular/angular-cli/blob/master/README.md).

## IDE configuration

### VSCode

#### Recommended Extensions for VSCode

- "dbaeumer.vscode-eslint" : for more information: // ESLint, https://marketplace.visualstudio.com/items?itemName=dbaeumer.vscode-eslint.
- "esbenp.prettier-vscode" : for more information: // Code formater Prettier, https://marketplace.visualstudio.com/items?itemName=esbenp.prettier-vscode
- "sibiraj-s.vscode-scss-formatter" : for more information: // SCSS Formatter, https://marketplace.visualstudio.com/items?itemName=sibiraj-s.vscode-scss-formatter
- "msjsdiag.debugger-for-chrome": for more information: // Debugger for Chrome, https://marketplace.visualstudio.com/items?itemName=msjsdiag.debugger-for-chrome
- "firefox-devtools.vscode-firefox-debug" : for more information: // Debugger for Firefox, https://marketplace.visualstudio.com/items?itemName=firefox-devtools.vscode-firefox-debug

#### VSCode settings

Bellow, an example to manage code format for Visual studio code using `settings.json` file:

```json5
{
  "editor.formatOnSave": true,
  "[typescript]": {
    "editor.codeActionsOnSave": {
      "source.fixall.eslint": "explicit",
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

## How to add icons to the library

Go to [icomoon.io](https://icomoon.io/app/#/select).

Click on `Import icons` and select the `icomoon-selection.json` file located in the `ui/ui-frontend/projects/vitamui-library/icomoon-selection.json` folder. And then, click on `Yes` when asked if you would you like to load all the settings stored in your selection file.

A `vitamui-icon` icon set should appear on the screen. From here you can select/deselect icons, rename, delete or add new icons.

Drag and drop your icon that you want to add, then select them and remove the color

Once you made the desired modifications, you can generate the font by clicking `Generate Font` at the bottom right corner of the screen. It will show a preview of the icons. Click again at the bottom right of the screen, on the `Download` button.

Automatic Method :
Run the import script: `./tools/import-icon.sh path/to/exported/icomoon-folder` with zip file (or unzipped directory) in parameter

Manual Method :
Now extract the archive you just downloaded and copy the content of the `fonts` folder and put it in the `src/sass/icons/fonts` folder of the project.

Update the `icomoon-selection.json` file : copy the `selection.json` file from the archive and rename it to replace the `icomoon-selection.json` file in the `ui/ui-frontend/projects/vitamui-library` folder of this project.

You also need to update the `vitamui-icons.css` file. Open the `style.css` from your downloaded archive and copy everything from line `27` to the end of the file. Now open `src/sass/icons/vitamui-icons.css` and replace the same portion of code.

> Attention: Do not override the first part of the file (from line `1` to line `25`)

Copy the `vitamui-icons.css` file and `fonts` directory to `cas-server/src/main/resources/static/icons`

## How to add application icons to the portal

Place you new svg icon file in the `ui/ui-frontend/projects/portal/src/assets/app-icons` folder.

> Attention: Make sure your svg icon file name corresponds to the concerned application identifier (ex: PORTAL_APP.svg), you can find your application identifier by checking database.

Replace each static hex/rgb colors inside the svg file by the right css variable theme colors.

Replace each css class ids (for example .stXXX) and svgids (for example #SVGID_XX) by unique identifier compared to the others svg.

> Attention: If an other application svg have the same class identifiers, your svg will not be displayed properly. **Also, do not forget to rename the css class & svgids usage in the whole file**.

## Standalone Profile

The standalone profile is used to build the pastis project ignoring other ui-frontend projects :

- api/api-pastis/pastis-standalone/src/main/resources/standalone/package.json -> package.json
- api/api-pastis/pastis-standalone/src/main/resources/standalone/angular.json -> angular.json
