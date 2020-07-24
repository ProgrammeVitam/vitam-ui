db = db.getSiblingDB('iam')

print("START 101_iam_client2_demo.js");

db.customers.insert({
	"_id": "5c7928337884583d1ebb6ebaa3f3eb30bc0542178127d1572b8f70c7c0b0cb68",
	"identifier": "12",
	"code": "659845",
	"name": "Client2",
	"companyName": "Client2",
	"enabled": true,
	"language": "FRENCH",
	"passwordRevocationDelay": 6,
	"otp": "OPTIONAL",
	"emailDomains": [
		"client2.fr",
		"client2.com"
	],
	"defaultEmailDomain": "client2.fr",
	"address": {
		"street": "2 rue de paris",
		"zipCode": "75002",
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
	"_id": "5c7928337884583d1ebb6ebb5be6372ef9394a8691874ef993e013ab5da0e42f",
	"identifier": "55",
	"name": "Client 2",
	"code": "659845",
	"companyName": "Client 2",
	"address": {
		"country": "FR"
	},
	"readonly": false,
	"customerId": "5c7928337884583d1ebb6ebaa3f3eb30bc0542178127d1572b8f70c7c0b0cb68",
	"_class": "owners"
});

db.owners.insert({
	"_id": "5c7928597884583d1ebb6ed24e893dcfc5cc4d0a9a69f63dbfa2a7ebd08f6ed8",
	"identifier": "56",
	"name": "Propriétaire 1",
	"code": "659846",
	"companyName": "Propriétaire 1",
	"address": {
		"country": "FR"
	},
	"readonly": false,
	"customerId": "5c7928337884583d1ebb6ebaa3f3eb30bc0542178127d1572b8f70c7c0b0cb68",
	"_class": "owners"
});

db.tenants.insert({
	"_id": "5c7928337884583d1ebb6ebe056678aa36364074b97135c2b372270949f60e1a",
	"enabled": true,
	"proof": true,
	"name": "Client 2",
	"identifier": NumberInt(105),
	"ownerId": "5c7928337884583d1ebb6ebb5be6372ef9394a8691874ef993e013ab5da0e42f",
	"readonly": false,
	"customerId": "5c7928337884583d1ebb6ebaa3f3eb30bc0542178127d1572b8f70c7c0b0cb68",
	"ingestContractHoldingIdentifier" : "IC-000001",
	"itemIngestContractIdentifier" : "IC-000001",
	"accessContractHoldingIdentifier" : "AC-000001",
	"accessContractLogbookIdentifier" : "AC-000002",
	"_class": "tenants"
});

db.tenants.insert({
	"_id": "5c7928597884583d1ebb6ed48b92969264764e55a61db10314aa8633cd0c6fc4",
	"enabled": true,
	"proof": false,
	"name": "Coffre 1 ",
	"identifier": NumberInt(106),
	"ownerId": "5c7928597884583d1ebb6ed24e893dcfc5cc4d0a9a69f63dbfa2a7ebd08f6ed8",
	"readonly": false,
	"customerId": "5c7928337884583d1ebb6ebaa3f3eb30bc0542178127d1572b8f70c7c0b0cb68",
	"ingestContractHoldingIdentifier" : "IC-000001",
	"itemIngestContractIdentifier" : "IC-000001",
	"accessContractHoldingIdentifier" : "AC-000001",
	"_class": "tenants"
});

db.providers.insert({
	"_id": "5c7928337884583d1ebb6ebd3b672beb39d04beda4c55d70d5352184d926ed31",
	"identifier": "52",
	"name": "default",
	"internal": true,
	"enabled": true,
	"patterns": [
		".*@client2.fr"
	],
	"readonly": false,
	"customerId": "5c7928337884583d1ebb6ebaa3f3eb30bc0542178127d1572b8f70c7c0b0cb68",
	"_class": "providers"
});

db.profiles.insert({
	"_id": "5c7928337884583d1ebb6ebf1843c77a02554d35b902ced3aa65c0aa58899b72",
	"identifier": "263",
	"name": "USERS 105",
	"enabled": true,
	"description": "USERS 105",
	"tenantIdentifier": NumberInt(105),
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
	"customerId": "5c7928337884583d1ebb6ebaa3f3eb30bc0542178127d1572b8f70c7c0b0cb68",
	"_class": "profiles"
});
db.profiles.insert({
	"_id": "5c7928337884583d1ebb6ec030ebc4100c8646208c3014c96e4f74e6c2432352",
	"identifier": "264",
	"name": "GROUPS 105",
	"enabled": true,
	"description": "GROUPS 105",
	"tenantIdentifier": NumberInt(105),
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
	"customerId": "5c7928337884583d1ebb6ebaa3f3eb30bc0542178127d1572b8f70c7c0b0cb68",
	"_class": "profiles"
});
db.profiles.insert(
{
	"_id": "5c7928337884583d1ebb6ec1eeb9cd5cb44b4110a512abde58fd04307a395f14",
	"identifier": "265",
	"name": "PROFILES 105",
	"enabled": true,
	"description": "PROFILES 105",
	"tenantIdentifier": NumberInt(105),
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
	"customerId": "5c7928337884583d1ebb6ebaa3f3eb30bc0542178127d1572b8f70c7c0b0cb68",
	"_class": "profiles"
});
db.profiles.insert(
{
	"_id": "5c7928337884583d1ebb6ec33fdeff1270174f37bb32afa3c2ca36f41c304120",
	"identifier": "267",
	"name": "ACCOUNTS 105",
	"enabled": true,
	"description": "ACCOUNTS 105",
	"tenantIdentifier": NumberInt(105),
	"applicationName": "ACCOUNTS_APP",
	"roles": [
		{
			"name": "ROLE_UPDATE_ME_USERS"
		}
	],
	"level": "",
	"readonly": true,
	"customerId": "5c7928337884583d1ebb6ebaa3f3eb30bc0542178127d1572b8f70c7c0b0cb68",
	"_class": "profiles"
});
db.profiles.insert(
{
	"_id": "5c7928337884583d1ebb6eca27b70a0b0e7f4d6ea8723fa5c577804f6da5da3d",
	"identifier": "274",
	"name": "Hierarchy Profiles 105",
	"enabled": true,
	"description": "Hierarchy Profiles 105",
	"tenantIdentifier": NumberInt(105),
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
	"customerId": "5c7928337884583d1ebb6ebaa3f3eb30bc0542178127d1572b8f70c7c0b0cb68",
	"_class": "profiles"
});
db.profiles.insert(
{
	"_id": "5c79285a7884583d1ebb6ee28433459e0e414e3983bc96f8d5604f52adf34266",
	"identifier": "290",
	"name": "Hierarchy Profiles 106",
	"enabled": true,
	"description": "Hierarchy Profiles 106",
	"tenantIdentifier": NumberInt(106),
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
	"customerId": "5c7928337884583d1ebb6ebaa3f3eb30bc0542178127d1572b8f70c7c0b0cb68",
	"_class": "profiles"
});


db.groups.insert(
{
	"_id" : "5c7928337884583d1ebb6ecbee8ed316ea5546bf92500eb136afa905d237233b",
	"identifier" : "275",
	"name" : "ADMIN_CLIENT_ROOT 659845",
	"description" : "ADMIN_CLIENT_ROOT",
	"enabled" : true,
	"profileIds" : [
		"5c7928337884583d1ebb6ebf1843c77a02554d35b902ced3aa65c0aa58899b72",
		"5c7928337884583d1ebb6ec030ebc4100c8646208c3014c96e4f74e6c2432352",
		"5c7928337884583d1ebb6ec1eeb9cd5cb44b4110a512abde58fd04307a395f14",
		"5c7928337884583d1ebb6ec33fdeff1270174f37bb32afa3c2ca36f41c304120",
		"5c7928337884583d1ebb6eca27b70a0b0e7f4d6ea8723fa5c577804f6da5da3d",
		"5c79285a7884583d1ebb6ee28433459e0e414e3983bc96f8d5604f52adf34266"
	],
	"readonly" : false,
	"level" : "",
	"customerId" : "5c7928337884583d1ebb6ebaa3f3eb30bc0542178127d1572b8f70c7c0b0cb68",
	"_class": "groups"
});

db.users.insert({
    "_id" : "5c7928337884583d1ebb6ecc1e146e10286647b9a195349483f8f09b1628721c",
    "email" : "admin@client2.fr",
    "firstname" : "Admin",
    "identifier" : "105",
    "otp" : false,
    "subrogeable" : true,
    "lastname" : "ADMIN",
    "language" : "FRENCH",
    "groupId" : "5c7928337884583d1ebb6ecbee8ed316ea5546bf92500eb136afa905d237233b",
    "nbFailedAttempts" : 0,
    "status" : "ENABLED",
    "type" : "GENERIC",
    "readonly" : false,
    "level" : "",
    "passwordExpirationDate" : "2050-01-09T00:00:00.000+01:00",
    "customerId" : "5c7928337884583d1ebb6ebaa3f3eb30bc0542178127d1572b8f70c7c0b0cb68",
    "_class": "users"
});

print("END 101_iam_client2_demo.js");
