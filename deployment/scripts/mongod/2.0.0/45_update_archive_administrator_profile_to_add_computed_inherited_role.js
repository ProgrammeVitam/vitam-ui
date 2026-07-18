dbIam.profiles.updateOne(
    { "_id": "system_archive_search_profile_archiviste_administrator" },
    {
        $addToSet: {
            "roles": {
                $each: [
                    { "name": "ROLE_COMPUTED_INHERITED_RULES" }
                ]
            }
        }
    }
);
