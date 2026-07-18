dbIam.applications.updateOne(
    {
        "identifier": "CONTEXTS_APP",
        "category": "referential"
    },
    {
        $set: {
            "category": "security_and_application_rights"
        }
    }
);
