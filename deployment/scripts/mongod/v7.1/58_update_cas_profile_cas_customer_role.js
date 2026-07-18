print("START 58_update_cas_profile_cas_customer_role");

db = db.getSiblingDB('iam');

db.profiles.updateOne({"_id": "cas_profile"}, {
    $addToSet: {
        "roles": {
            $each: [{"name": "ROLE_CAS_CUSTOMERS"}]
        }
    }
});

print("END 58_update_cas_profile_cas_customer_role");
