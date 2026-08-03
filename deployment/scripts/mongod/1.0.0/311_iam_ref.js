dbIam.profiles.updateOne(
    { "_id": "system_context" },
    {
        $set: {
            "roles": [
                { "name": "ROLE_GET_CONTEXTS" },
                { "name": "ROLE_CREATE_CONTEXTS" },
                { "name": "ROLE_UPDATE_CONTEXTS" },
                { "name": "ROLE_GET_CUSTOMERS" },
                { "name": "ROLE_GET_TENANTS" },
                { "name": "ROLE_GET_ALL_TENANTS" }
            ]
        }
    }
);

dbIam.profiles.updateOne(
    { "_id": "system_agencies" },
    {
        $set: {
            "roles": [
                { "name": "ROLE_GET_AGENCIES" },
                { "name": "ROLE_CREATE_AGENCIES" },
                { "name": "ROLE_UPDATE_AGENCIES" },
                { "name": "ROLE_DELETE_AGENCIES" },
                { "name": "ROLE_EXPORT_AGENCIES" },
                { "name": "ROLE_IMPORT_AGENCIES" }
            ]
        }
    }
);

dbIam.profiles.updateOne(
    { "_id": "system_file_format" },
    {
        $set: {
            "roles": [
                { "name": "ROLE_GET_FILE_FORMATS" },
                { "name": "ROLE_CREATE_FILE_FORMATS" },
                { "name": "ROLE_UPDATE_FILE_FORMATS" },
                { "name": "ROLE_DELETE_FILE_FORMATS" },
                { "name": "ROLE_IMPORT_FILE_FORMATS" }
            ]
        }
    }
);

dbIam.profiles.updateOne(
    { "_id": "system_ontology" },
    {
        $set: {
            "roles": [
                { "name": "ROLE_GET_ONTOLOGIES" },
                { "name": "ROLE_CREATE_ONTOLOGIES" },
                { "name": "ROLE_DELETE_ONTOLOGIES" },
                { "name": "ROLE_IMPORT_ONTOLOGIES" }
            ]
        }
    }
);
