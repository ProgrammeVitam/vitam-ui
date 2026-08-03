dbSecurity.contexts.updateOne(
    { "_id": "ui_referential_context" },
    {
        $addToSet: {
            "roleNames": {
                $each: [
                    "ROLE_UPDATE_LOGBOOK_OPERATION",
                    "ROLE_GET_ALL_LOGBOOK_OPERATION",
                    "ROLE_GET_LOGBOOK_OPERATION",
                    "ROLE_UPDATE_ME_USERS",
                    "ROLE_IMPORT_RULES",
                    "ROLE_GET_FILLING_PLAN_ACCESS",
                    "ROLE_GET_EXTERNAL_PARAMS"
                ]
            }
        }
    }
);
