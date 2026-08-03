dbIam.profiles.updateMany(
    { "applicationName": "COLLECT_APP" },
    {
        $addToSet: {
            "roles": {
                $each: [
                    { "name": "ROLE_ABORT_TRANSACTIONS" },
                    { "name": "ROLE_REOPEN_TRANSACTIONS" }
                ]
            }
        }
    }
);
