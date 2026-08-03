dbIam.profiles.updateMany(
    { "applicationName": "USERS_APP" },
    {
        $addToSet: {
            "roles": {
                $each: [
                    { "name": "ROLE_GET_USER_INFOS" }
                ]
            }
        }
    }
);

dbIam.profiles.updateMany(
    {
        "applicationName": "USERS_APP",
        "roles": {
            "name": "ROLE_CREATE_USERS"
        }
    },
    {
        $addToSet: {
            "roles": {
                $each: [
                    { "name": "ROLE_CREATE_USER_INFOS" }
                ]
            }
        }
    }
);

dbIam.profiles.updateMany(
    {
        "applicationName": "USERS_APP",
        "roles": {
            "name": "ROLE_UPDATE_USERS"
        }
    },
    {
        $addToSet: {
            "roles": {
                $each: [
                    { "name": "ROLE_UPDATE_USER_INFOS" }
                ]
            }
        }
    }
);
