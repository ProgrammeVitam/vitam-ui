// ---- ROLE_GET_UNITS will be added to profiles including COLLECT_APP --- //
dbIam.profiles.updateMany(
    { "applicationName": "COLLECT_APP" },
    {
        $addToSet: {
            "roles": {
                $each: [
                    { "name": "ROLE_GET_UNITS" }
                ]
            }
        }
    }
);

dbSecurity.contexts.updateOne(
    { "_id": "ui_collect_context" },
    {
        $addToSet: {
            "roleNames": "ROLE_GET_UNITS"
        }
    }
);
