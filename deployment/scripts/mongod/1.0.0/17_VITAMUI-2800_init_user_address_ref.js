// Update all users without address : add an empty address
dbIam.users.updateMany(
    { "address": { $exists: false } },
    {
        $set: {
            "address": {
                "street": "",
                "zipCode": "",
                "city": "",
                "country": ""
            }
        }
    }
);
