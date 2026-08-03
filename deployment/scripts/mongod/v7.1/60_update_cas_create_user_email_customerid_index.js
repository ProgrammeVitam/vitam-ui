//Create a new index base on user email with userId for multi domain feature
dbIam.users.createIndex(
    {
        "email": 1,
        "customerId": 1
    },
    {
        "unique": true,
        "name": "idx_user_email_customerid"
    }
)
