dbIam.applications.updateOne(
    { "identifier": "AGENCIES_APP" },
    {
        $set: {
            "name": "Services Agents"
        }
    }
);
