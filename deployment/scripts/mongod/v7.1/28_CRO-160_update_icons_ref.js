print('START 28_CRO-160_update_icons_ref.js');

db = db.getSiblingDB('iam');

db.applications.updateOne(
  {
    identifier: 'ACCESSION_REGISTER_APP'
  },
  {
    $set: {
      icon : "vitamui-icon vitamui-icon-accession-register"
    }
  }
);

db.applications.updateOne(
  {
    identifier: 'LOGBOOK_MANAGEMENT_OPERATION_APP'
  },
  {
    $set: {
      icon : "vitamui-icon vitamui-icon-logbook-management-operation"
    }
  }
);

print('END 28_CRO-160_update_icons_ref.js');
