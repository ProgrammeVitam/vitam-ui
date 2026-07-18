dbIam.profiles.updateOne(
    { "_id": "system_archive_search_profile_archiviste_administrator" },
    {
        $addToSet: {
            "roles": {
                $each: [
                    { "name": "ROLE_GET_RULES" }
                ]
            }
        }
    });

dbIam.profiles.updateOne(
    { "_id": "system_archive_search_profile_consultation" },
    {
        $addToSet: {
            "roles": {
                $each: [
                    { "name": "ROLE_GET_RULES" }
                ]
            }
        }
    });

dbIam.profiles.updateOne(
    { "_id": "system_archive_search_profile_archiviste" },
    {
        $addToSet: {
            "roles": {
                $each: [
                    { "name": "ROLE_GET_RULES" }
                ]
            }
        }
    }
);
