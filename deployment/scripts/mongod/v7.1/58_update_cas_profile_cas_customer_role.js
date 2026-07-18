dbIam.profiles.updateOne(
    { "_id": "cas_profile" },
    {
        $addToSet: {
            "roles": {
                $each: [
                    { "name": "ROLE_CAS_CUSTOMERS" }
                ]
            }
        }
    }
);
