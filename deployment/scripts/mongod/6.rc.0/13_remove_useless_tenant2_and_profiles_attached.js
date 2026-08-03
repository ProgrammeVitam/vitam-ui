dbIam.profiles.deleteOne(
    {
        "_id": "auto_system_rules",
        "tenantIdentifier": NumberInt(2)
    }
);
dbIam.tenants.deleteOne(
    {
        "_id": "auto_tenant",
        "identifier": NumberInt(2)
    }
);
