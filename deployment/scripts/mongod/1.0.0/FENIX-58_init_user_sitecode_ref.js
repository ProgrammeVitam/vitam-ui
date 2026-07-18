// Update all users without siteCode : add an empty siteCode
dbIam.users.updateMany(
    { "siteCode": { $exists: false } },
    {
        $set: {
            "siteCode": ""
        }
    }
);
