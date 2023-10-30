db = db.getSiblingDB('iam')

print("START VITAMUI-2800_init_user_address_ref.js");

// Update all users without address : add an empty address
db.users.updateMany({ "address": { $exists: false } }, {
  $set: {
    "address":
    {
      "street": "",
      "zipCode": "",
      "city": "",
      "country": ""
    }
  }
});

print("END VITAMUI-2800_init_user_address_ref.js");
