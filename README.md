# VitamUI
# Prerequisites
## Tools
- Install JDK version >= 8
- Install Maven
    - Run [this script](https://github.com/ProgrammeVitam/vitam/blob/b1b7bb6e8ee83e9e747a9849b457824af650cd16/vitam-conf-dev/scripts/maven-setup-chapelle-edition.sh) to set it up
- Install Git
- Install Node.js and npm (with nvm)
    - Configure default registry: `npm config set registry https://registry.npmjs.org/`
- Python version 2.7 + pip for Python 2
- Install Ansible (see [Ansible](#Ansible))

### Ansible
Current version of VitamUI depends on Ansible version 2.7.0 in order to run installation scripts.

#### With VirtualEnv
In order not to interfere with more recent Ansible version, deploy a Python VirtualEnv
in which you install Ansible 2.7.0:
* Check that VirtualEnv executable is installed: `apt-get install python-virtualenv`
* In a directory of your choice, create the virtual environment: `virtualenv vitamUI-ansible`
* Activate the environment: `vitamUI-ansible/bin/activate`

#### Without VirtualEnv
* First remove older versions of Ansible before re-installing it:
`pip uninstall ansible`.
* Si une version d'ansible à été installée via `apt-get install`, il est nécessaire de la désinstaller : `apt-get remove ansible`

#### Common steps
* Install Ansible 2.7.0: `pip install ansible==2.7.0`
* Check the version of ansible: `ansible --version`

Il est possible que l'ajout du lien vers ansible dans le PATH et/ou qu'un redémarage soit nécessaire

## Configuration
- Clone project and change ownership: `sudo chown -R $USER vitam-ui/`
- Build project using the "right" profile (see [Maven profiles](#Maven-profiles) and [Build](#Build))

### For Vitam internal developers
- Build project using `vitam` profile (see [Build for Vitam developers
](#Build-for-Vitam-internal-developers))
- Set up environment variables : `SERVICE_NEXUS_URL` and `SERVICE_REPOSITORY_URL`
- Copy files: `access-external-client.conf`, `ingest-external-client.conf`, `keystore_ihm-demo.p12` and `truststore_ihm-demo.jks` into `api/api-(iam|referential)/(iam|referential)-internal/src/main/config/dev-vitam/`
- Redirect `dev.vitamui.com` URL defined in code to `localhost` : add this line `127.0.0.1       dev.vitamui.com` to your `hosts` (`/etc/hosts`) file

### For non Vitam developers
- [Build Vitam locally](https://github.com/ProgrammeVitam/vitam/#id11)

## Common errors
`/bin/sh: 1: /usr/bin/python: not found`
=> Create symlink, for instance:
`sudo ln -s /usr/bin/python2.7 /usr/bin/python`

# Maven profiles
Without a profile, only Java projects are build.

## Global Maven profiles
In order to build and package UI projects (i.e. Java backend & Angular frontend altogether), we use the plugin `frontend-maven-plugin` provided by `com.github.eirslett`.

### dev
This profile is used to build the entire project for dev purposes, backend & frontend included.
* UI modules are packaged with both Java & Angular.
* Angular projects are built without optimization in order to reduce global build time.
* Jasmine Karma tests are launched with the headless chrome.

### prod
This profile is used to build the entire project for prod purposes, backend/frontend included.
* UI modules are packaged with both Java & Angular.
* Angular projects are built with optimization.
* Jasmine Karma tests are launched with the headless chrome.

### npm-publish
This profile is used to build, test & push npm packages to the npm repository.

It should be used in ui/ui-frontend-common to push the npm package of the common UI library.

### rpm
This profile is used to build rpm packages.

Only Maven modules with `rpm.skip = false` in their properties are eligible.

### rpm-publish
This profile is used to push the generated rpm package.

Only Maven modules with `rpm.skip = false` in their properties are eligible.

### skipTestsRun
This profile is automatically activated if the option `-DskipTests` is used during Maven execution in order to disable Jasmine Karma tests execution.

### sonar
This profile is used to update sonar information.

### webpack
This profile is used to build the entire project, backend & frontend included.
* Angular projects are build without optimization in order to reduce global build time.
* Jasmine Karma tests are launched with the headless chrome.
* Jenkins can use this profile.

### swagger
This profile is used to generate the `swagger.json` draft file for swagger documentation. It's only needed for API modules.

### swagger-docs
This profile is used to generate `.html` & `.pdf` swagger documentation in `tools/swagger/docs/`.

Only Maven modules with `rpm.skip = false` in their properties are eligible.

### vitam
This is the profile to use for all Vitam internal developers.

## Integration Tests Maven profiles
No integration test is launched during the _“normal”_ build of the project.
Integration tests need a full running environment in order to launch API tests & few UI tests also.

### integration
This profile should be used to launch integration tests in Jenkins. The configuration used for the tests is available in `integration-tests/src/test/resources/application-integration.yml`

### dev-it
This profile should be used to launch integration tests in our development environment. The configuration used for the tests is available in `integration-tests/src/test/resources/application-dev.yml`

### iam
This profile should be used to launch API IAM integration tests in our development environment. The configuration used for the tests is available in `integration-tests/src/test/resources/application-dev.yml`

### security
This profile should be used to launch API Security integration tests in our development environment. The configuration used for the tests is available in `integration-tests/src/test/resources/application-dev.yml`

### front
This profile should be used to launch UI integration tests in our development environment. The configuration used for the tests is available in `integration-tests/src/test/resources/application-dev.yml`

# Build
## Build (first build) // DEPRECATED?
Publish ui-frontend-common package. It's needed for angular projects ui-portal & ui-identity to compile.
Execute this command to build the project with unit tests and without building our angular projects:

    cd ui/ui-frontend-common;
    mvn clean install -Pnpm-publish

## Build (only Java)
Execute this command to build the project with unit tests and without building our angular projects:

    mvn clean install

## Build (only Java) without test
Execute this command to build the project without unit tests and without building our angular projects:

    mvn clean install -DskipTests
    
## Build for Vitam internal developers

    mvn clean install [-Ddependency-check.skip=true] -Denv.SERVICE_NEXUS_URL=... -Denv.SERVICE_REPOSITORY_URL=... [-DskipTests] -Pvitam

## Build with IHM (JS) in dev mode
Use the `dev` maven profile to build the project with our angular projects.

For our angular projects, the build doesn't generate the sourcemap and doesn't optimize the build.

For the karma tests, we don't generate the code coverage and use the headless chrome.

    mvn clean install -Pdev

## Build with IHM (JS) for our Jenkins
Use the `webpack` maven profile to build the project with our angular projects.

For our angular projects, the build generate the sourcemap and doesn't optimize the build.

For the karma tests, we don't generate the code coverage and use the headless chrome.

    mvn clean install -Pwebpack

## Build with IHM (JS) for our Production environment
Use the `prod` maven profile to build the project with our angular projects.

For our angular projects, the build generate the sourcemap and optimize the build.

For the karma tests, we don't generate the code coverage and use the headless chrome.

    mvn clean install -Pprod

If `-DskipTests` id added during the build of dev, webpack or prod, unit tests and karma tests are both ignored.

## Build with integration tests for development environment
Use the `dev-it` maven profile to build the project with unit tests and integration tests.

    mvn clean verify -Pdev-it

For more details see [README](integration-tests/README.md) in integration-tests module.

## Build with integration tests for Jenkins

    mvn clean verify -Pintegration

## Package to RPM
Use the `rpm` and `webpack` maven profiles to build the project and package to RPM:

    mvn clean package -Prpm,webpack

## Execute sonar report

    mvn clean verify -Psonar
You can specify properties to change URL and login to sonar:

    mvn clean verify -Psonar \
        -Dsonar.host.url=http://localhost:9000 \
        -Dsonar.login=<TOKEN AUTHENTICATION>


## Deploy artifact
Use the `rpm` and `webpack` maven profiles to build all artifacts and deploy to NEXUS use:

    mvn clean deploy -Prpm,webpack

## Swagger
To generate `swagger.json`:

    mvn test -Pswagger
##### ATTENTION : #####
`In case you change the model part of an object or an entity in one of the projects, it is not essential to regenerate the swagger.json file, you just have to modify it manually by adding the necessary information on the modifications we made on the model part.`

To edit the file you can use [this website](https://editor.swagger.io/).

To generate `index.pdf` and `index.html` from `swagger.json`:

     mvn generate-resources -Pswagger-docs

# Run
Pour lancer [VITAM](tools/vitamui-conf-dev/README.md) en mode développement et permettre à VITAMUI d'accéder à ces APIs,
voir la [configuration](tools/vitamui-conf-dev/README.md) suivante.

## 1 - Démarrage du Mongo VitamUI
```
├── tools
│   ├── docker
│   │   ├── mongo: './restart_dev.sh'
```

## 2 - Démarrage du docker smpt4dev (facultatif)
```
├── tools
│   ├── docker
│   │   ├── mail: './start.sh'
```

## 3 - Lancement de l'application SpringBoot Security-Internal
```
├── api
│   ├── api-security
│   │   ├── security-internal: 'mvn clean spring-boot:run [-Puse-profile-here]' ou './run.sh'
```

## 4 - Lancement de l'application SpringBoot IAM-Internal
```
├── api
│   ├── api-iam
│   │   ├── iam-internal: 'mvn clean spring-boot:run [-Puse-profile-here]' ou './run.sh'
```

## 5 - Lancement de l'application SpringBoot IAM-External
```
├── api
│   ├── api-iam
│   │   ├── iam-external: 'mvn clean spring-boot:run [-Puse-profile-here]' ou './run.sh'
```

## 6 - Lancement de l'application SpringBoot Ingest-Internal

```
├── api
│   ├── api-ingest
│   │   ├── ingest-internal: 'mvn clean spring-boot:run' ou './run.sh'
```

## 7 - Lancement de l'application SpringBoot Ingest-External

```
├── api
│   ├── api-ingest
│   │   ├── ingest-external: 'mvn clean spring-boot:run' ou './run.sh'
```

## 8 - Lancement de l'application CAS Server.
La surcharge faite sur CAS nous empêche de lancer avec le plugin spring-boot

**CAS-Server dépend de security-internal, iam-internal & iam-external**

```
├── cas
│   ├── cas-server: './run.sh'
```

## Scénario 1 : utilisation en dev

### 9a - Lancement de l'application SpringBoot correspondant au back de UI-Portal

```
└── ui
    └── ui-portal: 'mvn clean spring-boot:run [-Puse-profile-here]'
```

**NB:** Profile should be `vitam` for Vitam internal developers to resolve dependency issues.

### 9b - Lancement de l'application Angular UI-Portal

```
└── ui
    ├── ui-frontend: 'npm run start:portal'
```

### 10a - Lancement de l'application SpringBoot correspondant au back de UI-Identity

```
└── ui
    └── ui-identity: 'mvn clean spring-boot:run [-Puse-profile-here]'
```

### 10b - Lancement de l'application Angular UI-Identity

```
└── ui
    ├── ui-frontend: 'npm run start:identity'
```

### 11a - Lancement de l'application SpringBoot correspondant au back de UI-Ingest

```
└── ui
    └── ui-ingest: 'mvn clean spring-boot:run'
```

### 11b - Lancement de l'application Angular UI-Ingest

```
└── ui
    ├── ui-frontend: 'npm run start:ingest'
```

## Scénario 2 : utilisation en mode recette
Une compilation avec `-Pwebpack` a été effectuée.

**Attention les JAR doivent contenir les pages et scripts de la partie UI Frontend générés avec `ng build`.**

### 9 - Lancement de l'application SpringBoot correspondant au back de UI-Portal

```
└── ui
    └── ui-portal: './run.sh'
```

### 10 - Lancement de l'application SpringBoot correspondant au back de UI-Identity

```
└── ui
    └── ui-identity : './run.sh'
```

### 11 - Lancement de l'application SpringBoot correspondant au back de UI-Ingest

```
└── ui
    └── ui-ingest : './run.sh'
```

## 12 - Les certificats sont auto-signés, il faut les accepter dans le navigateur pour :
- UI-Frontend
    - https://dev.vitamui.com:4200
    - https://dev.vitamui.com:4201/user
    - https://dev-vitamui.com:4208
- Ui-Back
    - https://dev.vitamui.com:9000/
    - https://dev.vitamui.com:9001/
    - https://dev-vitamui.com:9008

**Attention : sans cette étape, le logout sur toutes les applications par CAS ne fonctionne pas**.

## 13 - Se connecter sur le portail via :
- https://dev.vitamui.com:4200

## 14 - Se connecter sur la page de réception des mails smpt4dev via :
- http://localhost:3000/
