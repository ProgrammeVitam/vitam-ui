# Presentation

This module is a set of REST/JSON web services to perform CRUD operations on the business models:

- customers
- tenants
- identity providers
- profile groups
- profiles
- users.


# Run the web services

```shell
mvn spring-boot:run
```

# Provisioning

The users can be provided by a tier service

To enable this function, you must configure an external identity provider with the property autoprovisioning set to true.

At the module level, you must add properties to do the mapping between the identity provider and the webservice to call

Example : **application.yml**

``` 
    provisioning-client:
        identity-providers:
            - idp-identifier: system_idp
              uri: https://localhost:8090/provisioning/v1/users
              client:
                secure: true
                ssl-configuration:
                    keystore:
                    key-path: src/main/config/keystore_provisioning-users.jks
                    key-password: changeme
                    type: JKS
                truststore:
                    key-path: src/main/config/truststore_server.jks
                    key-password: changeme
                    type: JKS
                hostname-verification: false
```

Please note that the configuration take a list in input

