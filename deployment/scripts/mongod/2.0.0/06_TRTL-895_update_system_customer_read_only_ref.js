dbIam.customers.updateOne(
    { "_id": "system_customer" },
    {
        $set: {
            "readonly": false
        }
    }
);
