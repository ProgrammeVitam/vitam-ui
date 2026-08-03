dbIam.applications.updateOne(
    {
        "identifier": "ARCHIVE_SEARCH_MANAGEMENT_APP"
    },
    {
        $set: {
            "tooltip": "Recherche, consultation et gestion des archives",
            "name": "Recherche, consultation et gestion des archives"
        },
    }
);
