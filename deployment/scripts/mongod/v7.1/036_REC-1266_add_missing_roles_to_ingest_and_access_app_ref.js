dbIam.profiles.updateMany(
    { "applicationName": "ACCESS_APP" }, {
    $push: {
        "roles":
        {
            $each:
                [
                    { "name": "ROLE_GET_UNITS" },
                    { "name": "ROLE_GET_EXTERNAL_PARAMS" }
                ]
        }
    }
}
);

dbIam.profiles.updateMany(
    { "applicationName": "INGEST_APP" },
    {
        $push: {
            "roles":
            {
                $each:
                    [
                        { "name": "ROLE_GET_FILE_FORMATS" },
                        { "name": "ROLE_GET_UNITS" },
                        { "name": "ROLE_GET_EXTERNAL_PARAMS" }
                    ]
            }
        }
    }
);
