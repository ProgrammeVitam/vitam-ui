print("START 001_REC-496_add_provisioning_role_to_cas_user_ref.js");

db = db.getSiblingDB('security');

// Add role to context
db.contexts.updateOne(
  { _id: "iam_internal_context" },
  { $addToSet: { roleNames: { $each: [
    "ROLE_PROVISIONING_USER"
  ]}}}
);

db = db.getSiblingDB('iam');

// Add role to profile
db.profiles.updateOne(
  { "_id": "cas_profile" },
  { $addToSet: { "roles": { $each: [
    { "name" : "ROLE_PROVISIONING_USER" }
  ]}}}
);

print("END 001_REC-496_add_provisioning_role_to_cas_user_ref.js");
