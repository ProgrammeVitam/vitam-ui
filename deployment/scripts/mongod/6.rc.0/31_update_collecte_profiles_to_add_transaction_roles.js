dbIam.profiles.updateMany(
    { "applicationName": "COLLECT_APP" },
    {
        $addToSet: {
            "roles": {
                $each: [
                    { "name": "ROLE_CREATE_TRANSACTIONS" },
                    { "name": "ROLE_UPDATE_TRANSACTIONS" },
                    { "name": "ROLE_GET_TRANSACTIONS" },
                    { "name": "ROLE_DELETE_TRANSACTIONS" }
                ]
            }
        }
    }
);
