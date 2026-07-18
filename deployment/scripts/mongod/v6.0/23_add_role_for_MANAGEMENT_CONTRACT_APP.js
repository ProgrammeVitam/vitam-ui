// -------- ADD MANAGEMENT CONTRACT APPLICATION ROLES TO UI REFERENTIAL CONTEXT -----
dbSecurity.contexts.updateOne(
    { "_id": "ui_referential_context" },
    {
        $addToSet: {
            "roleNames": {
                $each: [
                    "ROLE_GET_MANAGEMENT_CONTRACT",
                    "ROLE_CREATE_MANAGEMENT_CONTRACT",
                    "ROLE_UPDATE_MANAGEMENT_CONTRACT",
                    "ROLE_DELETE_MANAGEMENT_CONTRACT"
                ]
            }
        }
    }
);
