dbCas.services.updateMany(
    { _class: "org.apereo.cas.services.RegexRegisteredService" },
    {
        $set: {
            _class: "org.apereo.cas.services.CasRegisteredService",
        },
    }
);
