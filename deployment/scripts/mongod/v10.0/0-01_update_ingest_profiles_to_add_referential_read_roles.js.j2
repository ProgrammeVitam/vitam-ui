dbIam = db.getSiblingDB('{{ mongodb.iam.db | default('iam') }}')
dbSecurity = db.getSiblingDB('{{ mongodb.security.db | default('security') }}')

print("START v10.0.0-01_update_ingest_profiles_to_add_referential_read_roles.js");

// Add referential read roles to all ingest profiles
dbIam.profiles.updateMany({
    "applicationName": "INGEST_MANAGEMENT_APP"
}, {
    "$addToSet": {
        "roles": {
            $each: [
                { "name": "ROLE_GET_AGENCIES" },
                { "name": "ROLE_GET_INGEST_CONTRACTS" },
                { "name": "ROLE_GET_ARCHIVE_PROFILES" }
            ]
        }
    }
});

// Add referential read roles to ingest-ui context
dbSecurity.contexts.updateOne({
    "_id": "ui_ingest_context"
}, {
    $addToSet: {
        "roleNames": {
            $each: [
                "ROLE_GET_AGENCIES",
                "ROLE_GET_INGEST_CONTRACTS",
                "ROLE_GET_ARCHIVE_PROFILES"
            ]
        }
    }
});

print("END v10.0.0-01_update_ingest_profiles_to_add_referential_read_roles.js");
