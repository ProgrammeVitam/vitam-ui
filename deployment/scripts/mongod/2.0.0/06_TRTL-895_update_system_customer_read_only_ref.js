db = db.getSiblingDB('iam')

print("START 06_TRTL-895_update_system_customer_read_only_ref.js");

db.customers.update({
  "_id": "system_customer",
}, {
  $set: {
    "readonly": false
  },
});
print("END 06_TRTL-895_update_system_customer_read_only_ref.js");
