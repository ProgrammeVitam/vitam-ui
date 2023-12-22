print('START 26_CRO-181_add_more_roles_for_INGEST_APP_ref.js');

db = db.getSiblingDB('iam');

db.profiles.updateMany(
  {
    applicationName: 'INGEST_APP'
  },
  {
    $addToSet: {
      roles: {
        $each : [
            { name : "ROLE_GET_FILE_FORMATS"},
            { name : "ROLE_GET_UNITS"},
            { name : "ROLE_GET_EXTERNAL_PARAMS"}
        ]
      }
    }
  }
);

print('END 26_CRO-181_add_more_roles_for_INGEST_APP_ref.js');
