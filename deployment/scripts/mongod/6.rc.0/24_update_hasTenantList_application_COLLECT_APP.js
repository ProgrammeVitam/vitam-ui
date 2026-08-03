dbIam.applications.updateOne(
    { "identifier": "COLLECT_APP" },
    {
        $set: {
            "hasTenantList": true
        }
    }
);
