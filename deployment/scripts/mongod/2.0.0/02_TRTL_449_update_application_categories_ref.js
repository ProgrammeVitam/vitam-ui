db = db.getSiblingDB('iam')

print("Start 06_TRTL_449_update_application_categories_ref.js");

db.applications.updateOne({
    "identifier" : "ACCOUNTS_APP",
    "category": "users",
}, {
    $set: {
        "category": "ingest_and_consultation"
    },
});

db.applications.updateOne({
    "identifier" : "PROFILES_APP",
    "category": "settings",
}, {
    $set: {
        "category": "organization_and_user_rights"
    },
});

db.applications.updateOne({
    "identifier" : "USERS_APP",
    "category": "administrators",
}, {
    $set: {
        "category": "organization_and_user_rights"
    },
});

db.applications.updateOne({
    "identifier" : "GROUPS_APP",
    "category": "settings",
}, {
    $set: {
        "category": "organization_and_user_rights"
    },
});

db.applications.updateOne({
    "identifier" : "CUSTOMERS_APP",
    "category": "settings",
}, {
    $set: {
        "category": "organization_and_user_rights"
    },
});

db.applications.updateOne({
    "identifier" : "SUBROGATIONS_APP",
    "category": "administrators",
}, {
    $set: {
        "category": "organization_and_user_rights"
    },
});


db.applications.updateOne({
    "identifier" : "HIERARCHY_PROFILE_APP",
    "category": "settings",
}, {
    $set: {
        "category": "organization_and_user_rights"
    },
});

db.applications.updateOne({
    "identifier": "INGEST_APP",
    "category": "referential",
}, {
    $set: {
        "category": "security_and_application_rights"
    },
});

db.applications.updateOne({
    "identifier": "ACCESS_APP",
    "category": "referential",
}, {
    $set: {
        "category": "security_and_application_rights"
    },
});

db.applications.updateOne({
    "identifier": "SECURITY_PROFILES_APP",
    "category": "referential",
}, {
    $set: {
        "category": "security_and_application_rights"
    },
});

db.applications.updateOne({
    "identifier": "AUDIT_APP",
    "category": "opaudit",
}, {
    $set: {
        "category": "supervision_and_audits"
    },
});

db.applications.updateOne({
    "identifier": "PROBATIVE_VALUE_APP",
    "category": "opaudit",
}, {
    $set: {
        "category": "supervision_and_audits"
    },
});

db.applications.updateOne({
    "identifier": "SECURE_APP",
    "category": "opaudit",
}, {
    $set: {
        "category": "supervision_and_audits"
    },
});

db.applications.updateOne({
    "identifier": "LOGBOOK_OPERATION_APP",
    "category": "referential",
}, {
    $set: {
        "category": "supervision_and_audits"
    },
});

db.applications.updateOne({
    "identifier": "DSL_APP",
    "category": "techadmin",
}, {
    $set: {
        "category": "supervision_and_audits"
    },
});

print("End 06_TRTL_449_uate_application_categories_ref.js");
