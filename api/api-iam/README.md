# Presentation

These components are a set of REST/JSON web services to perform CRUD operations on the business models:

- customers
- tenants
- identity providers
- profile groups
- profiles
- users.

There are composed of the web services themselves (api-iam-server module), the REST clients of these web services (api-iam-client module) and the DTOs shared between the two modules (api-iam-common module).


# Run the web services

```shell
mvn spring-boot:run
```

En développement, définir également la propriété `config.dir` qui doit pointer sur la répertoire de configuration : `xxx/api-iam-admin/api-iam-server/src/main/config` pour récupérer le certificat serveur.


# Database

## Start the MongoDB server

```shell
./docker/mongo/start_dev.sh
```

## Connect to the Mongo shell (if needed)

```shell
mongo --port 27018 -u "mongod_dbuser_iam" -p "mongod_dbpwd_iam" --authenticationDatabase "iam"
```


# Security

Generate the certificate:

`keytool -genkeypair -alias api-iam-admin-server -keyalg RSA -validity 1825 -keystore api-iam-admin-server-keystore.jks -storetype JKS -keypass keypwd -storepass keyspwd`

`keytool -genkeypair -alias admin-client-keystore -keyalg RSA -validity 1825 -keystore admin-client-keystore.jks -storetype JKS -keypass keyspwd -storepass keyspwd`

Extract the public key:

`keytool -exportcert -alias api-iam-admin-server -keystore api-iam-admin-server-keystore.jks -file api-iam-admin-server-public.der`

`keytool -exportcert -alias admin-client-keystore -keystore admin-client-keystore.jks -file admin-client-public.der`

Import the keys:

`keytool -importcert -alias api-iam-admin-server-public -keystore admin-client-truststore.jks -file api-iam-admin-server-public.der`

`keytool -importcert -alias admin-client-public -keystore api-iam-admin-server-truststore.jks -file admin-client-public.der`

`keytool -importcert -alias cas-server-public -keystore api-iam-admin-client-truststore.jks -file cas-server-public.der`

Convert to PEM files (PEM = base 64 vs DER = binary):

`openssl x509 -inform der -in admin-client-public.cer -out admin-client-public.pem`
