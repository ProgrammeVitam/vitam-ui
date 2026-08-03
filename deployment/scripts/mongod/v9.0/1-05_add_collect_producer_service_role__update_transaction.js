dbIam.profiles.updateMany(
    {
        applicationName: "COLLECT_APP",
        name: {
            $regex: "Service producteur"
        }
    },
    {
        $addToSet: {
            "roles": {
                "name": "ROLE_UPDATE_TRANSACTIONS"
            }
        }
    }
);
