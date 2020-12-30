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

## How to add icons to the library

Go to [icomoon.io](https://icomoon.io/app/#/select).

Click on `Import icons` and select the `icomoon-selection.json` file located in the `ui/ui-frontend-common/icomoon-selection.json` folder. And then, click on `Yes` when asked if you would you like to load all the settings stored in your selection file.

A `vitamui-icon` icon set should appear on the screen. From here you can select/deselect icons, rename, delete or add new icons.

Drag and drop your icon that you want to add, then select them and remove the color

Once you made the desired modifications, you can generate the font by clicking `Generate Font` at the bottom right corner of the screen. It will show a preview of the icons. Click again at the bottom right of the screen, on the `Download` button.

Automatic Method :
Go to commons project's directory: `cd ui/ui-frontend-common`
Run the import script: `./import-icons.sh path/to/exported/icomoon-folder`

Manual Method :
Now extract the archive you just downloaded and copy the content of the `fonts` folder and put it in the `src/sass/icons/fonts` folder of the project.

Update the `icomoon-selection.json` file : copy the `selection.json` file from the archive and rename it to replace the `icomoon-selection.json` file in the `ui/ui-frontend-common` folder of this project.

You also need to update the `vitamui-icons.css` file. Open the `style.css` from your downloaded archive and copy everything from line `27` to the end of the file. Now open `src/sass/icons/vitamui-icons.css` and replace the same portion of code.

> Attention: Do not override the first part of the file (from line `1` to line `25`)

Please add each new icon to the icon category in the starter-kit project.
