print('START 25_CRO-181_add_role_for_INGEST_APP_ref.js');

db = db.getSiblingDB('iam');

db.profiles.updateMany(
  {
    applicationName: 'INGEST_APP'
  },
  {
    $addToSet: {
      roles: {
        $each : [
          { name : "ROLE_GET_MANAGEMENT_CONTRACT"},
          { name : "ROLE_GET_FILE_FORMATS"},
          { name : "ROLE_GET_UNITS"},
          { name : "ROLE_GET_EXTERNAL_PARAMS"}
        ]
      }
    }
  }
);

print('END 25_CRO-181_add_role_for_INGEST_APP_ref.js');
