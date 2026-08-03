// Add role to profile
dbIam.profiles.updateOne(
    { "_id": "cas_profile" },
    {
        $addToSet: {
            "roles": {
                $each: [
                    { "name": "ROLE_PROVISIONING_USER" }
                ]
            }
        }
    }
);
