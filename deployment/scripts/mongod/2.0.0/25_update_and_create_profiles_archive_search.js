// -------- ARCHIVE_SEARCH PROFILE WITH RULES MANAGEMENT -----
dbIam.profiles.updateOne(
    { "_id": "system_archive_search_profile" },
    {
        $addToSet: {
            "roles": {
                $each: [
                    { "name": "ROLE_SEARCH_WITH_RULES" }
                ]
            }
        }
    }
);

dbIam.profiles.updateOne(
    { "_id": "system_archive_search_profile" },
    {
        $set: {
            "description": "Profil pour la recherche et consultation des archives avec mises à jour des règles",
            "name": "Profil pour la recherche et consultation des archives avec mises à jour des règles"
        }
    }
);
