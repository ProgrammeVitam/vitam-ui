dbIam.customers.updateOne(
    { "_id": "system_customer" },
    {
        $set: {
            "portalTitles": {
                "FRENCH": "Portail des applications de l'archivage",
                "ENGLISH": "Archiving Applications Portal"
            },
            "portalMessages": {
                "FRENCH": "Profitez d'un portail unique pour rechercher dans les archives de vos coffres, pour déposer des éléments en toute sécurité et pour imprimer des étiquettes en quelques clics.",
                "ENGLISH": "Take advantage of a single portal to search your safe archives, to deposit items safely and to print labels in a few clicks"
            }
        }
    }
);
