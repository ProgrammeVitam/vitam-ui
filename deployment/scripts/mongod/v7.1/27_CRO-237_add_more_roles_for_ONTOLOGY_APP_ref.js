print('START 27_CRO-237_add_more_roles_for_ONTOLOGY_APP_ref.js');

db = db.getSiblingDB('iam');
db.profiles.updateMany(
  {
    applicationName: 'ONTOLOGY_APP'
  },
  {
    $addToSet: {
      roles: {
        $each : [
            { name : "ROLE_UPDATE_ONTOLOGIES" }
        ]
      }
    }
  }
);

db = db.getSiblingDB('security');
db.contexts.updateOne(
  {
      "_id": "ui_referential_context"
  },
  {
  $addToSet: {
      "roleNames": "ROLE_UPDATE_ONTOLOGIES"
  }
});

print('END 27_CRO-237_add_more_roles_for_ONTOLOGY_APP_ref.js');
