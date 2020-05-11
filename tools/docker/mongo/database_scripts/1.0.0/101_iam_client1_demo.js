db = db.getSiblingDB('iam')

print("START 101_iam_client1_demo.js");

db.customers.insert({
	"_id": "5c7927af7884583d1ebb6e7a74547a15e35d431599d976a9708eb12d6c5e56c9",
	"identifier": "11",
	"code": "654852",
	"name": "Client1 ",
	"companyName": "Client1",
	"enabled": true,
	"language": "FRENCH",
	"passwordRevocationDelay": NumberInt(6),
	"otp": "OPTIONAL",
	"emailDomains": [
		"gmail.com"
	],
	"defaultEmailDomain": "gmail.com",
	"address": {
		"street": "1 rue de paris",
		"zipCode": "75001",
		"city": "paris",
		"country": "FR"
	},
    "graphicIdentity": {
    	"hasCustomGraphicIdentity": false
    },
	"readonly": false,
	"subrogeable": true,
	"_class": "customers"
});

db.owners.insert({
	"_id": "5c7927af7884583d1ebb6e7bc40d999675514044b20ead59978775930bf8e00e",
	"identifier": "52",
	"name": "Client 1",
	"code": "654852",
	"companyName": "Client 1",
	"address": {
		"country": "FR"
	},
	"readonly": false,
	"customerId": "5c7927af7884583d1ebb6e7a74547a15e35d431599d976a9708eb12d6c5e56c9",
	"_class": "owners"
});

db.owners.insert({
	"_id": "5c7927d57884583d1ebb6e925dd54c14cd7544cc807a32d1e2c64e27d4b86731",
	"identifier": "53",
	"name": "Propriétaire 1",
	"code": "654853",
	"companyName": "Propriétaire 1",
	"address": {
		"country": "FR"
	},
	"readonly": false,
	"customerId": "5c7927af7884583d1ebb6e7a74547a15e35d431599d976a9708eb12d6c5e56c9",
	"_class": "owners"
});

db.owners.insert({
	"_id": "5c7927e37884583d1ebb6ea6907e15b8d3a747f6af027d6418557344a6606ae2",
	"identifier": "54",
	"name": "Propriétaire 2",
	"code": "654854",
	"companyName": "Propriétaire 2",
	"address": {
		"country": "FR"
	},
	"readonly": false,
	"customerId": "5c7927af7884583d1ebb6e7a74547a15e35d431599d976a9708eb12d6c5e56c9",
	"_class": "owners"
});

db.tenants.insert({
	"_id": "5c7927af7884583d1ebb6e7ea256dede38354ecba085d10543289322e6fdfd76",
	"enabled": true,
	"proof": true,
	"name": "Client 1",
	"identifier": NumberInt(102),
	"ownerId": "5c7927af7884583d1ebb6e7bc40d999675514044b20ead59978775930bf8e00e",
	"readonly": false,
	"customerId": "5c7927af7884583d1ebb6e7a74547a15e35d431599d976a9708eb12d6c5e56c9",
	"ingestContractHoldingIdentifier" : "IC-000001",
	"itemIngestContractIdentifier" : "IC-000001",
	"accessContractHoldingIdentifier" : "AC-000001",
	"accessContractLogbookIdentifier" : "AC-000002",
	"_class": "tenants"
});

db.tenants.insert({
	"_id": "5c7927d57884583d1ebb6e9448db1139c8824b81b95b5ca808c22e0ec4ce8099",
	"enabled": true,
	"proof": false,
	"name": "Coffre 1",
	"identifier": NumberInt(103),
	"ownerId": "5c7927d57884583d1ebb6e925dd54c14cd7544cc807a32d1e2c64e27d4b86731",
	"readonly": false,
	"customerId": "5c7927af7884583d1ebb6e7a74547a15e35d431599d976a9708eb12d6c5e56c9",
	"ingestContractHoldingIdentifier" : "IC-000001",
	"itemIngestContractIdentifier" : "IC-000001",
	"accessContractHoldingIdentifier" : "AC-000001",
	"_class": "tenants"
});

db.tenants.insert({
	"_id": "5c7927e47884583d1ebb6ea892e497b59b47466fad00e3a8ee337ea450b1499c",
	"enabled": true,
	"proof": false,
	"name": "Coffre 2",
	"identifier": NumberInt(104),
	"ownerId": "5c7927e37884583d1ebb6ea6907e15b8d3a747f6af027d6418557344a6606ae2",
	"readonly": false,
	"customerId": "5c7927af7884583d1ebb6e7a74547a15e35d431599d976a9708eb12d6c5e56c9",
	"ingestContractHoldingIdentifier" : "IC-000001",
	"itemIngestContractIdentifier" : "IC-000001",
	"accessContractHoldingIdentifier" : "AC-000001",
	"_class": "tenants"
});

db.providers.insert({
	"_id": "5c7927af7884583d1ebb6e7ddda2807afe234014846b3f4b0c68fdc49261f881",
	"identifier": "51",
	"name": "default",
	"internal": true,
	"enabled": true,
	"patterns": [
		".*@gmail.com"
	],
	"readonly": false,
	"customerId": "5c7927af7884583d1ebb6e7a74547a15e35d431599d976a9708eb12d6c5e56c9",
	"_class": "providers"
});

db.profiles.insert({
	"_id": "5c7927af7884583d1ebb6e7f17e5db9b937b469e93fc87b62f56ddafb179ca53",
	"identifier": "217",
	"name": "USERS 102",
	"enabled": true,
	"description": "USERS 102",
	"tenantIdentifier": NumberInt(102),
	"applicationName": "USERS_APP",
	"roles": [
		{
			"name": "ROLE_GET_USERS"
		},
		{
			"name": "ROLE_CREATE_USERS"
		},
		{
			"name": "ROLE_UPDATE_USERS"
		},
		{
			"name": "ROLE_UPDATE_STANDARD_USERS"
		},
		{
			"name": "ROLE_MFA_USERS"
		},
		{
			"name": "ROLE_ANONYMIZATION_USERS"
		},
		{
			"name": "ROLE_GENERIC_USERS"
		},
		{
			"name": "ROLE_GET_GROUPS"
		}
	],
	"level": "",
	"readonly": true,
	"customerId": "5c7927af7884583d1ebb6e7a74547a15e35d431599d976a9708eb12d6c5e56c9",
	"_class": "profiles"
});
db.profiles.insert({
	"_id": "5c7927af7884583d1ebb6e80c0149571ac9e424aad530a9f87ed2fc32d226c63",
	"identifier": "218",
	"name": "GROUPS 102",
	"enabled": true,
	"description": "GROUPS 102",
	"tenantIdentifier": NumberInt(102),
	"applicationName": "GROUPS_APP",
	"roles": [
		{
			"name": "ROLE_GET_GROUPS"
		},
		{
			"name": "ROLE_CREATE_GROUPS"
		},
		{
			"name": "ROLE_UPDATE_GROUPS"
		},
		{
			"name": "ROLE_DELETE_GROUPS"
		},
		{
			"name": "ROLE_GET_PROFILES"
		},
		{
			"name": "ROLE_GET_PROFILES_ALL_TENANTS"
		}
	],
	"level": "",
	"readonly": true,
	"customerId": "5c7927af7884583d1ebb6e7a74547a15e35d431599d976a9708eb12d6c5e56c9",
	"_class": "profiles"
});
db.profiles.insert({
	"_id": "5c7927af7884583d1ebb6e8141952c6fa39c4f858a951aad767e453d4ddb3974",
	"identifier": "219",
	"name": "PROFILES 102",
	"enabled": true,
	"description": "PROFILES 102",
	"tenantIdentifier": NumberInt(102),
	"applicationName": "PROFILES_APP",
	"roles": [
		{
			"name": "ROLE_GET_PROFILES"
		},
		{
			"name": "ROLE_CREATE_PROFILES"
		},
		{
			"name": "ROLE_UPDATE_PROFILES"
		},
		{
			"name": "ROLE_DELETE_PROFILES"
		},
		{
			"name": "ROLE_GET_GROUPS"
		}
	],
	"level": "",
	"readonly": true,
	"customerId": "5c7927af7884583d1ebb6e7a74547a15e35d431599d976a9708eb12d6c5e56c9",
	"_class": "profiles"
});
db.profiles.insert({
	"_id": "5c7927af7884583d1ebb6e83a608d8eaf01041bbb45c33ac9b862d1716af01fd",
	"identifier": "221",
	"name": "ACCOUNTS 102",
	"enabled": true,
	"description": "ACCOUNTS 102",
	"tenantIdentifier": NumberInt(102),
	"applicationName": "ACCOUNTS_APP",
	"roles": [
		{
			"name": "ROLE_UPDATE_ME_USERS"
		}
	],
	"level": "",
	"readonly": true,
	"customerId": "5c7927af7884583d1ebb6e7a74547a15e35d431599d976a9708eb12d6c5e56c9",
	"_class": "profiles"
});
db.profiles.insert({
	"_id": "5c7927af7884583d1ebb6e8ae004eb3dc6d6476d964a4f079ad2bdbe06eb0465",
	"identifier": "228",
	"name": "Hierarchy Profiles 102",
	"enabled": true,
	"description": "Hierarchy Profiles 102",
	"tenantIdentifier": NumberInt(102),
	"applicationName": "HIERARCHY_PROFILE_APP",
	"roles": [
		{
			"name": "ROLE_GET_PROFILES"
		},
		{
			"name": "ROLE_CREATE_PROFILES"
		},
		{
			"name": "ROLE_UPDATE_PROFILES"
		},
		{
			"name": "ROLE_DELETE_PROFILES"
		}
	],
	"level": "",
	"readonly": true,
	"customerId": "5c7927af7884583d1ebb6e7a74547a15e35d431599d976a9708eb12d6c5e56c9",
	"_class": "profiles"
});
db.profiles.insert({
	"_id": "5c7927d57884583d1ebb6ea2a26b9d609a1440e397b39684e9209c662d9a81d4",
	"identifier": "244",
	"name": "Hierarchy Profiles 103",
	"enabled": true,
	"description": "Hierarchy Profiles 103",
	"tenantIdentifier": NumberInt(103),
	"applicationName": "HIERARCHY_PROFILE_APP",
	"roles": [
		{
			"name": "ROLE_GET_PROFILES"
		},
		{
			"name": "ROLE_CREATE_PROFILES"
		},
		{
			"name": "ROLE_UPDATE_PROFILES"
		},
		{
			"name": "ROLE_DELETE_PROFILES"
		}
	],
	"level": "",
	"readonly": true,
	"customerId": "5c7927af7884583d1ebb6e7a74547a15e35d431599d976a9708eb12d6c5e56c9",
	"_class": "profiles"
});
db.profiles.insert({
	"_id": "5c7927e47884583d1ebb6eb64c7e540032924ed797da29054add06f32cb8e718",
	"identifier": "260",
	"name": "Hierarchy Profiles 104",
	"enabled": true,
	"description": "Hierarchy Profiles 104",
	"tenantIdentifier": NumberInt(104),
	"applicationName": "HIERARCHY_PROFILE_APP",
	"roles": [
		{
			"name": "ROLE_GET_PROFILES"
		},
		{
			"name": "ROLE_CREATE_PROFILES"
		},
		{
			"name": "ROLE_UPDATE_PROFILES"
		},
		{
			"name": "ROLE_DELETE_PROFILES"
		}
	],
	"level": "",
	"readonly": true,
	"customerId": "5c7927af7884583d1ebb6e7a74547a15e35d431599d976a9708eb12d6c5e56c9",
	"_class": "profiles"
});
db.profiles.insert({
    "_id" : "5c7e63347884583d1ebb6fea7caf6cf786214ae1835ff6277c3a0ca35aefb13d",
    "identifier" : "306",
    "name" : "GROUPS 102",
    "enabled" : true,
    "description" : "GROUPS 102",
    "tenantIdentifier" : NumberInt(102),
    "applicationName" : "GROUPS_APP",
    "roles" : [
        {
            "name" : "ROLE_GET_GROUPS"
        },
        {
            "name" : "ROLE_CREATE_GROUPS"
        },
        {
            "name" : "ROLE_UPDATE_GROUPS"
        },
        {
            "name" : "ROLE_DELETE_GROUPS"
        },
        {
            "name" : "ROLE_GET_PROFILES"
        },
        {
            "name" : "ROLE_GET_PROFILES_ALL_TENANTS"
        }
    ],
    "level" : "FRANCE",
    "readonly" : false,
    "customerId" : "5c7927af7884583d1ebb6e7a74547a15e35d431599d976a9708eb12d6c5e56c9",
    "_class": "profiles"
});
db.profiles.insert({
    "_id" : "5c7e633c7884583d1ebb6fed93bc936892c44dd8a6875d78292e63badcec2376",
    "identifier" : "307",
    "name" : "GROUPS 102",
    "enabled" : true,
    "description" : "GROUPS 102",
    "tenantIdentifier" : NumberInt(102),
    "applicationName" : "GROUPS_APP",
    "roles" : [
        {
            "name" : "ROLE_GET_GROUPS"
        },
        {
            "name" : "ROLE_CREATE_GROUPS"
        },
        {
            "name" : "ROLE_UPDATE_GROUPS"
        },
        {
            "name" : "ROLE_DELETE_GROUPS"
        },
        {
            "name" : "ROLE_GET_PROFILES"
        },
        {
            "name" : "ROLE_GET_PROFILES_ALL_TENANTS"
        }
    ],
    "level" : "ITALIE",
    "readonly" : false,
    "customerId" : "5c7927af7884583d1ebb6e7a74547a15e35d431599d976a9708eb12d6c5e56c9",
    "_class": "profiles"
});
db.profiles.insert({
    "_id" : "5c7e7473788458585c0ccb48a5b9e48a0eb448aab40893d9010b8df80f2f34b2",
    "identifier" : "314",
    "name" : "Profil Admin France",
    "enabled" : true,
    "description" : "Profil de l'administrateur des utilisateurs France",
    "tenantIdentifier" : NumberInt(102),
    "applicationName" : "USERS_APP",
    "roles" : [
        {
            "name" : "ROLE_GET_USERS"
        },
        {
            "name" : "ROLE_GET_GROUPS"
        },
        {
            "name" : "ROLE_MFA_USERS"
        },
        {
            "name" : "ROLE_UPDATE_USERS"
        },
        {
            "name" : "ROLE_CREATE_USERS"
        },
        {
            "name" : "ROLE_UPDATE_STANDARD_USERS"
        },
        {
            "name" : "ROLE_GENERIC_USERS"
        }
    ],
    "level" : "FRANCE",
    "readonly" : false,
    "customerId" : "5c7927af7884583d1ebb6e7a74547a15e35d431599d976a9708eb12d6c5e56c9",
    "_class": "profiles"
});
db.profiles.insert({
    "_id" : "5c7e748f788458585c0ccb4b93ed9fed6e634c9da9ce0bf064d107713ef32ce2",
    "identifier" : "315",
    "name" : "Profil Admin Italie",
    "enabled" : true,
    "description" : "Profil de l'administrateur des utilisateurs Italie",
    "tenantIdentifier" : NumberInt(102),
    "applicationName" : "USERS_APP",
    "roles" : [
        {
            "name" : "ROLE_GET_USERS"
        },
        {
            "name" : "ROLE_GET_GROUPS"
        },
        {
            "name" : "ROLE_MFA_USERS"
        },
        {
            "name" : "ROLE_UPDATE_USERS"
        },
        {
            "name" : "ROLE_CREATE_USERS"
        },
        {
            "name" : "ROLE_UPDATE_STANDARD_USERS"
        },
        {
            "name" : "ROLE_GENERIC_USERS"
        }
    ],
    "level" : "ITALIE",
    "readonly" : false,
    "customerId" : "5c7927af7884583d1ebb6e7a74547a15e35d431599d976a9708eb12d6c5e56c9",
    "_class": "profiles"
});

db.groups.insert({
    "_id" : "5c7927af7884583d1ebb6e8b3ccccdb5d1a64684aa6cb24df41aa6736bc9e5c0",
    "identifier" : "229",
    "name" : "ADMIN_CLIENT_ROOT 654852",
    "description" : "ADMIN_CLIENT_ROOT",
    "enabled" : true,
    "profileIds" : [
        "5c7927af7884583d1ebb6e7f17e5db9b937b469e93fc87b62f56ddafb179ca53",
        "5c7927af7884583d1ebb6e80c0149571ac9e424aad530a9f87ed2fc32d226c63",
        "5c7927af7884583d1ebb6e8141952c6fa39c4f858a951aad767e453d4ddb3974",
        "5c7927af7884583d1ebb6e83a608d8eaf01041bbb45c33ac9b862d1716af01fd",
        "5c7927af7884583d1ebb6e8ae004eb3dc6d6476d964a4f079ad2bdbe06eb0465",
        "5c7927d57884583d1ebb6ea2a26b9d609a1440e397b39684e9209c662d9a81d4",
        "5c7927e47884583d1ebb6eb64c7e540032924ed797da29054add06f32cb8e718"
    ],
    "readonly" : false,
    "level" : "",
    "customerId" : "5c7927af7884583d1ebb6e7a74547a15e35d431599d976a9708eb12d6c5e56c9",
    "_class": "groups"
});
db.groups.insert({
    "_id" : "5c7e64c17884583d1ebb7002f268e1d589294a70996ffbb859979cc8bd27766d",
    "identifier" : "129",
    "name" : "Groupe Admin Users France",
    "description" : "Groupe pour les administrateurs de la filiale France",
    "enabled" : true,
    "profileIds" : [
        "5c7e63347884583d1ebb6fea7caf6cf786214ae1835ff6277c3a0ca35aefb13d",
        "5c7e7473788458585c0ccb48a5b9e48a0eb448aab40893d9010b8df80f2f34b2"
    ],
    "readonly" : false,
    "level" : "FRANCE",
    "customerId" : "5c7927af7884583d1ebb6e7a74547a15e35d431599d976a9708eb12d6c5e56c9",
    "_class": "groups"
});
db.groups.insert({
    "_id" : "5c7e64fc7884583d1ebb70041fdaa849d74c4eb8b8ee42a1ae83f42be85ba171",
    "identifier" : "130",
    "name" : "Groupe Admin Users Italie",
    "description" : "Groupe pour les administrateurs de la filiale Italie",
    "enabled" : true,
    "profileIds" : [
        "5c7e633c7884583d1ebb6fed93bc936892c44dd8a6875d78292e63badcec2376",
        "5c7e748f788458585c0ccb4b93ed9fed6e634c9da9ce0bf064d107713ef32ce2"
    ],
    "readonly" : false,
    "level" : "ITALIE",
    "customerId" : "5c7927af7884583d1ebb6e7a74547a15e35d431599d976a9708eb12d6c5e56c9",
    "_class": "groups"
});

db.users.insert({
    "_id" : "5c7927af7884583d1ebb6e8c05d5356889264b248f082e269e67cb9a8af7941e",
    "email" : "admin@gmail.com",
    "firstname" : "Admin",
    "identifier" : "103",
    "otp" : false,
    "subrogeable" : true,
    "lastname" : "ADMIN",
    "language" : "FRENCH",
    "groupId" : "5c7927af7884583d1ebb6e8b3ccccdb5d1a64684aa6cb24df41aa6736bc9e5c0",
    "nbFailedAttempts" : 0,
    "status" : "ENABLED",
    "type" : "GENERIC",
    "readonly" : false,
    "level" : "",
    "passwordExpirationDate" : "2050-01-09T00:00:00.000+01:00",
    "customerId" : "5c7927af7884583d1ebb6e7a74547a15e35d431599d976a9708eb12d6c5e56c9",
    "_class": "users"
});
db.users.insert({
    "_id" : "5c7e65367884583d1ebb700572a02a0978b84b8883396a7a53ebb787fcd6e9ce",
    "email" : "adminfrance@gmail.com",
    "firstname" : "Admin",
    "identifier" : "132",
    "otp" : false,
    "subrogeable" : true,
    "lastname" : "FRANCE",
    "language" : "FRENCH",
    "groupId" : "5c7e64c17884583d1ebb7002f268e1d589294a70996ffbb859979cc8bd27766d",
    "nbFailedAttempts" : 0,
    "status" : "ENABLED",
    "type" : "GENERIC",
    "readonly" : false,
    "level" : "FRANCE",
    "passwordExpirationDate" : "2050-01-09T00:00:00.000+01:00",
    "customerId" : "5c7927af7884583d1ebb6e7a74547a15e35d431599d976a9708eb12d6c5e56c9",
    "_class": "users"
});
db.users.insert({
    "_id" : "5c7e65427884583d1ebb7007e0d6d611bd254f809fab5b39dd71f465e6270fe0",
    "email" : "adminitalie@gmail.com",
    "firstname" : "Admin",
    "identifier" : "133",
    "otp" : false,
    "subrogeable" : true,
    "lastname" : "ITALIE",
    "language" : "FRENCH",
    "groupId" : "5c7e64fc7884583d1ebb70041fdaa849d74c4eb8b8ee42a1ae83f42be85ba171",
    "nbFailedAttempts" : 0,
    "status" : "ENABLED",
    "type" : "GENERIC",
    "readonly" : false,
    "level" : "ITALIE",
    "passwordExpirationDate" : "2050-01-09T00:00:00.000+01:00",
    "customerId" : "5c7927af7884583d1ebb6e7a74547a15e35d431599d976a9708eb12d6c5e56c9",
    "_class": "users"
});

print("END 101_iam_client1_demo.js");
