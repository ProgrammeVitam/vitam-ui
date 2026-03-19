
print("START v9.0-1-04_update_collecte_profiles_to_add_download_transaction_role.js");

dbIam = db.getSiblingDB('{{ mongodb.iam.db | default('iam') }}')

// We need a regex for newest profiles created from customer-init because it contains the tenantId...
dbIam.profiles.updateMany({
    applicationName: "COLLECT_APP",
    $or: [
            { name: { $regex: "^Archiviste - Administrateur" } },
            { name: { $regex: "^Archiviste - gestion et collecte" } }
    ]
}, {
    $addToSet: {
        roles: {
            $each: [
                { name: "ROLE_DOWNLOAD_SIP_TRANSACTIONS" }
            ]
        }
    }
});

dbSecurity = db.getSiblingDB('{{ mongodb.security.db | default('security') }}')
dbSecurity.contexts.updateOne({
    "_id": "ui_collect_context"
}, {
    $addToSet: {
        "roleNames": {
            $each: [
                "ROLE_DOWNLOAD_SIP_TRANSACTIONS"
            ]
        }
    }
});

print("END v9.0-1-04_update_collecte_profiles_to_add_download_transaction_role.js");
