db = db.getSiblingDB('iam')

print("START VITAMUI-2800_init_user_address_ref.js");

// Update all users without address : add an empty address
db.users.update({ "address": { $exists: false } }, {
  $set: {
    "address":
    {
      "street": "",
      "zipCode": "",
      "city": "",
      "country": ""
    }
  }
}, {multi: true});

print("END VITAMUI-2800_init_user_address_ref.js");
