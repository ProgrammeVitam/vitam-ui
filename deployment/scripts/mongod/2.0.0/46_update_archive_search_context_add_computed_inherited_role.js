dbSecurity.contexts.updateOne(
    { "_id": "ui_archive_search_context" },
    {
        $addToSet: {
            "roleNames": {
                $each: [
                    "ROLE_COMPUTED_INHERITED_RULES"
                ]
            }
        }
    }
);
