// The CAS server calls GET /iam/v1/cas/customers (ROLE_CAS_CUSTOMERS) while building a subrogation,
// e.g. to resolve the customers of a super user. This role was only ever granted through the v7.1
// migrations (58_update_cas_profile_cas_customer_role.js, 59_update_cas_ui_contextes_cas_customer_role.js),
// which a base seeded straight from 1.0.0 without replaying v7.1 never received - causing a 403 on
// subrogation. Re-grant it here as its own migration: editing the old 1.0.0 seed would not help
// already-migrated environments, since the changelog skips a script by filename, not by content.
// Effective authorities = context roles INTERSECT user profile roles, so both the CAS context and
// the CAS profile (casuser -> cas_group -> cas_profile) must carry the role.
dbSecurity.contexts.updateOne(
    { "_id": "cas_context" },
    { $addToSet: { "roleNames": "ROLE_CAS_CUSTOMERS" } }
);

dbIam.profiles.updateOne(
    { "_id": "cas_profile" },
    { $addToSet: { "roles": { "name": "ROLE_CAS_CUSTOMERS" } } }
);
