# Presentation

This component is the CAS server.


# Start

```shell
mvn clean package
java -Dspring.config.additional-location=src/main/config/cas-server-application-dev.yml -Dspring.profiles.active=gateway -Xms128m -Xmx512m -jar target/cas-server.jar
```

# Main features / workflows

## Organizations, users and email-domains

VitamUI comes with a system organization, to which the superuser belongs.
It is used to manage customer organizations.
The superuser (superadmin@<systemdomain>) account is used to create other customer organizations.

Every customer organization comes with a generic admin account (admin@<maincustomerdomain>).
The admin account can create users and their permissions within the organization.

Organizations can have multiple email domains (ex mydomain.com, myotherdomain.fr...).
The admin account can create users within their organization with email accounts matching organization email domains.
The user email is used as a unique identifier.

Since Vitam 7.1, email domains can be shared among multiple organizations; A user can have multiple "user accounts" with
the same email address for multiple organizations.
However, a single email address is unique within a specific organization.

During CAS authentication workflow, the user if first asked to enter its email.
If multiple accounts match the provided email, then the user if asked to select the target organization (customerId)
that he wants to log into.

## Subrogation / deleguated authentication

The super-admin user can surrogate another user to help assist them to configure or troubleshoot their account.

To do so, he needs to log into VitamUI, select target organization / user to surrogate, and emits a subrogation
request.
The superuser is first logged out from VitamUI/CAS, then he is redirected to CAS to a welcome / confirmation
page, to re-authenticate.
Once logged-in, the super-user will be authenticated in the name of the surrogate user, and will be acting on his
behalf.

If the surrogate user is a generic account (e.g. an organization admin account), the subrogation request is
automatically accepted.
However, if the user is a nominative account (non-generic), the user to surrogate must explicitly authorize the
subrogation request.
To do so, he needs to log in first into his account, and then "accept" the subrogation request.
The superuser must wait for the subrogation request to be accepted before being redirected to CAS for re-authentication.

The superuser can abort the subrogation session and is redirected to CAS to re-authenticate again as a regular
superuser.

**NB.** The surrogate user needs to have a "subrogable" flag enabled.

## Providers configuration

VitamUI/CAS support the configuration of multiple identity providers.

- Built-in / internal provider (password, with optional SMS MFA)
- OpenId Connect: delegate authentication to another Identity Provider using OIDC protocol.
- SAML: delegate authentication to another Identity Provider using SAML protocol.
- TLS / X509 certificate

## Internally managed password authentication

CAS supports a fully featured password authentication mode :
- Configurable password pattern policy (min length, special characters...)
- Previously used passwords
- Password expiration
- Locked account due to too many failed passwords

Initial password creation is done using magic-link mail upon user creation.

There is no dedicated "update password" workflow (enter old password + new password + confirm new password screen).
However, the user can update its password using "I forgot my password" link, using a magic-link mail for password reset.

If the user logs in with an expired password, he'll be requested to enter a new password.

## Local mail tests (dev SMTP server)

A test local SMTP server can be found in `tools/docker/mail`.
This is useful to test account creation / reset password mails with existing or fake email domains, without requiring
actual email accounts.

Just run `./start.sh`, to pop a local SMTP dev docker server. Then browse http://localhost:3000/ to view sent emails.

## Multi-factor authentication (MFA) by SMS

When configuring an organization and/or for a specific user, an SMS-based MFA may be configured.

Authentication provider must be configured to use CAS internal authentication password, and user must have a
phone number configured in its user account.

Current version of VitamUI only supports `smsmode` (an SMS gateway platform).

For now, **NO** test mode is available yet.
To test this MFA authentication locally, you would need to either temporarily alter the SmsModeSmsSender class, to mock
the smsmode API, or to get a real test token from smsmode...

## Certificate authentication

CAS can be configured to use client x509 certificates to authenticate the user.

Local test configuration is available in the `tools/docker/nginx-cas-x509` folder. It provides:
- A set of test certificates generated using a test openssl PKI: root ca, intermediate ca, client & server cert
- Client certificate has an identifier encoded in its Subject DN (`/CN=UserCN/C=FR`), and an email `user@domain.com`
  encoded as a SUBJECT_ALTERNATE_NAME.
- A nginx reverse proxy configured with client/server TLS authn. It can be started using a simple `docker compose up -d`
  The nginx server forwards the client x509 certificate to CAS (https://localhost:8080) via the `x-ssl-cert` http
  header (configurable).
- We'll need to update CAS configuration as follows:

```
# Authent with x509 certificate
###############################
# Name of HTTP header (nginx is configured to forward TSL client cert in this header)
cas.authn.x509.sslHeaderName: x-ssl-cert
vitamui.authn.x509.enabled: true
vitamui.authn.x509.mandatory: true
# Extract user email (identifier) from the Certificate "subject alternate name"
vitamui.authn.x509.emailAttribute: 'SUBJECT_ALTERNATE_NAME'
vitamui.authn.x509.emailAttributeParsing: '.*RFC822Name=(.*)'
vitamui.authn.x509.emailAttributeExpansion:
# Extract user technical identifier (used for provisionning?!) using a regex
# /!\ the regex pattern must contain a single group (in parentheses)
vitamui.authn.x509.identifierAttribute: 'subject_dn'
vitamui.authn.x509.identifierAttributeParsing: 'C=.*, CN=(.*)'
vitamui.authn.x509.identifierAttributeExpansion:
vitamui.authn.x509.defaultDomain: 'domain.com'
```

You might need to change CAS URL `login.url` to `http://dev.vitamui.com/cas/login` in iam-external configuration.

Client certificate needs to be imported into local browser certificate store (password: `azerty`).

Nginx reverse proxy may be accessed using https://dev.vitamui.com:443/cas/login (instead of direct CAS access via
https://dev.vitamui.com:8080/cas/login).

**Warnings:**
- For now, the above configuration is only available locally. VitamUI Ansible configuration must be updated to support
  these settings.
- Certificate authentication does work NOT with subrogation (superuser cannot be authenticated with X509 certificate).
- Only ONE organization may use x509 certificate authentication per email domain; CAS has no means to know to which
  organization a certificate belongs.

## OpenId Connect authentication delegation

> TODO

## SAML authentication delegation

> TODO

# Development

Développement des pages html - en static grace à thymeleaf et avec sass :
```
npm install -g sass
sass --watch src/main/config/sass/cas.scss src/main/resources/static/css/cas.css
```

### Pour les parcours utilisant l'envois de mail :
lancez dans un conteneur votre webmail Mailhog.
```shell
docker run -d -p 1025:1025 -p 8025:8025 mailhog/mailhog
```
Et dans `cas-server-application-dev.yml` passez les parametres suivant:
```shell
spring.mail.host: localhost
spring.mail.port: 1025
```
et rendez-vous sur l'url http://localhost:8025/

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


# CAS server customizations

CAS is deployed in as a war-overlay (the officially recommended deployment method) with many customizations.

CAS implements standard workflows using spring WebFlow; an old yet flexible framework.

CAS customizations include :
- Overriding static resources
- Rewriting new Spring services that override partially or completely core CAS behavior.
- Overwriting authentication workflows: Ex. Supporting multi-domain organizations / multiple-users with the same
  login...
- Overriding persistence: IAM internal is used for persisting of user account information, organizations (customers),
  authentication providers...
- ...

**/!\ WARNING:**
CAS does not officially recommend extending core workflows, but unfortunately, VitamUI does exactly that...
This also means that upgrading CAS versions implies basically a rewrite of much customizations (3-way merge between CAS
vN-1, CAS vN, and VitamUI's CAS customization)

## Configuration

All customizations are loaded via the `AppConfig` or `WebflowConfig` classes.

## Model

The identity providers are defined via the `SamlIdentityPoviderDto` class (based on the `IdentityProviderDto`).
The `ProvidersService` loads the identity providers from the IAM API every minute and at startup.

## Webflow

Spring WebFlow workflows can be highly customizable. A workflow consists of :
- Views: templated pages rendered to the used
- Actions: Java code handlers to process user actions (like a POST request)
- Transitions / states: From a specific state, executing an action returns a transition to a target state (a view or
  another action)
- Session data: Data that can be persisted within user web session
- Flow-scope data: Data that can be persisted and can be passed from page to page while navigating within a specific
  web workflow. It is serialized as a secure blob persisted on client-side (as an encrypted hidden field in html forms).
  Flow-scope data is "lost" if the client closes or refreshes his browser.

Login workflow (`webflow/login/login-webflow` file) has been (deeply) customized to perform the login process in 3
steps:
- login input (`templates/emailForm.html`)
- customer selection (`templates/customerForm.html`) : Optional, only when multiple user accounts match the input email.
- password input (new file: `templates/passwordForm.html`) or authentication delegation.

If the provided email address matches a single user account, the target organization (customerId) is automatically
selected.

If multiple user accounts match the provided email, then the user if asked to select the target organization
(customerId) that he wants to log into.

If the provided email is invalid / does not exist in the system :
- If the domain does not match any configured organization email domains ==> KO
- If the domain matches a single organization, user is prompted to enter its credentials.
  Only after credential validation, then he will be rejected with an "email or password is invalid" error message
  (so we don't disclose account existence information).
- If the domain matches multiple organizations, the user is asked to select the target organization then to enter its
  credentials. Then he will be rejected with an "email or password is invalid" error message (so we don't disclose
  account existence information).

Once customerId selected (automatically or manually), the user is redirected to the appropriate authentication provider
using the `DispatcherAction` (called by the webflow) which redirects the user: to password page if internal provider is
selected, or an external IdP OIDC / SAML.
TLS X509 certificate authentication is automatic, and does not follow this workflow.

The external IdP can be forced using the `idp` request parameter (the `cas_idp` parameter must be used at the applications level via the `VitamUICasAuthenticationEntryPoint`).

**Important:** Information disclosure is still possible in some cases :
- The client enters too many bad credentials and locks the account: this is a global problem for most authentication
  providers.
- The list of configured organizations for an email domain: this is currently not considered as sensitive information.
- The fact that organization selection depends on user account existence (no prompt for single user accounts VS multiple
  for users having some accounts VS and all organizations for users having accounts for all organization or unknown
  emails): This is a by-design limitation, and aims to provide enhanced user experience.

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

The underneath SMS provider is smsmode.


# Database

## Start the MongoDB server

```shell
./docker/mongo/start_dev.sh
```

## Connect to the Mongo shell (if needed)

```shell
mongosh --port 27018 -u "mongod_dbuser_cas" -p "mongod_dbpwd_cas" --authenticationDatabase "cas"
```


# OAuth support

The OAuth server support is enabled in the CAS server. To support the resource owner password grant type, an appropriate service must be declared:

```json
db.services.insertOne({
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
db.services.insertOne({
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
