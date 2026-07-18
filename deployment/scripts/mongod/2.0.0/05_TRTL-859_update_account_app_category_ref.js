dbIam.applications.updateOne(
    {
        "identifier": "ACCOUNTS_APP",
        "category": "ingest_and_consultation"
    },
    {
        $unset: { 'category': "" }
    }
);
