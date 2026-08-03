print("Add role TRANSFER_REQUEST to archive search UI context");
dbSecurity.contexts.updateOne(
    { "_id": "ui_archive_search_context" },
    {
        $addToSet: {
            "roleNames": "ROLE_TRANSFER_REQUEST"
        }
    }
);

print("Add role TRANSFER_REQUEST to Archivist Admin roles");
dbIam.profiles.updateMany(
    {
        "name": { $regex: "Archiviste administrateur" },
        "applicationName": "ARCHIVE_SEARCH_MANAGEMENT_APP"
    },
    {
        $addToSet: {
            "roles": {
                "name": "ROLE_TRANSFER_REQUEST"
            }
        },
        $set: {
            "description": "Profil pour la recherche et consultation des archives dans Vitam sans mises à jour des règles de gestion, avec requette de transfert, export DIP et sans élimination"
        }
    }
);
