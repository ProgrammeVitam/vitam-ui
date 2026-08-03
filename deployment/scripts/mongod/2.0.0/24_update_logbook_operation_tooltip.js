dbIam.applications.updateOne(
    { "identifier": "LOGBOOK_MANAGEMENT_OPERATION_APP" },
    {
        $set: {
            "tooltip": "Consulter et gérer l'ensemble des opérations qui sont en cours"
        },
    }
);
