## But

Le projet starter-kit a pour but de référencer les classes .scss et composants existants mis à disposition dans vitam-ui par le biais d'exemples d'utilisation.

## Inspecter

Inspecter le DOM pour voir les classes .scss et/ou composants utilisées.

## Lancement

1. Démarer iam-internal, iam-external, ui-identity et security internal
> vitam-ui/api/api-iam/iam-external$ mvn spring-boot:run
> vitam-ui/api/api-iam/iam-internal$ mvn spring-boot:runs
> vitam-ui/api/api-security/security-internal$ mvn spring-boot:run
> vitam-ui/ui/ui-identity$ mvn spring-boot:run

2. Démarrer CAS
> vitam-ui/cas/cas-server$ ./run.sh

3. Démarer le projet front starter-kit 
> vitam-ui/ui/ui-frontend$ npm run start:starter-kit

4. S'authentifier sur CAS [https://dev.vitamui.com:8080/cas](https://dev.vitamui.com:8080/cas)

5. Aller sur l'URL du starter-kit [https://dev.vitamui.com:4201/](https://dev.vitamui.com:4201/)