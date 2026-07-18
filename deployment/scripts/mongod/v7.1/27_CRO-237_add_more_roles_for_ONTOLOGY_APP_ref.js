dbIam.profiles.updateMany(
    { "applicationName": "ONTOLOGY_APP" },
    {
        $addToSet: {
            "roles": {
                $each: [
                    { "name": "ROLE_UPDATE_ONTOLOGIES" }
                ]
            }
        }
    }
);

dbSecurity.contexts.updateOne(
    { "_id": "ui_referential_context" },
    {
        $addToSet: {
            "roleNames": "ROLE_UPDATE_ONTOLOGIES"
        }
    }
);
