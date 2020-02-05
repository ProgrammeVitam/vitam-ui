# Presentation

These components are a set of REST/JSON web services to perform CRUD operations on the business models:

- application contexts
- certificates (X509).

There are composed of the web services themselves (api-security module), the REST clients of these web services (api-security-client module) and the DTOs shared between the two modules (api-security-common module).


# Run the web services

```shell
mvn spring-boot:run
```

# Database

## Start the MongoDB server

```shell
./docker/mongo/start_dev.sh
```

## Connect to the Mongo shell (if needed)

```shell
mongo --port 27018 -u "mongod_dbuser_security" -p "mongod_dbpwd_security" --authenticationDatabase "security"
```
