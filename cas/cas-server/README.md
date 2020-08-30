# Presentation

This component is the CAS server.


# Start

```shell
mvn clean package
java -Dspring.config.location=src/main/config/cas-server-application-dev.yml -jar target/cas-server.jar
```


# SAML metadata generation

1) Retrieval of the IdP metadata, IdP metadata are ignored, so test metdata can be used instead
2) Creation of a keystore for the IdP: `keytool -genkeypair -alias idp-test -keypass password -keystore idp-test-keystore.jks -storepass password -keyalg RSA -keysize 2048 -validity 3650`
3) Generation of the SP metadata using the `GenerateSpMetadata` class (in `api-iam-server`) and saving into a file


# Security

Generate the certificate:

`keytool -genkeypair -alias cas-client-keystore -keyalg RSA -validity 1825 -keystore cas-client-keystore.jks -storetype JKS -keypass keyspwd -storepass keyspwd`
`keytool -genkeypair -alias cas-server-keystore -keyalg RSA -validity 1825 -keystore cas-server-keystore.jks -storetype JKS -keypass keyspwd -storepass keyspwd`

Extract the public key:

`keytool -exportcert -alias cas-client-keystore -keystore cas-client-keystore.jks -file cas-client-public.der`
`keytool -exportcert -alias cas-server-keystore -keystore cas-server-keystore.jks -file cas-server-public.der`

Import the keys:

`keytool -importcert -alias cas-client-public -keystore api-iam-admin-server-truststore.jks -file cas-client-public.der`
`keytool -importcert -alias api-iam-admin-server-public -keystore cas-client-truststore.jks -file api-iam-admin-server-public.der`


# URL

The login URL is `https://dev.vitamui.com:8080/cas/login`, with or without the `service` parameter which is the application the user wants to log in.


# Users

Internal: pierre@vitamui.com / password
Internal(SMS): julien@vitamui.com / password
Internal+MFA: john@vitamui.com / password
IdP: jean@total.com / password
IdP fails at CAS: kevin@total.com
password reset link: jerome.leleu@vitamui.com
julien@vitamui.com can surrogate pierre@vitamui.com


# CAS server (v5.2.5) customizations

## Configuration

All customizations are loaded via the `AppConfig` or `WebflowConfig` classes.

## Model

The identity providers are defined via the `SamlIdentityPoviderDto` class (based on the `IdentityProviderDto`).
The `ProvidersService` loads the identity providers from the IAM API every minute and at startup.

## Webflow

The webflow (`webflow/login/login-webflow` file) is customized to perform the login process in two steps:

- login input (`templates/casLoginView.html`)
- password input (new file: `templates/casPwdView.html`) or authentication delegation.

It's the `DispatcherAction` (called by the webflow) which computes if the user must fill in his password or be redirected to an IdP for login.

The external IdP can be forced using the `idp` request parameter (the `cas_idp` parameter must be used at the applications level via the `VitamUICasAuthenticationEntryPoint`).

## Authentication

The login/password authentication is handled by the `UserAuthenticationHandler` (which uses the IAM API).
In all cases (login/pwd or authentication delegation), the `UserPrincipalResolver` is called based on the identifier (email) to retrieve the user from the IAM API.

The default authentication handler is disabled (`cas.authn.accept.users` property).

## Password management

The password management is done by the `cas-server-support-surrogate-webflow` module (which is a dependency in the `pom.xml` file).
It is configured via the overriden `RestPasswordManagementConfiguration` class to use the new `IamRestPasswordManagementService` component based on the `UserExternalRestClient` (IAM API).
The link to use is on the login page: "Reset your password".

A password change may be triggered (whether the user is authenticated or not) thanks to the `TriggerChangePasswordAction` and the request parameter: `doChangePassword=true`.

A password reset may be requested via the URL: `/extras/resetPassword?username=xxx&ttl=1day` using the `ResetPasswordController`.

## Surrogation

The surrogation is handled by the `cas-server-support-surrogate-webflow` module (dependency in the `pom.xml` file) configured in REST mode (using the IAM API).
It is triggered by the '|' character before the identifier: "|julien@vitamui.com" to choose who Julien can surrogate or "pierre@vitamui.com|julien@vitamui.com" so that Julien surrogates Pierre.

The `DelegatedSurrogateAuthenticationPostProcessor` component is used to handle surrogation with authentication delegation.

## Logout

Post-logout URLs are enabled if they are defined as a service and passed by the `next` request parameter.

## Throttling

No more than 1 login per 3 seconds is accepted.

## MFA by SMS

A specific only SMS MFA provider has been developed in the `mfa` package. It is globally applied with a bypass based on the IAM web service.

The underneath SMS provider is Twilio.


# Database

## Start the MongoDB server

```shell
./docker/mongo/start_dev.sh
```

## Connect to the Mongo shell (if needed)

```shell
mongo --port 27018 -u "mongod_dbuser_cas" -p "mongod_dbpwd_cas" --authenticationDatabase "cas"
```


# OAuth support

The OAuth server support is enabled in the CAS server. To support the resource owner password grant type, an appropriate service must be declared:

```json
db.services.insert({
  "_id" : NumberInt(61),
  "_class" : "org.apereo.cas.support.oauth.services.OAuthRegisteredService",
  "clientId": "testclientid",
  "clientSecret": "testclientsecret",
  "serviceId" : "testclientid",
  "name" : "Test OAuth",
  "supportedGrantTypes": [ "password" ],
  "attributeReleasePolicy" : {
    "_class" : "org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy",
    "allowedAttributes" : [ "authtoken" ]
  }
});
```

or with a JSON response:

```json
db.services.insert({
  "_id" : NumberInt(61),
  "_class" : "org.apereo.cas.support.oauth.services.OAuthRegisteredService",
  "clientId": "testclientid",
  "clientSecret": "testclientsecret",
  "serviceId" : "testclientid",
  "name" : "Test OAuth",
  "supportedGrantTypes": [ "password" ],
  "attributeReleasePolicy" : {
    "_class" : "org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy",
    "allowedAttributes" : [ "authtoken" ]
  },
  "jsonFormat": true
});
```

Then, a user can get his auth token as the access token by providing his credentials and the testclientid.

The call must be a POST request:

`clear; curl -k -X POST -H "Content-Type: application/x-www-form-urlencoded" -d "grant_type=password&client_id=testclientid&username=admin@vitamui.com&password=password" https://dev.vitamui.com:8080/cas/oauth2.0/accessToken`

The result contains the auth token in a plain response:

`access_token=TOK-1-F8lEhVif0FWjgDF32ov73TtKhE6mflRu&expires_in=28800`

or in a JSON response:

`{"access_token":"TOK-1-F8lEhVif0FWjgDF32ov73TtKhE6mflRu","token_type":"bearer","expires_in":28800}`
