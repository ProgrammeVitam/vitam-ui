var lastIdProfile = dbIam.getCollection('sequences').findOne({ '_id': 'profile_identifier' }).sequence;

dbIam.tenants.find({ "identifier": { $gte: 0 } }).forEach(function (tenant) {

    dbIam.profiles.insertOne({
        "_id": "PROFIL_" + tenant.identifier + "-RULES_APP-CONSULTATION",
        "identifier": NumberInt(lastIdProfile++),
        "name": "Profil consultation des règles de gestion",
        "description": "Profil pour la consultation des règles de gestion dans Vitam sans mises à jour des règles",
        "tenantIdentifier": NumberInt(tenant.identifier),
        "applicationName": "RULES_APP",
        "level": "",
        "enabled": true,
        "readonly": false,
        "customerId": tenant.customerId,
        "roles": [
            {
                "name": "ROLE_GET_RULES"
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
