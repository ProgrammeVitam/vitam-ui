dbIam = db.getSiblingDB('{{ mongodb.iam.db | default('iam') }}')
dbSecurity = db.getSiblingDB('{{ mongodb.security.db | default('security') }}')

print("START v9.0.0-02_add_collect_external_referential_roles");

// Add external referential roles to collect profiles
dbIam.profiles.updateMany({
    applicationName: "COLLECT_APP"
}, {
    $addToSet: {
        roles: {
            $each: [
                { name: "ROLE_GET_EXTERNAL_REFERENTIAL_CONFIG" },
                { name: "ROLE_GET_EXTERNAL_REFERENTIAL_AGENCIES" },
                { name: "ROLE_GET_EXTERNAL_REFERENTIAL_ARCHIVE_PROFILES" },
                { name: "ROLE_GET_EXTERNAL_REFERENTIAL_INGEST_CONTRACTS" }
            ]
        }
    }
});

// Add external referential roles to collect-ui context
dbSecurity.contexts.updateOne({
    "_id": "ui_collect_context"
}, {
    $addToSet: {
        "roleNames":  {
            $each: [
                "ROLE_GET_EXTERNAL_REFERENTIAL_CONFIG",
                "ROLE_GET_EXTERNAL_REFERENTIAL_AGENCIES",
                "ROLE_GET_EXTERNAL_REFERENTIAL_ARCHIVE_PROFILES",
                "ROLE_GET_EXTERNAL_REFERENTIAL_INGEST_CONTRACTS"
            ]
        }
    }
});

print("END v9.0.0-02_add_collect_external_referential_roles");
