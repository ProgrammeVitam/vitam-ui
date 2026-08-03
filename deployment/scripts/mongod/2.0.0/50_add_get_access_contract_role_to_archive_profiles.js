//----------- Add ROLE_GET_ACCESS_CONTRACTS to all archive search profiles  --------------
dbIam.profiles.updateMany(
    { "applicationName": "ARCHIVE_SEARCH_MANAGEMENT_APP" },
    {
        $addToSet: {
            "roles": {
                "name": "ROLE_GET_ACCESS_CONTRACTS"
            }
        }
    }
);
