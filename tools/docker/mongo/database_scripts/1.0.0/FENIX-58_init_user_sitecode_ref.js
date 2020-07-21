db = db.getSiblingDB('iam')

print("START FENIX-58_init_user_sitecode_ref.js");

// Update all users without siteCode : add an empty siteCode
db.users.updateMany(
  {"siteCode": {$exists: false}},
  {
    $set: {
      "siteCode": ""
    }
  }
);

print("END FENIX-58_init_user_sitecode_ref.js");
