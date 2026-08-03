// -------- ADD MANAGEMENT CONTRACT APPLICATION ROLES TO UI REFERENTIAL CONTEXT -----
dbIam.profiles.updateMany(
    { "applicationName": "AUDIT_APP" },
    {
        $addToSet: {
            "roles":
                { "name": "ROLE_GET_OPERATIONS" }
        }
    }
);
