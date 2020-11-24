# Presentation

These components are a set of REST/JSON web services to perform CRUD operations on the business models:

- accessContract

There are composed of the web services themselves (api-referential-server module), the REST clients of these web services (api-referential-client module) and the DTOs shared between the two modules (api-referential-common module).


# Run the web services

```shell
mvn spring-boot:run
```

En développement, définir également la propriété `config.dir` qui doit pointer sur la répertoire de configuration : `xxx/api-referential/api-referential-server/src/main/config` pour récupérer le certificat serveur.
