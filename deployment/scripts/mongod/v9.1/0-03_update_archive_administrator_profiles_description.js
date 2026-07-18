dbIam.profiles.updateMany(
    {
        "name": { $regex: "Archiviste administrateur" },
        "applicationName": "ARCHIVE_SEARCH_MANAGEMENT_APP"
    },
    {
        $set: {
            "description": "Profil ayant tous les droits de recherche et consultation des archives dont la modification, l'élimination, le transfert et la réattribution"
        }
    }
);
