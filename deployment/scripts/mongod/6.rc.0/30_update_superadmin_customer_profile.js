dbIam.profiles.updateOne(
    { "_id": "system_customer_profile" },
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
