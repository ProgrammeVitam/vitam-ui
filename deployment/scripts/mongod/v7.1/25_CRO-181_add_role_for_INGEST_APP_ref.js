dbIam.profiles.updateMany(
    { "applicationName": "INGEST_APP" },
    {
        $addToSet: {
            "roles": {
                $each: [
                    { "name": "ROLE_GET_MANAGEMENT_CONTRACT" },
                    { "name": "ROLE_GET_FILE_FORMATS" },
                    { "name": "ROLE_GET_UNITS" },
                    { "name": "ROLE_GET_EXTERNAL_PARAMS" }
                ]
            }
        }
    }
);
