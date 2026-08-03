dbSecurity.contexts.updateOne(
    { "_id": "ui_collect_context" },
    {
        $addToSet: {
            "roleNames": {
                $each: [
                    "ROLE_SEND_TRANSACTIONS",
                    "ROLE_CLOSE_TRANSACTIONS"
                ]
            }
        }
    }
);
