dbIam.applications.updateOne(
    { "identifier": "ACCESSION_REGISTER_APP" },
    {
        $set: {
            "icon": "vitamui-icon vitamui-icon-accession-register"
        }
    }
);

dbIam.applications.updateOne(
    { "identifier": "LOGBOOK_MANAGEMENT_OPERATION_APP" },
    {
        $set: {
            "icon": "vitamui-icon vitamui-icon-logbook-management-operation"
        }
    }
);
