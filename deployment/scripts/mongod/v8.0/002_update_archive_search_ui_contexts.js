dbSecurity.contexts.updateOne(
    { "_id": "ui_archive_search_context" },
    {
        $addToSet: {
            "roleNames": {
                $each: [
                    "ROLE_ARCHIVE_SEARCH_GET_ARCHIVE_SEARCH",
                    "ROLE_ARCHIVE_SEARCH_GET_ARCHIVE_BINARY",
                    "ROLE_ARCHIVE_SEARCH_UPDATE_ARCHIVE_UNIT",
                    "ROLE_GET_ONTOLOGIES"
                ]
            }
        }
    }
);

dbSecurity.contexts.updateOne(
    { "_id": "ui_archive_search_context" },
    {
        $pull: {
            roleNames: {
                $in: [
                    "ROLE_GET_ALL_ARCHIVE_SEARCH",
                    "ROLE_SEARCH_WITH_RULES",
                    "ROLE_CREATE_ARCHIVE_SEARCH",
                    "ROLE_UPDATE_UNIT_DESC_METADATA",
                    "ROLE_GET_ARCHIVE_SEARCH"
                ]
            }
        }
    }
);
