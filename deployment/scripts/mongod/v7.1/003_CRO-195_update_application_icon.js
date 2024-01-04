print("Start 003_CRO-195_update_application_icon");

db = db.getSiblingDB('iam')

db.applications.update({
  "identifier" : "SECURITY_PROFILES_APP"
}, {
  $set: {
    "icon": "vitamui-icon vitamui-icon-security-profile"
  },
});

print('End 003_CRO-195_update_application_icon');
