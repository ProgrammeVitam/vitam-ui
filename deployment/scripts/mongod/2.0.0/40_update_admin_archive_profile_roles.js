// -------- ARCHIVE_SEARCH ADMINISTRATOR PROFILE -----
dbIam.profiles.updateOne(
    { "_id": "system_archive_search_profile_archiviste_administrator" },
    {
        $addToSet: {
            "roles": {
                $each: [
                    { "name": "ROLE_UPDATE_MANAGEMENT_RULES" }
                ]
            }
        }
    }
);
