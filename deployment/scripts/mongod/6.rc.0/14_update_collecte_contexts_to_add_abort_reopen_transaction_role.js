dbSecurity.contexts.updateOne(
    { "_id": "ui_collect_context" },
    {
        $addToSet: {
            "roleNames": {
                $each: [
                    "ROLE_ABORT_TRANSACTIONS",
                    "ROLE_REOPEN_TRANSACTIONS"
                ]
            }
        }
    }
);
