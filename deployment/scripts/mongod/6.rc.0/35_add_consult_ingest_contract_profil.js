// Create new profile CONSULTATION for INGEST_APP
var lastIdProfile = dbIam.getCollection('sequences').findOne({ '_id': 'profile_identifier' }).sequence;

dbIam.tenants.find({ "identifier": { $gte: 0 } }).forEach(function (tenant) {

    dbIam.profiles.insertOne({
        "_id": "PROFIL_" + tenant.identifier + "-INGEST_APP-CONSULTATION",
        "identifier": NumberInt(lastIdProfile++),
        "name": "Profil pour la consultation des contrats d'entrée",
        "description": "Profil pour la consultation des contrats d'entrée dans Vitam sans mises à jour des contrats",
        "tenantIdentifier": NumberInt(tenant.identifier),
        "applicationName": "INGEST_APP",
        "level": "",
        "enabled": true,
        "readonly": false,
        "customerId": tenant.customerId,
        "roles": [
            {
                "name": "ROLE_GET_INGEST_CONTRACTS"
            },
            {
                "name": "ROLE_GET_FILLING_PLAN_ACCESS"
            },
            {
                "name": "ROLE_GET_MANAGEMENT_CONTRACTS"
            },
            {
                "name": "ROLE_GET_ARCHIVE_PROFILES"
            }
        ]
    });

});

// Update sequence
dbIam.sequences.updateOne(
    { "_id": "profile_identifier" },
    {
        $set: {
            "sequence": NumberInt(lastIdProfile)
        }
    }
);
