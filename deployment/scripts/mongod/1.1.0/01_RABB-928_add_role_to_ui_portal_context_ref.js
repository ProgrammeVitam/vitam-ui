dbSecurity.contexts.updateOne(
    { "_id": "ui_portal_context" },
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
