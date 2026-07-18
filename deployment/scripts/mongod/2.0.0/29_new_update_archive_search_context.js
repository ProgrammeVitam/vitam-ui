dbSecurity.contexts.updateOne(
    { "_id": "ui_archive_search_context" },
    {
        $addToSet: {
            "roleNames": {
                $each: [
                    "ROLE_EXPORT_DIP",
                    "ROLE_GET_EXTERNAL_PARAMS"
                ]
            }
        }
    }
);
