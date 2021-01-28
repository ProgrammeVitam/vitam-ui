db = db.getSiblingDB('iam')

print("Start 03_update_application_for_new_portal_ref.js");

db.applications.update({
    "identifier" : "INGEST_MANAGEMENT_APP",
    "category": "ingests"
}, {
    $set: {
        "category": "ingest_and_consultation"
    },
});

db.applications.update({
    "identifier" : "INGEST_MANAGEMENT_APP",
    "hasTenantList": false,
}, {
    $set: {
        "hasTenantList": true
    },
});

db.applications.update({
    "identifier" : "AGENCIES_APP",
    "hasTenantList": false,
}, {
    $set: {
        "hasTenantList": true
    },
});

db.applications.update({
    "identifier" : "SECURE_APP",
    "hasTenantList": false,
}, {
    $set: {
        "hasTenantList": true
    },
});


db.applications.update({
    "identifier" : "DSL_APP",
    "hasTenantList": false,
}, {
    $set: {
        "hasTenantList": true
    },
});

db.applications.update({
    "identifier" : "LOGBOOK_OPERATION_APP",
    "hasTenantList": false,
}, {
    $set: {
        "hasTenantList": true
    },
});
