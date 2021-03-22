db = db.getSiblingDB('iam')

print("START 05_TRTL-859_update_account_app_category.js");

db.applications.update({
  "identifier" : "ACCOUNTS_APP",
  "category": "ingest_and_consultation"
}, {
  $unset: {'category': ""},

});
print("END 05_TRTL-859_update_account_app_category.js");
