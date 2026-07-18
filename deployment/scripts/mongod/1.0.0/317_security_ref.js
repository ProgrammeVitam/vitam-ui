dbSecurity.contexts.updateOne(
    { "_id": "ui_identity_context" },
    {
        $addToSet: {
            "roleNames": {
                $each: [
                    "ROLE_CREATE_EXTERNAL_PARAM_PROFILE",
                    "ROLE_EDIT_EXTERNAL_PARAM_PROFILE",
                    "ROLE_SEARCH_EXTERNAL_PARAM_PROFILE"
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
                    "ROLE_CREATE_EXTERNAL_PARAM_PROFILE",
                    "ROLE_EDIT_EXTERNAL_PARAM_PROFILE",
                    "ROLE_SEARCH_EXTERNAL_PARAM_PROFILE"
                ]
            }
        }
    }
);
