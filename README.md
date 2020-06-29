# VitamUI
VitamUI project.

# Prerequisites
- Install Java at least version 8
- Install Maven
- Install Git
- Install NodeJs
- Configure default registry:  npm config set registry https://registry.npmjs.org/
- Environment variable :
    - Vitam developer
        - Specify the environment variables : SERVICE_NEXUS_URL and SERVICE_REPOSITORY_URL
        - The maven command. Params between {} are optional: mvn clean install {-Ddependency-check.skip=true} -Denv.SERVICE_NEXUS_URL=... -Denv.SERVICE_REPOSITORY_URL=... {-DskipTests} -Pvitam
    - Non vitam developer
        - Build vitam locally @see  https://github.com/ProgrammeVitam/vitam/#id11


# Clone
Execute this command to clone the project from the bitbucket repo:

    git clone https://github.com/vitam-prg/vitamUI.git

# Global Maven profiles

Without a profile, only Java projects are build.
In order to build and package UI projects (i.e. Java backend & Angular frontend altogether), we use the plugin `frontend-maven-plugin` provided by `com.github.eirslett`.

## dev
This profile is used to build the entire project for dev purposes, backend & frontend included.
* UI modules are packaged with both Java & Angular.
* Angular projects are build without optimization in order to reduce global build time.
* Jasmine Karma tests are launched with the headless chrome.


## prod
This profile is used to build the entire project for prod purposes, backend/frontend included.
* UI modules are packaged with both Java & Angular.
* Angular projects are build with optimization.
* Jasmine Karma tests are launched with the headless chrome.


## npm-publish
This profile is used to build, test & push npm packages to the npm repository.
* It should be used in ui/ui-frontend-common to push the npm package of the common UI library.


## rpm
This profile is used to build rpm packages. Only Maven modules with `rpm.skip = false` in their properties are eligible.


## rpm-publish
This profile is used to push the generated rpm package. Only Maven modules with `rpm.skip = false` in their properties are eligible.


## skipTestsRun
This profile is automatically activated if the option `-DskipTests` is used during Maven execution in order to disable Jasmine Karma tests execution.


## sonar
This profile is used to update sonar informations.


## webpack
This profile is used to build the entire project, backend & frontend included.
* Angular projects are build without optimization in order to reduce global build time.
* Jasmine Karma tests are launched with the headless chrome.
* Jenkins can use this profile.

## swagger
This profile is used to generate the swagger.json draft file for swagger documentation. It's only needed for API modules.

## swagger-docs
This profile is used to generate .html & .pdf swagger documentation in tools/swagger/docs/.
* Only Maven modules with 'rpm.skip = false' in their properties are eligible.

## vitam
Profile to use for all Vitam internal developers


# Integration Tests Maven profiles

No integration test is launched during the _“normal”_ build of the project.
Integration tests need a full running environnement in order to launch API tests & few UI tests also.

## integration
This profile should be used to launch integration tests in Jenkins. The configuration used for the tests is available in `integration-tests/src/test/resources/application-integration.yml`

## dev-it
This profile should be used to launch integration tests in our development environment. The configuration used for the tests is available in `integration-tests/src/test/resources/application-dev.yml`

## iam
This profile should be used to launch API IAM integration tests in our development environment. The configuration used for the tests is available in `integration-tests/src/test/resources/application-dev.yml`

## security
This profile should be used to launch API Security integration tests in our development environment. The configuration used for the tests is available in `integration-tests/src/test/resources/application-dev.yml`

## front
This profile should be used to launch UI integration tests in our development environment. The configuration used for the tests is available in `integration-tests/src/test/resources/application-dev.yml`


# Build (first build)
Publish ui-frontend-common package. It's needed for angular projects ui-portal & ui-identity to compile.
Execute this command to build the project with unit tests and without building our angular projects:

    cd ui/ui-frontend-common;
    mvn clean install -Pnpm-publish

# Build (only Java)
Execute this command to build the project with unit tests and without building our angular projects:

    mvn clean install

## Build (only Java) without test
Execute this command to build the project without unit tests and without building our angular projects:

    mvn clean install -DskipTests

## Build with IHM (JS) in dev mode
Use the dev maven profile to build the project with our angular projects.
For our angular projects, the build doesn't generate the sourcemap and doesn't optimize the build.
For the karma tests, we don't generate the code coverage and use the headless chrome.

    mvn clean install -Pdev

## Build with IHM (JS) for our Jenkins
Use the webpack maven profile to build the project with our angular projects.
For our angular projects, the build generate the sourcemap and doesn't optimize the build.
For the karma tests, we don't generate the code coverage and use the headless chrome.

    mvn clean install -Pwebpack

## Build with IHM (JS) for our Production environment
Use the webpack maven profile to build the project with our angular projects.
For our angular projects, the build generate the sourcemap and optimize the build.
For the karma tests, we don't generate the code coverage and use the headless chrome.

    mvn clean install -Pprod


If `-DskipTests` id added during the build of dev, webpack or prod, unit tests and karma tests are both ignored.


## Integration tests
Use the integration-tests maven profile to build the project with unit tests and integration tests

    mvn clean verify -Pdev-it

For more details see [README](integration-tests/README.md) in integration-tests module.

## Package to RPM
Use the RPM maven profile to build the project and package to RPM:

    mvn clean package -Prpm,webpack

## Execute Integration tests

    mvn clean verify -Pintegration

## Execute sonar report

    mvn clean verify -Psonar
You can specify properties to change URL and login to sonar:

    mvn clean verify -Psonar \
        -Dsonar.host.url=http://localhost:9000 \
        -Dsonar.login=<TOKEN AUTHENTICATION>


## Deploy artifact
To build all artifacts and deploy to NEXUS use:

    mvn clean deploy -Prpm,webpack

## Swagger
To generate swagger.json use:

    mvn test -Pswagger

To edit swagger.json you can use this website:

    https://editor.swagger.io/

To generate index.pdf and index.html from `swagger.json`:

     mvn generate-resources -Pswagger-docs

# Lancement de VITAMUI des versions

Current version of VITAMUI depends on Ansible version 2.7.0.

In order not to interfere with more recent Ansible version, deploy a Python VirtualEnv
in which you install Ansible 2.7.0:
 * Check that VirtualEnv executable is installed: `apt-get install python-virtualenv`
 * In a directory of your choice, create the virtual environment: `virtualenv vitamUI-ansible`
 * Activate the environment `. virtamUI-ansible/bin/activate`
 * Install Ansible 2.7.0: `pip install ansible==2.7.0`
 * Check the version of ansible: `ansible --version`

Autre possibilité, Ansible version 2.7.0 doit être installé pour lancer le script mongo:
* Si une version d'ansible à été installé via apt-get install, il est nécessaire de la sésinstaller `apt-get remove ansible`
* Installer pip `apt-get install -y python-pip`
* Installer ansible via pip `pip install ansible==2.7.0`
* Vérifier que l'installation c'est bien déroulée `ansible --version`
* Il est possible que l'ajout du lien vers ansible dans le PATH et/ou qu'un redémarage soit nécessaire

### 1 - Démarrage du Mongo VITAMUI

```
├── tools
│   ├── docker
│   │   ├── mongo: './restart_dev.sh'

### 2 démarrage de la plateforme logicielle Vitam
Pour lancer [VITAM](tools/vitamui-conf-dev/README.md) en mode développement et permettre à VITAMUI d'accéder à ces APIs,
voir la [configuration](tools/vitamui-conf-dev/README.md) suivante.

### 3 - Démarrage du docker smpt4dev

```
├── tools
│   ├── docker
│   │   ├── mail: './start.sh'
```

### 4 - Lancement de l'application SpringBoot Security-Internal

```
│   ├── api-security
│   │   ├── security-internal: 'mvn clean spring-boot:run' ou './run.sh'
```

### 5 - Lancement de l'application SpringBoot IAM-Internal

```
├── api
│   ├── api-iam
│   │   ├── iam-internal: 'mvn clean spring-boot:run' ou './run.sh'
```

### 6 - Lancement de l'application SpringBoot IAM-External

```
├── api
│   ├── api-iam
│   │   ├── iam-external: 'mvn clean spring-boot:run'
                        ou './run.sh'
```

### 7 - Lancement de l'application SpringBoot Ingest-Internal

```
├── api
│   ├── api-ingest
│   │   ├── ingest-internal: 'mvn clean spring-boot:run' ou './run.sh'
```

### 8 - Lancement de l'application SpringBoot Ingest-External

```
├── api
│   ├── api-ingest
│   │   ├── ingest-external: 'mvn clean spring-boot:run' ou './run.sh'
```

### 9 - Lancement de l'application CAS Server. La surcharge faite sur CAS nous empêche de lancer avec le plugin spring-boot

**CAS-Server dépend de security-internal, iam-internal & iam-external**

```
├── cas
│   ├── cas-server: './run.sh'
```

## Scénario 1 : utilisation en dev

### 10a - Lancement de l'application SpringBoot correspondant au back de UI-Portal

```
└── ui
    └── ui-portal: 'mvn clean spring-boot:run'
```

### 10b - Lancement de l'application Angular UI-Portal

```
└── ui
    ├── ui-frontend: 'npm run start:portal'
```

### 11a - Lancement de l'application SpringBoot correspondant au back de UI-Identity

```
└── ui
    └── ui-identity: 'mvn clean spring-boot:run'
```

### 11b - Lancement de l'application Angular UI-Identity

```
└── ui
    ├── ui-frontend: 'npm run start:identity'
```

### 12a - Lancement de l'application SpringBoot correspondant au back de UI-Ingest

```
└── ui
    └── ui-ingest: 'mvn clean spring-boot:run'
```

### 12b - Lancement de l'application Angular UI-Ingest

```
└── ui
    ├── ui-frontend: 'npm run start:ingest'
```

## Scénario 2 : utilisation en mode recette : Une compilation avec `-Pwebpack` a été effectuée.
**Attention les JAR doivent contenir les pages et scripts de la partie UI Frontend généré avec ng build.**


### 13 - Lancement de l'application SpringBoot correspondant au back de UI-Portal

```
└── ui
    └── ui-portal: './run.sh'
```

### 14 - Lancement de l'application SpringBoot correspondant au back de UI-Identity

```
└── ui
    └── ui-identity : './run.sh'
```

### 15 - Lancement de l'application SpringBoot correspondant au back de UI-Ingest

```
└── ui
    └── ui-ingest : './run.sh'
```

### 16. Les certificats sont auso-signés, il faut accepter les certificats dans le navigateur pour :

**Attention : sans cette étape, le logout sur toutes les applications par CAS ne fonctionne pas**

UI-Frontend

* https://dev.vitamui.com:4200

* https://dev.vitamui.com:4201/user

* https://dev-ingest.vitamui.com:4202

Ui-Back

* https://dev.vitamui.com:9000/

* https://dev.vitamui.com:9001/

* https://dev-ingest.vitamui.com:9008/

### 17. Se connecter sur le portail via
* https://dev.vitamui.com:4200

### 18. Se connecter sur la page de réception des mails smpt4dev via
* http://localhost:3000/
