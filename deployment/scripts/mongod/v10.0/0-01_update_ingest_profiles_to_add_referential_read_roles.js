// Add referential read roles to all ingest profiles
dbIam.profiles.updateMany(
    { "applicationName": "INGEST_MANAGEMENT_APP" },
    {
        $addToSet: {
            "roles": {
                $each: [
                    { "name": "ROLE_GET_AGENCIES" },
                    { "name": "ROLE_GET_INGEST_CONTRACTS" },
                    { "name": "ROLE_GET_ARCHIVE_PROFILES" }
                ]
            }
        }
    }
);

// Add referential read roles to ingest-ui context
dbSecurity.contexts.updateOne(
    { "_id": "ui_ingest_context" },
    {
        $addToSet: {
            "roleNames": {
                $each: [
                    "ROLE_GET_AGENCIES",
                    "ROLE_GET_INGEST_CONTRACTS",
                    "ROLE_GET_ARCHIVE_PROFILES"
                ]
            }
        }
    }
);
