dbIam.groups.updateOne(
    {
        "_id": "5c79022e7884583d1ebb6e5d0bc0121822684250a3fd2996fd93c04634363363",
        "profileIds": { $nin: ["system_ingest_profile"] }
    },
    {
        $addToSet: { "profileIds": "system_ingest_profile" }
    },
    {
        "upsert": false
    }
);
