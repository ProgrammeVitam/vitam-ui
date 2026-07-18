// For each customer (except system_customer)
dbIam.customers.find({ "_id": { $ne: "system_customer" } }).forEach(function (customer) {

    var genericAdmin = dbIam.getCollection('users').findOne({
        type: 'GENERIC',
        email: {
            $regex: '^admin@',
            $options: 'i'
        },
        customerId: customer._id
    });

    if (!genericAdmin) {
        throw new Error(
            "No generic admin found for customer: " + customer._id
        );
    }

    var currentGroupForGenericAdmin = dbIam.getCollection('groups').findOne({
        _id: genericAdmin.groupId
    });

    if (!currentGroupForGenericAdmin) {
        throw new Error(
            "No group found for generic admin of customer: " + customer._id
        );
    }

    if (
        currentGroupForGenericAdmin?.name &&
        !currentGroupForGenericAdmin.name.startsWith("RESTRICTED_ADMIN_GROUP") &&
        !currentGroupForGenericAdmin.name.startsWith("ADMIN_CLIENT_ROOT")
    ) {
        throw new Error(
            "Generic admin is affected to a different group than RESTRICTED_ADMIN_GROUP or ADMIN_CLIENT_ROOT"
        );
    }

    // Update full admin group to make it read-only
    dbIam.groups.updateOne(
        {
            _id: currentGroupForGenericAdmin._id
        },
        {
            $set: {
                readonly: true
            }
        }
    );

    if (
        currentGroupForGenericAdmin?.name &&
        currentGroupForGenericAdmin.name.startsWith("ADMIN_CLIENT_ROOT")
    ) {

        // Not yet updated for this customer

        // Get current sequence for profiles
        var maxIdProfile = dbIam.getCollection('sequences').findOne({ '_id': 'profile_identifier' }).sequence;

        // Find the proof tenant (created by default with the customer)
        var proofTenant = dbIam.getCollection('tenants').findOne({
            customerId: customer._id,
            proof: true
        });

        // CREATE NEW PROFILES FOR APPLICATIONS:
        // USERS_APP, ACCOUNTS_APP, GROUPS_APP, PROFILES_APP, HIERARCHY_PROFILE_APP

        dbIam.profiles.insertOne({
            "_id": "PROFIL_" + proofTenant.identifier + "-USERS_APP_PROFILE_GENERIC_ADMIN",
            "identifier": NumberInt(++maxIdProfile),
            "name": "Profil restreint pour la gestion des utilisateurs " + proofTenant.identifier,
            "description": "Profil de l'application de gestion restreinte des utilisateurs",
            "tenantIdentifier": NumberInt(proofTenant.identifier),
            "applicationName": "USERS_APP",
            "level": "",
            "enabled": true,
            "readonly": false,
            "customerId": customer._id,
            "roles": [
                { "name": "ROLE_GET_USERS" },
                { "name": "ROLE_CREATE_USERS" },
                { "name": "ROLE_UPDATE_USERS" },
                { "name": "ROLE_UPDATE_STANDARD_USERS" },
                { "name": "ROLE_MFA_USERS" },
                { "name": "ROLE_ANONYMIZATION_USERS" },
                { "name": "ROLE_GET_GROUPS" },
                { "name": "ROLE_GET_USER_INFOS" },
                { "name": "ROLE_CREATE_USER_INFOS" },
                { "name": "ROLE_UPDATE_USER_INFOS" }
            ]
        });

        // Find existing profiles in admin group for applications:
        // ACCOUNTS_APP, GROUPS_APP, PROFILES_APP, HIERARCHY_PROFILE_APP
        var matchingProfileIds = dbIam.profiles.find(
            {
                _id: {
                    $in: currentGroupForGenericAdmin.profileIds
                },
                applicationName: {
                    $in: [
                        "PROFILES_APP",
                        "HIERARCHY_PROFILE_APP",
                        "ACCOUNTS_APP",
                        "GROUPS_APP"
                    ]
                }
            },
            {
                _id: 1
            }
        ).toArray().map(function (profile) {
            return profile._id;
        });

        //Add custom USERS_APP profile to the matchingProfileIds list
        matchingProfileIds.push(
            "PROFIL_" + proofTenant.identifier + "-USERS_APP_PROFILE_GENERIC_ADMIN"
        );

        // Get current sequence for groups
        var maxIdGroup = dbIam.getCollection('sequences').findOne({
            _id: 'group_identifier'
        }).sequence;

        dbIam.groups.insertOne({
            _id: "RESTRICTED_ADMIN_GROUP_" + proofTenant.identifier,
            name: "RESTRICTED_ADMIN_GROUP " + customer.code,
            readonly: true,
            identifier: NumberInt(++maxIdGroup),
            description: "Groupe de profils pour l'administrateur générique",
            profileIds: matchingProfileIds,
            customerId: customer._id
        });

        // Update generic admin group
        dbIam.users.updateOne(
            { _id: genericAdmin._id },
            {
                $set: {
                    groupId: "RESTRICTED_ADMIN_GROUP_" + proofTenant.identifier
                }
            }
        );

        // Update sequences
        dbIam.sequences.updateOne(
            { _id: "group_identifier" },
            {
                $set: {
                    sequence: NumberInt(maxIdGroup)
                }
            }
        );
        dbIam.sequences.updateOne(
            { _id: "profile_identifier" },
            {
                $set: {
                    sequence: NumberInt(maxIdProfile)
                }
            }
        );
    }
});
