// -------- EXTERNAL_PARAM_PROFILE_APP  -----
dbIam.applications.updateOne(
    { "identifier": "EXTERNAL_PARAM_PROFILE_APP" },
    {
        $set: {
            "hasTenantList": true
        }
    }
);
