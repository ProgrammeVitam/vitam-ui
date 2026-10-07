var casRoles = [
    "ROLE_CAS_HRD",
    "ROLE_CAS_SUBROGATION_VALIDATE",
    "ROLE_CAS_PRINCIPAL_ATTRIBUTES",
    "ROLE_CAS_PASSWORD_POLICY",
    "ROLE_CAS_CUSTOMERS"
];

dbSecurity.contexts.updateOne(
    { "_id": "cas_context" },
    { $addToSet: { "roleNames": { $each: casRoles } } }
);

dbIam.profiles.updateOne(
    { "_id": "cas_profile" },
    { $addToSet: { "roles": { $each: casRoles.map(function (name) { return { "name": name }; }) } } }
);
