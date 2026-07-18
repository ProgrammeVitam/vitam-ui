// ------ ACCESS CONTRACT PROFILE --------
dbIam.profiles.updateOne(
    { "_id": "system_access_contract" },
    {
        $addToSet: {
            "roles": {
                $each: [
                    { "name": "ROLE_GET_FILLING_PLAN_ACCESS" }
                ]
            }
        }
    }
);

// ---------- INGEST CONTRACT PROFILE -----------
dbIam.profiles.updateOne(
    { "_id": "system_ingest_contract" },
    {
        $addToSet: {
            "roles": {
                $each: [
                    { "name": "ROLE_GET_FILLING_PLAN_ACCESS" }
                ]
            }
        }
    }
);
