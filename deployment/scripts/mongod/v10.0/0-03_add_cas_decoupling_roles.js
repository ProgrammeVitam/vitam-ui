// Grant the new decoupling roles now that the authentication server consumes the endpoints
// (resolveHrd, validateSubrogation, buildPrincipalAttributes, getPasswordPolicy).
// Effective authorities = context roles INTERSECT user profile roles, so both the CAS context and
// the CAS profile (casuser -> cas_group -> cas_profile) must carry the roles.
var casDecouplingRoles = [
    "ROLE_CAS_HRD",
    "ROLE_CAS_SUBROGATION_VALIDATE",
    "ROLE_CAS_PRINCIPAL_ATTRIBUTES",
    "ROLE_CAS_PASSWORD_POLICY"
];

dbSecurity.contexts.updateOne(
    { "_id": "cas_context" },
    { $addToSet: { "roleNames": { $each: casDecouplingRoles } } }
);

dbIam.profiles.updateOne(
    { "_id": "cas_profile" },
    { $addToSet: { "roles": { $each: casDecouplingRoles.map(function (name) { return { "name": name }; }) } } }
);
