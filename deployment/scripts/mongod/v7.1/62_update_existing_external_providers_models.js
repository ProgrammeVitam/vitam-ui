print("START 61_update_existing_external_providers_models.js.j2");

db = db.getSiblingDB('iam');

db.providers.updateMany(
  {
    internal: false,
    $or: [
      { protocoleType: "SAML" },
      { protocoleType: "OIDC" }
    ],
    wantsAssertionsSigned: { $exists: false }
  },
  [
    {
      $set: {
        wantsAssertionsSigned: {
          $cond: {
            if: { $eq: [{ $type: "$spMetadata" }, "missing"] },
            then: false,
            else: {
              $cond: {
                if: { $regexMatch: { input: "$spMetadata", regex: /WantAssertionsSigned="true"/ } },
                then: true,
                else: false
              }
            }
          }
        }
      }
    }
  ]
);



db.providers.updateMany(
  {
    internal: false,
    $or: [
      { protocoleType: "SAML" },
      { protocoleType: "OIDC" }
    ],
    authnRequestSigned: { $exists: false }
  },
  [
    {
      $set: {
        authnRequestSigned: {
          $cond: {
            if: { $eq: [{ $type: "$spMetadata" }, "missing"] },
            then: false,
            else: {
              $cond: {
                if: { $regexMatch: { input: "$spMetadata", regex: /AuthnRequestsSigned="true"/ } },
                then: true,
                else: false
              }
            }
          }
        }
      }
    }
  ]
);


db.providers.updateMany(
  {
    internal: false,
    $or: [
      { protocoleType: "SAML" },
      { protocoleType: "OIDC" }
    ],
    propagateLogout: { $exists: false }
  },
  {
    $set: { propagateLogout: false }
  }
);

print("END 61_update_existing_external_providers_models.js.j2");
