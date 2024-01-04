print('START 29_CRO-265_update_icon_EXTERNAL_PARAM_PROFILE_APP_ref.js');

db = db.getSiblingDB('iam');

db.applications.updateOne(
  {
    identifier: 'EXTERNAL_PARAM_PROFILE_APP'
  },
  {
    $set: {
      icon : "vitamui-icon vitamui-icon-external-param-profil"
    }
  }
);

print('END 29_CRO-265_update_icon_EXTERNAL_PARAM_PROFILE_APP_ref.js');
