dbIam.profiles.updateMany(
    { "applicationName": "ARCHIVE_SEARCH_MANAGEMENT_APP" },
    {
        $addToSet: {
            "roles": {
                "name": "ROLE_GET_RULES"
            }
        }
    }
);
