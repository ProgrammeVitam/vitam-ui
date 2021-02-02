db = db.getSiblingDB('iam')

print("START 101_iam_system_demo.js");

db.tenants.updateOne({
	"_id" : "system_tenant"
}, {
	$set : {
		"itemIngestContractIdentifier" : "IC-000005"
	}
});

db.sequences.updateOne({
    "_id": "tenant_identifier"
}, {
	$set: {
		"sequence": NumberInt(106)
	}
});
db.sequences.updateOne({
    "_id": "user_identifier"
}, {
	$set: {
		"sequence": NumberInt(143)
	}
});
db.sequences.updateOne({
    "_id": "profile_identifier"
}, {
	$set: {
		"sequence": NumberInt(318)
	}
});
db.sequences.updateOne({
    "_id": "group_identifier"
}, {
	$set: {
		"sequence": NumberInt(133)
	}
});
db.sequences.updateOne({
    "_id": "provider_identifier"
}, {
	$set: {
		"sequence": NumberInt(52)
	}
});
db.sequences.updateOne({
    "_id": "customer_identifier"
}, {
	$set: {
		"sequence": NumberInt(12)
	}
});
db.sequences.updateOne({
    "_id": "owner_identifier"
}, {
	$set: {
		"sequence": NumberInt(56)
	}
});

db.owners.insert({
	"_id": "5c7927537884583d1ebb6e66aaacceedbb0541fb97538964f8fc26f58085b92b",
	"identifier": "51",
	"name": "VitamUI",
	"code": "000003",
	"companyName": "VitamUI",
	"address": {
		"country": "FR"
	},
	"readonly": false,
	"customerId": "system_customer",
	"_class": "owners"
});

db.tenants.update({"identifier": 1},{"$set" : {"itemIngestContractIdentifier" : "IC-000005"}});

db.tenants.insert({
	"_id": "5c7927537884583d1ebb6e682b0f33f74d9c4483b7b3b12c8a075dc2e21fa771",
	"enabled": true,
	"proof": false,
	"name": "Docs VitamUI",
	"identifier": NumberInt(9),
	"ownerId": "5c7927537884583d1ebb6e66aaacceedbb0541fb97538964f8fc26f58085b92b",
	"readonly": false,
	"customerId": "system_customer",
	"ingestContractHoldingIdentifier" : "IC-000001",
	"itemIngestContractIdentifier" : "IC-000001",
	"accessContractHoldingIdentifier" : "AC-000001",
	"_class": "tenants"
});

db.owners.insert({
	"_id": "system_owner_externe",
	"identifier" : NumberInt(5),
	"enabled": true,
	"readonly": true,
	"code": "000006",
	"name": "system_owner externe",
	"companyName": "system_company",
	"customerId": "system_customer",
	"address": {
	  street: "73 rue du faubourg poissonnière",
	  zipCode: "75009",
	  city: "Paris",
	  country: "FR"
	}
});

db.tenants.insert({
	"_id": "tenant_20",
	"name": "Tenant Externe",
	"proof" : false,
	"enabled": true,
	"readonly": false,
	"identifier": NumberInt(20),
	"ownerId": "system_owner_externe",
	"ingestContractHoldingIdentifier" : "IC-000001",
	"itemIngestContractIdentifier" : "IC-000001",
	"accessContractHoldingIdentifier" : "AC-000001",
	"customerId": "system_customer"
});

//----------------------------------------- PROFILES TENANT 20 ------------------------------------

//----------------------------------------- PROFILES TENANT 9 ------------------------------------

db.profiles.insert({
	"_id": "5c7927537884583d1ebb6e769fcbc58f86f148a3ba96a58759b4befcdadb171c",
	"identifier": "214",
	"name": "Hierarchy Profiles 9",
	"enabled": true,
	"description": "Hierarchy Profiles 9",
	"tenantIdentifier": NumberInt(9),
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
	"customerId": "system_customer",
	"_class": "profiles"
});



db.groups.updateOne( {
	"_id": "admin_group"
}, {
    $addToSet: {
        "patterns":  {
            $each: [
				"5c7927537884583d1ebb6e769fcbc58f86f148a3ba96a58759b4befcdadb171c"
        	]
        }
    }
});

// ========================================= GROUPS =========================================

db.groups.insert({
	"_id": "5c79022e7884583d1ebb6e5d0bc0121822684250a3fd2996fd93c04634363363",
	"identifier": "101",
	"name": "Groupe acces complet",
	"description": "Acces à toutes les APP",
	"enabled": true,
	"profileIds": [
		"system_group_profile",
		"system_hierarchy_profile",
		"system_account_profile",
		"system_customer_profile",
		"system_profile_profile",
		"system_surrogate_profile",
		"system_user_profile",
		"5c7927537884583d1ebb6e769fcbc58f86f148a3ba96a58759b4befcdadb171c",
        "system_access_contract",
        "system_ingest_contract",
        "system_agencies",
        "system_file_format",
        "system_context",
        "system_security_profile",
        "system_ontology",
        "system_audit",
        "system_secure",
        "system_dsl",
        "system_probative_value",
        "system_logbook_operation",
        "system_holding_filling_scheme_profile"
	],
	"readonly": false,
	"level": "",
	"customerId": "system_customer",
	"_class": "groups"
});

db.groups.insert({
	"_id": "5caf30f57884585a1dcedc36759ce99393a94722aa3698482ec8fa95a12732d4",
	"identifier": "133",
	"name": "Groupe acces complet EMO",
	"description": "Acces à toutes les APP",
	"enabled": true,
	"profileIds": [
		"system_group_profile",
		"system_hierarchy_profile",
		"system_account_profile",
		"system_customer_profile",
		"system_profile_profile",
		"system_surrogate_profile",
		"system_user_profile",
		"5c7927537884583d1ebb6e769fcbc58f86f148a3ba96a58759b4befcdadb171c"
	],
	"readonly": false,
	"level": "",
	"customerId": "system_customer",
	"_class": "groups"
});

db.groups.insert({
	"_id": "5c79026f7884583d1ebb6e5f3c1910a7420244e7ac4638c42383831b2c64ed46",
	"identifier": "102",
	"name": "Groupe de l'utilisateur Démo",
	"description": "Groupe de l'utilisateur Démo",
	"enabled": true,
	"profileIds": [
		"system_group_profile",
		"system_hierarchy_profile",
		"system_customer_profile",
		"system_profile_profile",
		"system_surrogate_profile",
		"system_user_profile",
		"5c7927537884583d1ebb6e769fcbc58f86f148a3ba96a58759b4befcdadb171c"
	],
	"readonly": false,
	"level": "",
	"customerId": "system_customer",
	"_class": "groups"
});

// ========================================= USERS =========================================

db.users.insert(
{
	"_id": "5c7902ee7884583d1ebb6e6063569f12508f46e293f99e31a96c6b4449a7574f",
	"email": "demo@{{ vitamui_platform_informations.default_email_domain }}",
	"firstname": "Demo",
	"identifier": "101",
	"otp": false,
	"subrogeable": false,
	"lastname": "UTILISATEUR",
	"language": "FRENCH",
	"groupId": "5c79026f7884583d1ebb6e5f3c1910a7420244e7ac4638c42383831b2c64ed46",
	"nbFailedAttempts": 0,
	"status": "ENABLED",
	"type": "NOMINATIVE",
	"readonly": false,
	"level": "",
	"password" : "$2a$10$8fZVa7gCaj9UTNMAn36C5uyfPt7WQ.Vj1SqHzknWLkzhq9xFBClXy",
	"passwordExpirationDate": "2050-01-09T00:00:00.000+01:00",
	"customerId": "system_customer",
	"_class": "users"
});

db.providers.updateOne( {
	"_id": "system_idp"
}, {
    $addToSet: {
        "patterns":  {
            $each: [
            	"demo.*@{{ vitamui_platform_informations.default_email_domain }}"
        	]
        }
    }
});

print("END 101_iam_system_demo.js");
