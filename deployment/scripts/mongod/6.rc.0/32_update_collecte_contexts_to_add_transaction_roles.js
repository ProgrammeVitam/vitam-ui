dbSecurity.contexts.updateOne(
    { "_id": "ui_collect_context" },
    {
        $addToSet: {
            "roleNames": {
                $each: [
                    "ROLE_CREATE_TRANSACTIONS",
                    "ROLE_UPDATE_TRANSACTIONS",
                    "ROLE_GET_TRANSACTIONS",
                    "ROLE_DELETE_TRANSACTIONS"
                ]
            }
        }
    }
);
