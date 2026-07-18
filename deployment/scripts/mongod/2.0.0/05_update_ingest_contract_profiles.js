dbIam.profiles.updateOne(
    { "_id": "system_ingest_contract" },
    {
        $addToSet: {
            "roles": {
                $each: [
                    { "name": "ROLE_GET_ARCHIVE_PROFILES" }
                ]
            }
        }
    }
);

dbIam.profiles.updateOne(
    { "_id": "system_customer_profile" },
    {
        $addToSet: {
            "roles": {
                $each: [
                    { "name": "ROLE_GET_ARCHIVE_PROFILES" }
                ]
            }
        }
    }
);
