dbIam.profiles.updateMany(
    {
        "name": { $regex: "Archiviste administrateur" },
        "applicationName": "ARCHIVE_SEARCH_MANAGEMENT_APP"
    },
    {
        $addToSet: {
            "roles": {
                "name": "ROLE_TRANSFER_ACKNOWLEDGMENT"
            }
        },
        $set: {
            "description": "Profil pour la recherche et consultation des archives dans Vitam avec mise à jour des règles, export DIP, opérations d'élimination, reclassement, demande de transfert et acquittement de transfert"
        }
    }
);
