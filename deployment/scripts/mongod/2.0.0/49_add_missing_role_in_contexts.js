// Add missing role ROLE_GET_EXTERNAL_PARAMS to contexts
dbSecurity.contexts.updateOne(
    { "_id": "ui_identity_context" },
    {
        $addToSet: {
            "roleNames": "ROLE_GET_EXTERNAL_PARAMS"
        }
    }
);

dbSecurity.contexts.updateOne(
    { "_id": "ui_admin_identity_context" },
    {
        $addToSet: {
            "roleNames": "ROLE_GET_EXTERNAL_PARAMS"
        }
    }
);

dbSecurity.contexts.updateOne(
    { "_id": "ui_referential_context" },
    {
        $addToSet: {
            "roleNames": "ROLE_GET_EXTERNAL_PARAMS"
        }
    }
);
