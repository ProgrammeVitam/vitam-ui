dbIam.profiles.updateMany(
    { "applicationName": "COLLECT_APP" },
    {
        $addToSet: {
            "roles": {
                $each: [
                    { "name": "ROLE_SEND_TRANSACTIONS" },
                    { "name": "ROLE_CLOSE_TRANSACTIONS" }
                ]
            }
        }
    }
);
