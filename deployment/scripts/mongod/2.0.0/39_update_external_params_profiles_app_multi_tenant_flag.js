// -------- EXTERNAL_PARAM_PROFILE_APP  -----
dbIam.profiles.updateOne(
    { "applicationName": "EXTERNAL_PARAM_PROFILE_APP" },
    {
        $addToSet: {
            "roles": {
                $each: [
                    {
                        "name": "ROLE_GET_PROFILES",
                        "name": "ROLE_UPDATE_PROFILES",
                        "name": "ROLE_LOGBOOKS"
                    }
                ]
            }
        }
    }
);
