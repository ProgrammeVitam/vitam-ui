print("START TRTL-157_add_ROLE_UPDATE_ME_USERS_portal");
db = db.getSiblingDB('security');
db.contexts.updateOne(
    {"_id": "ui_portal_context"},
    {
        $addToSet: {
            "roleNames": {
                $each: [
                    "ROLE_UPDATE_ME_USERS"
                ]
            }
        }
    }
);
print("END TRTL-157_add_ROLE_UPDATE_ME_USERS_portal");
