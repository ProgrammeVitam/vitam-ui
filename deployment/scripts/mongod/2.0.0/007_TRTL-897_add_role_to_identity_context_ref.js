dbIam.profiles.updateMany(
    { "applicationName": "HIERARCHY_PROFILE_APP" },
    {
        $push: {
            "roles": {
                $each: [
                    { "name": "ROLE_UPDATE_ME_USERS" }
                ]
            }
        }
    }
);
