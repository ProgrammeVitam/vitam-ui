dbIam.profiles.updateMany(
    { "applicationName": "ARCHIVE_SEARCH_MANAGEMENT_APP" },
    {
        "$addToSet": {
            "roles": {
                $each: [
                    { "name": "ROLE_GET_ARCHIVE_PROFILES_UNIT" },
                    { "name": "ROLE_GET_AGENCIES" },
                    { "name": "ROLE_GET_ARCHIVE_PROFILES_UNIT" },
                    { "name": "ROLE_GET_FILLING_PLAN_ACCESS" }
                ]
            }
        }
    }
);

dbSecurity.contexts.updateOne(
    { "_id": "ui_archive_search_context" },
    {
        $addToSet: {
            roleNames: {
                $each: [
                    "ROLE_GET_ARCHIVE_PROFILES_UNIT",
                    "ROLE_GET_AGENCIES",
                    "ROLE_GET_ARCHIVE_PROFILES_UNIT",
                    "ROLE_GET_FILLING_PLAN_ACCESS"
                ]
            }
        }
    }
);
