
// We need a regex for newest profiles created from customer-init because it contains the tenantId...
dbIam.profiles.updateMany(
    {
        applicationName: "COLLECT_APP",
        $or: [
            { name: { $regex: "^Archiviste - Administrateur" } },
            { name: { $regex: "^Archiviste - gestion et collecte" } }
        ]
    },
    {
        $addToSet: {
            roles: {
                $each: [
                    { name: "ROLE_DOWNLOAD_SIP_TRANSACTIONS" }
                ]
            }
        }
    }
);

dbSecurity.contexts.updateOne(
    { "_id": "ui_collect_context" },
    {
        $addToSet: {
            "roleNames": {
                $each: [
                    "ROLE_DOWNLOAD_SIP_TRANSACTIONS"
                ]
            }
        }
    }
);
