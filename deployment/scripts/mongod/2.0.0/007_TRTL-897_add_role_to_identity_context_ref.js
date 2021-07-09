
print("START 007_TRTL-897_add_role_to_profile_ref.js");

db = db.getSiblingDB('iam');


db.profiles.updateMany({
        "applicationName": "HIERARCHY_PROFILE_APP"
    },
    {
        "$push": {
            "roles": {
                "$each": [
                    {
                        "name": "ROLE_UPDATE_ME_USERS"
                    }

                ]
            }
        }
    }
);
print("END 007_TRTL-897_add_role_to_profile_ref.js");
