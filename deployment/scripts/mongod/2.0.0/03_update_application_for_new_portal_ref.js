db = db.getSiblingDB('iam')

print("Start 03_update_application_for_new_portal_ref.js");

db.applications.updateOne({
    "identifier" : "INGEST_MANAGEMENT_APP",
    "category": "ingests"
}, {
    $set: {
        "category": "ingest_and_consultation"
    },
});

db.applications.updateOne({
    "identifier" : "INGEST_MANAGEMENT_APP",
    "hasTenantList": false,
}, {
    $set: {
        "hasTenantList": true
    },
});

db.applications.updateOne({
    "identifier" : "AGENCIES_APP",
    "hasTenantList": false,
}, {
    $set: {
        "hasTenantList": true
    },
});

db.applications.updateOne({
    "identifier" : "SECURE_APP",
    "hasTenantList": false,
}, {
    $set: {
        "hasTenantList": true
    },
});


db.applications.updateOne({
    "identifier" : "DSL_APP",
    "hasTenantList": false,
}, {
    $set: {
        "hasTenantList": true
    },
});

db.applications.updateOne({
    "identifier" : "LOGBOOK_OPERATION_APP",
    "hasTenantList": false,
}, {
    $set: {
        "hasTenantList": true
    },
});
