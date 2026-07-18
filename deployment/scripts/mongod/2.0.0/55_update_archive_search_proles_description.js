// ---- UPDATE DESCRIPTION OF ALL CONSULTATION PROFILES -----//
dbIam.profiles.updateMany(
    {
        "name": { $regex: "Consultation" },
        "applicationName": "ARCHIVE_SEARCH_MANAGEMENT_APP"
    },
    {
        $set: {
            "description": "Profil pour la recherche et consultation des archives dans Vitam sans mises à jour des règles, sans export DIP et sans élimination"
        },
    }
);

// ---- UPDATE DESCRIPTION OF ALL ARCHIVISTE PROFILES -----//
dbIam.profiles.updateMany(
    {
        "name": { $regex: "Archiviste" },
        "applicationName": "ARCHIVE_SEARCH_MANAGEMENT_APP"
    },
    {
        $set: {
            "description": "Profil pour la recherche et consultation des archives dans Vitam sans mises à jour des règles de gestion, avec export DIP et sans élimination"
        },
    }
);

// ---- UPDATE DESCRIPTION OF ALL ARCHIVISTE ADMINISTRATEUR PROFILES -----//
dbIam.profiles.updateMany(
    {
        "name": { $regex: "Archiviste administrateur" },
        "applicationName": "ARCHIVE_SEARCH_MANAGEMENT_APP"
    },
    {
        $set: {
            "description": "Profil pour la recherche et consultation des archives dans Vitam avec mises à jour des règles, export DIP et opérations d'élimination"
        },
    }
);
