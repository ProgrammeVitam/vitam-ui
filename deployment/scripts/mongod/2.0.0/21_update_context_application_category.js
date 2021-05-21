db = db.getSiblingDB('iam')

print("Start 08_update_context_application_category.js");

db.applications.update({
    "identifier" : "CONTEXTS_APP",
    "category": "referential",
}, {
    $set: {
        "category": "security_and_application_rights"
    },
});

print("End 08_update_context_application_category.js");
