dbSecurity.contexts.updateOne(
    { "_id": "ui_referential_context" },
    {
        $addToSet: {
            "roleNames": {
                $each: [
                    "ROLE_GET_UNITS"
                ]
            }
        }
    }
);

dbSecurity.contexts.updateOne(
    { "_id": "ui_identity_context" },
    {
        $addToSet: {
            "roleNames": {
                $each: [
                    "ROLE_GET_EXTERNAL_PARAMS"
                ]
            }
        }
    }
);

dbSecurity.contexts.updateOne(
    { "_id": "ui_admin_identity_context" },
    {
        $addToSet: {
            "roleNames": {
                $each: [
                    "ROLE_GET_EXTERNAL_PARAMS"
                ]
            }
        }
    }
);

dbSecurity.contexts.updateOne(
    { "_id": "ui_referential_context" },
    {
        $addToSet: {
            "roleNames": {
                $each: [
                    "ROLE_GET_EXTERNAL_PARAMS"
                ]
            }
        }
    }
);
