print('START 25_CRO-181_add_role_for_INGEST_APP_ref.js');

db = db.getSiblingDB('iam');

db.profiles.updateMany(
  {
    applicationName: 'INGEST_APP'
  },
  {
    $addToSet: {
      roles: {
        name: 'ROLE_GET_MANAGEMENT_CONTRACT'
      }
    }
  }
);

print('END 25_CRO-181_add_role_for_INGEST_APP_ref.js');
