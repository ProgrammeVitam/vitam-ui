dbSecurity.contexts.updateOne(
    { "_id": "ui_archive_search_context" },
    {
        $addToSet: {
            "roleNames": "ROLE_UPDATE_UNIT_DESC_METADATA"
        }
    }
);
