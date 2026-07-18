db = db.getSiblingDB('iam')

print("START v9.0.0-01_add_consult_ontology_profil.js");

// Update ROLE_IMPORT_ONTOLOGY to ROLE_IMPORT_ONTOLOGIES
db.profiles.updateMany(
  { "roles.name": "ROLE_IMPORT_ONTOLOGY" },
  { $set: { "roles.$[elem].name": "ROLE_IMPORT_ONTOLOGIES" } },
  { arrayFilters: [ { "elem.name": "ROLE_IMPORT_ONTOLOGY" } ] }
)

// Update ontology profile for ONTOLOGY_APP
db.profiles.updateOne(
  { "_id": "system_ontology" },
  {
    $set: {
      "name": "Ontologie - gestion",
      "description": "Profil pour la consultation et la gestion des ontologies et schémas dans Vitam"
    }
  }
);

// Create new profile CONSULTATION for ONTOLOGY_APP
var lastIdProfile = db.getCollection('sequences').findOne({ '_id': 'profile_identifier' }).sequence;

db.tenants.find({ "identifier": { $gte: 0 } }).forEach(function (tenant) {
    db.profiles.insertOne({
        "_id": "PROFIL_" + tenant.identifier + "-ONTOLOGY_APP-CONSULTATION",
        "identifier": NumberInt(lastIdProfile++),
        "name": "Ontologie - lecture seule",
        "description": "Profil pour la consultation des ontologies et schémas dans Vitam, en lecture seule",
        "tenantIdentifier": NumberInt(tenant.identifier),
        "applicationName": "ONTOLOGY_APP",
        "level": "",
        "enabled": true,
        "readonly": false,
        "customerId": tenant.customerId,
        "roles": [
            { name: "ROLE_GET_SCHEMAS" },
            { name: "ROLE_GET_ONTOLOGIES" }
        ]
    });
});

// Update sequence
db.sequences.updateOne({
    "_id": "profile_identifier"
}, {
    $set: {
        "sequence": NumberInt(lastIdProfile)
    }
});

print("END v9.0.0-01_add_consult_ontology_profil.js");
