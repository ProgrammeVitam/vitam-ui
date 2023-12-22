print("START 038_TRV-439_upgrade_cas_services_version_ref.js");

db = db.getSiblingDB('{{ mongodb.cas.db | default("cas") }}');

db.services.updateMany(
  { _class: "org.apereo.cas.services.RegexRegisteredService" },
  {
    $set: {
      _class: "org.apereo.cas.services.CasRegisteredService",
    },
  }
);

print("END 038_TRV-439_upgrade_cas_services_version_ref.js");
