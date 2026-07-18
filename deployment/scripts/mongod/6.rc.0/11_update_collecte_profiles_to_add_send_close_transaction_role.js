db = db.getSiblingDB('iam');

print("START 11_update_collecte_profiles_to_add_send_close_transaction_role.js");

db.profiles.updateMany(
  {"applicationName": "COLLECT_APP"},
  {
    $addToSet: {
      "roles": {
        $each: [
          {"name": "ROLE_SEND_TRANSACTIONS"},
          {"name": "ROLE_CLOSE_TRANSACTIONS"}
        ]
      }
    }
  }
);

print("END 11_update_collecte_profiles_to_add_send_close_transaction_role.js");
