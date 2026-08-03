dbIam.profiles.updateOne(
    { "_id": "system_dsl" },
    {
        $addToSet: {
            "roles": {
                $each: [
                    { "name": "ROLE_GET_UNITS" }
                ]
            }
        }
    }
);
