// -------- ARCHIVE_SEARCH PROFILE WITH RULES MANAGEMENT AND DIP EXPORT AND ELIMINATION OPERATIONS -----
dbIam.profiles.updateOne(
    { "_id": "system_archive_search_profile" },
    {
        $addToSet: {
            "roles": {
                $each: [
                    {
                        "name": "ROLE_ELIMINATION"
                    }
                ]
            }
        }
    }
);

dbIam.profiles.updateOne(
    { "_id": "system_archive_search_profile" },
    {
        $set: {
            "description": "Profil pour la recherche et consultation des archives avec mises à jour des règles, export DIP et opérations d'éliminations",
            "name": "Profil pour la recherche et consultation des archives avec mises à jour des règles, export DIP et opérations d'éliminations"
        }
    }
);

// -------- ARCHIVE_SEARCH PROFILE WITH RULES MANAGEMENT AND ELIMINATION OPERATIONS AND WITHOUT DIP EXPORT -----
dbIam.profiles.updateOne(
    { "_id": "system_archive_search_profile_with_rules_without_export" },
    {
        $addToSet: {
            "roles": {
                $each: [
                    {
                        "name": "ROLE_ELIMINATION"
                    }
                ]
            }
        }
    }
);

dbIam.profiles.updateOne(
    { "_id": "system_archive_search_profile_with_rules_without_export" },
    {
        $set: {
            "description": "Profil pour la recherche et consultation des archives avec mises à jour des règles  et opérations d'éliminations",
            "name": "Profil pour la recherche et consultation des archives avec mises à jour des règles et opérations d'éliminations"
        }
    }
);
