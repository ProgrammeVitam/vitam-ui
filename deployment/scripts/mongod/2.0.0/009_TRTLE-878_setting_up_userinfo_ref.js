dbIam.profiles.updateMany(
    { "applicationName": "USERS_APP" },
    {
        $addToSet: {
            "roles": {
                $each: [
                    { "name": "ROLE_GET_USER_INFOS" },
                    { "name": "ROLE_CREATE_USER_INFOS" },
                    { "name": "ROLE_UPDATE_USER_INFOS" }
                ]
            }
        }
    }
);
