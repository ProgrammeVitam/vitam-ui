dbSecurity.contexts.updateOne(
    { "_id": "ui_referential_context" },
    {
        $addToSet: {
            "roleNames": {
                $each: [
                    "ROLE_GET_UNITS",
                    "ROLE_GET_EXTERNAL_PARAMS",
                    "ROLE_GET_ACCESSION_REGISTER_DETAIL"
                ]
            }
        }
    }
);

dbSecurity.contexts.updateOne(
    {
        "_id": {
            $in: [
                "ui_admin_identity_context",
                "ui_identity_context"
            ]
        }
    },
    {
        $addToSet: {
            "roleNames": {
                $each: [
                    "ROLE_GET_EXTERNAL_PARAMS",
                    "ROLE_CREATE_EXTERNAL_PARAM_PROFILE",
                    "ROLE_EDIT_EXTERNAL_PARAM_PROFILE",
                    "ROLE_SEARCH_EXTERNAL_PARAM_PROFILE"
                ]
            }
        }
    }
);
