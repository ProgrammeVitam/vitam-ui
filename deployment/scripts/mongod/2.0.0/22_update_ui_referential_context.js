dbSecurity.contexts.updateOne(
    { "_id": "ui_referential_context" },
    {
        $addToSet: {
            "roleNames": {
                $each: [
                    "ROLE_UPDATE_LOGBOOK_OPERATION",
                    "ROLE_GET_ALL_LOGBOOK_OPERATION",
                    "ROLE_GET_LOGBOOK_OPERATION",
                    "ROLE_UPDATE_ME_USERS"
                ]
            }
        }
    }
);
