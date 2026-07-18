dbSecurity.contexts.updateOne(
    { "_id": "ui_referential_context" },
    {
        $addToSet: {
            "roleNames": {
                $each: [
                    "ROLE_GET_ACCESSION_REGISTER_DETAIL"
                ]
            }
        }
    }
);
