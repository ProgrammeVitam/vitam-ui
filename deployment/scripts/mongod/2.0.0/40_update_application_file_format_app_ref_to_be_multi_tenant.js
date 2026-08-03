// =============== REFERENTIAL : FILE_FORMATS_APP ==========
dbIam.applications.updateOne(
    { "identifier": "FILE_FORMATS_APP" },
    {
        $set: {
            "hasTenantList": true
        }
    }
);
