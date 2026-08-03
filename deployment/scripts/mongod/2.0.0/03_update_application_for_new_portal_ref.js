dbIam.applications.updateOne(
    {
        "identifier": "INGEST_MANAGEMENT_APP",
        "category": "ingests"
    },
    {
        $set: {
            "category": "ingest_and_consultation"
        }
    }
);

dbIam.applications.updateOne(
    {
        "identifier": "INGEST_MANAGEMENT_APP",
        "hasTenantList": false,
    },
    {
        $set: {
            "hasTenantList": true
        }
    }
);

dbIam.applications.updateOne(
    {
        "identifier": "AGENCIES_APP",
        "hasTenantList": false,
    },
    {
        $set: {
            "hasTenantList": true
        }
    }
);

dbIam.applications.updateOne(
    {
        "identifier": "SECURE_APP",
        "hasTenantList": false,
    },
    {
        $set: {
            "hasTenantList": true
        }
    }
);

dbIam.applications.updateOne(
    {
        "identifier": "DSL_APP",
        "hasTenantList": false,
    },
    {
        $set: {
            "hasTenantList": true
        }
    }
);

dbIam.applications.updateOne(
    {
        "identifier": "LOGBOOK_OPERATION_APP",
        "hasTenantList": false,
    },
    {
        $set: {
            "hasTenantList": true
        }
    }
);
