dbIam.profiles.updateOne(
    { "_id": "system_rules" },
    {
        $addToSet: {
            "roles": {
                $each: [
                    { "name": "ROLE_IMPORT_RULES" },
                    { "name": "ROLE_EXPORT_RULES" }
                ]
            }
        }
    }
);
