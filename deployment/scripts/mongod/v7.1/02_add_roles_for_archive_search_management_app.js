// -------- ADD MANAGEMENT CONTRACT APPLICATION ROLES TO UI REFERENTIAL CONTEXT -----
dbSecurity.contexts.updateOne(
    { "_id": "ui_archive_search_context" },
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
