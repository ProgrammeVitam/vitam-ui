db = db.getSiblingDB('iam')

print("START 206_test_data_dev.js");

// ========================================= TOKENS =========================================

db.tokens.insert({
  "_id": "tokenadmin",
  "updatedDate": "May 15, 2008 6:30:47 PM",
  "refId": "admin_user"
});

print("END 206_test_data_dev.js");
