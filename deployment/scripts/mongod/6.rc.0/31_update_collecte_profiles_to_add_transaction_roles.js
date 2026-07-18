db = db.getSiblingDB('iam');

print("START 31_update_collecte_profiles_to_add_transaction_roles");

db.profiles.updateMany(
  {"applicationName": "COLLECT_APP"},
  {
    $addToSet: {
      "roles": {
        $each: [
          {"name": "ROLE_CREATE_TRANSACTIONS"},
          {"name": "ROLE_UPDATE_TRANSACTIONS"},
          {"name": "ROLE_GET_TRANSACTIONS"},
          {"name": "ROLE_DELETE_TRANSACTIONS"}
        ]
      }
    }
  }
);

print("END 31_update_collecte_profiles_to_add_transaction_roles.js");
