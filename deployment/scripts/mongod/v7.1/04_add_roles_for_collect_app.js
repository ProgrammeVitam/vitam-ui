dbSecurity.contexts.updateOne(
    { "_id": "ui_collect_context" },
    {
        $addToSet: {
            "roleNames": {
                $each: [
                    "ROLE_GET_SCHEMAS"
                ]
            }
        }
    }
);
