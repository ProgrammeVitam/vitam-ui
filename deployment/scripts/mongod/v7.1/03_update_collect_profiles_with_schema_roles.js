dbIam.profiles.updateMany(
    { "applicationName": "COLLECT_APP" },
    {
        $addToSet: {
            "roles":
                { "name": "ROLE_GET_SCHEMAS" }
        }
    }
);
