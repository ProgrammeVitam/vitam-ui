dbSecurity.contexts.updateOne(
    { "_id": "ui_collect_context" },
    {
        $addToSet: {
            "roleNames": {
                $each: [
                    "ROLE_UPDATE_UNITS_METADATA"
                ]
            }
        }
    }
);
