// -----------------------------------------------------------------------------
// Attaches the Keycloak test IdP (OIDC + SAML) to a VitamUI organization.
//
// GENERATED FILE — rendered by tools/docker/keycloak/setup.sh from
// provision/vitamui-providers.js.tpl. Edit the template, not the output.
//
// The script is idempotent: running it again refreshes the two providers (in
// particular the SAML metadata, which changes whenever the Keycloak realm is
// recreated) and leaves everything else untouched.
//
// Run with:  mongosh "<uri>" generated/vitamui-providers.js
// -----------------------------------------------------------------------------

/* eslint-disable no-undef */

const OIDC_TECHNICAL_NAME = "${OIDC_TECHNICAL_NAME}";
const SAML_TECHNICAL_NAME = "${SAML_TECHNICAL_NAME}";
const OIDC_EMAIL_DOMAIN = "${OIDC_EMAIL_DOMAIN}";
const SAML_EMAIL_DOMAIN = "${SAML_EMAIL_DOMAIN}";
const OIDC_USER_EMAIL = "${OIDC_USER_EMAIL}";
const SAML_USER_EMAIL = "${SAML_USER_EMAIL}";
const OIDC_CLIENT_ID = "${OIDC_CLIENT_ID}";
const OIDC_CLIENT_SECRET = "${OIDC_CLIENT_SECRET}";
const OIDC_DISCOVERY_URL = "${KEYCLOAK_BASE_URL}/realms/${KEYCLOAK_REALM}/.well-known/openid-configuration";
const SP_KEYSTORE_BASE64 = "${SP_KEYSTORE_BASE64}";
const SP_KEYSTORE_PASSWORD = "${SP_KEYSTORE_PASSWORD}";
const IDP_METADATA = `${IDP_METADATA_XML}`;

const REFERENCE_USER_EMAIL = "${VITAMUI_REFERENCE_USER_EMAIL}";
const FORCED_CUSTOMER_ID = "${VITAMUI_CUSTOMER_ID}";
const FORCED_GROUP_ID = "${VITAMUI_GROUP_ID}";

const OIDC_PROVIDER_ID = "keycloak_test_idp_oidc";
const SAML_PROVIDER_ID = "keycloak_test_idp_saml";
const OIDC_USER_ID = "keycloak_test_user_oidc";
const SAML_USER_ID = "keycloak_test_user_saml";
const OIDC_USER_INFO_ID = "keycloak_test_userinfo_oidc";
const SAML_USER_INFO_ID = "keycloak_test_userinfo_saml";

db = db.getSiblingDB("${MONGO_DB}");

// --- Target organization ------------------------------------------------------

// The email domain never determines the organization at login time: CAS looks the
// user up by email in iam.users and takes the customerId from that record. So the
// organization is resolved from an existing user rather than from a domain.
let customerId = FORCED_CUSTOMER_ID;
let groupId = FORCED_GROUP_ID;

if (!customerId || !groupId) {
  const referenceUser = db.users.findOne({ email: REFERENCE_USER_EMAIL });
  if (!referenceUser) {
    throw new Error(
      "Reference user '" +
        REFERENCE_USER_EMAIL +
        "' not found in " +
        db.getName() +
        ".users. Set VITAMUI_REFERENCE_USER_EMAIL to an existing user, or pin " +
        "VITAMUI_CUSTOMER_ID and VITAMUI_GROUP_ID in .env."
    );
  }
  customerId = customerId || referenceUser.customerId;
  groupId = groupId || referenceUser.groupId;
}

const customer = db.customers.findOne({ _id: customerId });
if (!customer) {
  throw new Error("Customer '" + customerId + "' not found in " + db.getName() + ".customers.");
}
const group = db.groups.findOne({ _id: groupId });
if (!group) {
  throw new Error("Group '" + groupId + "' not found in " + db.getName() + ".groups.");
}
if (group.customerId !== customerId) {
  throw new Error("Group '" + groupId + "' does not belong to customer '" + customerId + "'.");
}

print("Organization : " + customer.name + " (" + customerId + ")");
print("Group        : " + group.name + " (" + groupId + ")");

// --- Guard against an ambiguous provider chain --------------------------------

// findByUserIdentifierAndCustomerId() returns the FIRST provider of the customer
// whose pattern matches the email. A pre-existing provider matching one of our
// two domains would shadow the one we are about to create, and the failure would
// only show up as a silent redirect to the wrong IdP.
function escapeForRegex(value) {
  return value.replace(/[.*+?^()|[\]{}$\\]/g, "\\$&");
}

function patternFor(domain) {
  return ".*@" + escapeForRegex(domain);
}

[
  [OIDC_EMAIL_DOMAIN, OIDC_PROVIDER_ID],
  [SAML_EMAIL_DOMAIN, SAML_PROVIDER_ID],
].forEach(function (entry) {
  const domain = entry[0];
  const ownId = entry[1];
  const probe = "probe@" + domain;
  db.providers
    .find({ customerId: customerId, _id: { $ne: ownId } })
    .toArray()
    .forEach(function (provider) {
      (provider.patterns || []).forEach(function (pattern) {
        if (new RegExp("^(?:" + pattern + ")$", "i").test(probe)) {
          throw new Error(
            "Provider '" +
              provider.name +
              "' (" +
              provider._id +
              ") already matches '" +
              probe +
              "' for this organization. Pick another email domain in .env."
          );
        }
      });
    });
});

// --- Identifier allocation ----------------------------------------------------

// Returns an int32, like the migration scripts: `$inc` with an integer literal
// preserves the BSON type of the sequence.
function nextSequence(sequenceId) {
  const updated = db.sequences.findOneAndUpdate(
    { _id: sequenceId },
    { $inc: { sequence: 1 } },
    { returnDocument: "after" }
  );
  if (!updated) {
    throw new Error("Sequence '" + sequenceId + "' not found in " + db.getName() + ".sequences.");
  }
  return updated.sequence;
}

// providers and users store their identifier as a string, userInfos as a number.
function nextIdentifier(sequenceId) {
  return String(nextSequence(sequenceId));
}

function identifierFor(collection, id, sequenceId) {
  const existing = collection.findOne({ _id: id }, { identifier: 1 });
  return existing ? existing.identifier : nextIdentifier(sequenceId);
}

// --- Providers ----------------------------------------------------------------

// Field set mirrors IdentityProviderDto: the OIDC branch of Pac4jClientBuilder
// needs clientId + clientSecret + discoveryUrl, the SAML branch needs
// technicalName + keystoreBase64 + keystorePassword + privateKeyPassword +
// idpMetadata. A single missing field silently downgrades the provider to "no
// client built".
const oidcProvider = {
  _id: OIDC_PROVIDER_ID,
  identifier: identifierFor(db.providers, OIDC_PROVIDER_ID, "provider_identifier"),
  name: "Keycloak (OIDC)",
  technicalName: OIDC_TECHNICAL_NAME,
  internal: false,
  enabled: true,
  readonly: false,
  patterns: [patternFor(OIDC_EMAIL_DOMAIN)],
  // Keycloak's `sub` is an opaque uuid, so the email has to be read from a
  // dedicated claim.
  mailAttribute: "email",
  autoProvisioningEnabled: false,
  propagateLogout: false,
  clientId: OIDC_CLIENT_ID,
  clientSecret: OIDC_CLIENT_SECRET,
  discoveryUrl: OIDC_DISCOVERY_URL,
  scope: "openid email profile",
  preferredJwsAlgorithm: "RS256",
  useState: true,
  useNonce: true,
  usePkce: false,
  protocoleType: "OIDC",
  customerId: customerId,
  _class: "providers",
};

const samlProvider = {
  _id: SAML_PROVIDER_ID,
  identifier: identifierFor(db.providers, SAML_PROVIDER_ID, "provider_identifier"),
  name: "Keycloak (SAML)",
  technicalName: SAML_TECHNICAL_NAME,
  internal: false,
  enabled: true,
  readonly: false,
  patterns: [patternFor(SAML_EMAIL_DOMAIN)],
  // The Keycloak client forces a NameID of format `email`, which pac4j exposes
  // as the profile id: no mail attribute lookup needed.
  mailAttribute: "",
  autoProvisioningEnabled: false,
  propagateLogout: false,
  keystoreBase64: SP_KEYSTORE_BASE64,
  keystorePassword: SP_KEYSTORE_PASSWORD,
  privateKeyPassword: SP_KEYSTORE_PASSWORD,
  idpMetadata: IDP_METADATA,
  spMetadata: "",
  maximumAuthenticationLifetime: 3600,
  authnRequestBinding: "POST",
  // The Keycloak client is declared with "Client signature required" off, so
  // the AuthnRequest is not signed; assertions are signed and pac4j validates
  // them against the certificate carried by the IdP metadata.
  authnRequestSigned: false,
  wantsAssertionsSigned: true,
  protocoleType: "SAML",
  customerId: customerId,
  _class: "providers",
};

[oidcProvider, samlProvider].forEach(function (provider) {
  db.providers.replaceOne({ _id: provider._id }, provider, { upsert: true });
  print("Provider     : " + provider.name + " -> " + provider.patterns[0]);
});

// --- Test users ---------------------------------------------------------------

// Delegated authentication provisions nothing: CAS refuses an unknown email
// before ever redirecting to the IdP.
function upsertUser(id, userInfoId, email, lastname, sequenceId) {
  // The language moved out of `users` into `userInfos` (migration
  // 010_TRTL-936). A user without a `userInfoId` makes the portal fail with a
  // 500 on load, so the two documents have to be created together.
  const existingUserInfo = db.userInfos.findOne({ _id: userInfoId }, { identifier: 1 });
  db.userInfos.replaceOne(
    { _id: userInfoId },
    {
      _id: userInfoId,
      identifier: existingUserInfo ? existingUserInfo.identifier : nextSequence("user_infos_identifier"),
      language: "FRENCH",
      _class: "userInfos",
    },
    { upsert: true }
  );

  const existing = db.users.findOne({ _id: id });
  const user = {
    _id: id,
    identifier: existing ? existing.identifier : nextIdentifier(sequenceId),
    email: email,
    firstname: "Demo",
    lastname: lastname,
    userInfoId: userInfoId,
    type: "GENERIC",
    status: "ENABLED",
    otp: false,
    subrogeable: true,
    readonly: false,
    level: group.level || "",
    groupId: groupId,
    nbFailedAttempts: 0,
    passwordExpirationDate: "2050-01-09T00:00:00.000+01:00",
    customerId: customerId,
    _class: "users",
  };

  const clash = db.users.findOne({ email: email, customerId: customerId, _id: { $ne: id } });
  if (clash) {
    throw new Error("User '" + email + "' already exists in this organization with _id " + clash._id + ".");
  }

  db.users.replaceOne({ _id: id }, user, { upsert: true });
  print("User         : " + email);
}

upsertUser(OIDC_USER_ID, OIDC_USER_INFO_ID, OIDC_USER_EMAIL, "OIDC", "user_identifier");
upsertUser(SAML_USER_ID, SAML_USER_INFO_ID, SAML_USER_EMAIL, "SAML", "user_identifier");

// --- Email domains ------------------------------------------------------------

// Not needed to log in — only so the two domains can be picked in the VitamUI
// interface when creating further test accounts.
db.customers.updateOne(
  { _id: customerId },
  { $addToSet: { emailDomains: { $each: [OIDC_EMAIL_DOMAIN, SAML_EMAIL_DOMAIN] } } }
);

print("");
print("Done. CAS reloads the provider list every minute, no restart needed.");
