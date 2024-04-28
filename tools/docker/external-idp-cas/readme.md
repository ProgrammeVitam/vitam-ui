# Readme  
To test the authentication delegation in the OIDC protocol, you will find an example of ready-made configuration here.
- Simply launch the `run-dev.sh` script, 
- This script will create a Docker container with a Keycloak and its database, create an OIDC client, and create a test user.
- The keycloak is the url `http://localhost:8041`  
- The keycloak credentials for admin are: 
  - user: `admin`, 
  - password: `changeme`
- The OIDC client created has the following informations:
  - identifier: `vitamui-oidc`
  - secret: `QQXbm6947N5kYVL0yLDAHwlo3ZW2I8ui`
  - OpenID Endpoint Configuration: `http://localhost:8041/realms/vitamui-test/.well-known/openid-configuration`
- The user created has the following informations:
  - Username: `demo@change-me.fr`
  - Email: `demo@change-me.fr`
  - Name: `demo oidc vitamui`
  - Password: `ChangeIt.2024` 

**Full example**
Based on the test container, bellow an example on OIDC authentication provider based on the script run-dev.sh :

```mongodb-json

{
    _id: '662f3f36f0fb0f340240221df27815df8c0a490d8f2f73b120d78a3fc0de2a7b',
    identifier: '53',
    name: 'keycloak-oidc',
    technicalName: 'idp295983',
    internal: false,
    enabled: true,
    patterns: [
        '.*@change-me.fr'
    ],
    readonly: false,
    mailAttribute: 'email',
    autoProvisioningEnabled: false,
    propagateLogout: false,
    authnRequestBinding: 'POST',
    wantsAssertionsSigned: false,
    authnRequestSigned: false,
    clientId: 'vitamui-oidc',
    clientSecret: 'QQXbm6947N5kYVL0yLDAHwlo3ZW2I8ui',
    discoveryUrl: 'http://localhost:8041/realms/vitamui-test/.well-known/openid-configuration',
    scope: 'openid email',
    preferredJwsAlgorithm: 'ES256',
    useState: true,
    useNonce: true,
    usePkce: false,
    protocoleType: 'OIDC',
    customerId: '5c7927af7884583d1ebb6e7a74547a15e35d431599d976a9708eb12d6c5e56c9',
    _class: 'providers'
}

```

Please follow steps in ```cas/cas-server/readme.md```
