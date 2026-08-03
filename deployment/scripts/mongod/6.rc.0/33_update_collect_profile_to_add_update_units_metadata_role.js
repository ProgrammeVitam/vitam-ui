dbIam.profiles.updateMany(
    { "applicationName": "COLLECT_APP" },
    {
        $addToSet: {
            "roles": {
                $each: [
                    { "name": "ROLE_UPDATE_UNITS_METADATA" }
                ]
            }
        }
    }
);
