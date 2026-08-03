dbSecurity.contexts.updateOne(
    { "_id": "cas_context" },
    {
        $addToSet: {
            "roleNames": "ROLE_CAS_CUSTOMERS"
        }
    }
);
