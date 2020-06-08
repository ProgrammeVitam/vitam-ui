db = db.getSiblingDB('iam')

print("START 101_iam_system_plus_demo.js");

// ========================================= GROUPS =========================================
db.groups.insert({
	"_id": "5c7e4edf7884583d1ebb6f891c55c325bb7a4a768d18a01cb68b9530e65d07c0",
	"identifier": "114",
	"name": "Groupe Groupe de profils",
	"description": "Groupe avec seulement l'APP Groupe de profils",
	"enabled": true,
	"profileIds": [
		"system_group_profile"
	],
	"readonly": false,
	"level": "",
	"customerId": "system_customer",
	"_class": "groups"
});

db.groups.insert({
	"_id": "5c7e4ef07884583d1ebb6f8bc378561a559341bc83936231b69696eca8a51192",
	"identifier": "115",
	"name": "Groupe Hierarchisation",
	"description": "Groupe avec seulement l'APP Hierarchisation des profils",
	"enabled": true,
	"profileIds": [
		"5c7927537884583d1ebb6e769fcbc58f86f148a3ba96a58759b4befcdadb171c",
		"system_hierarchy_profile"
	],
	"readonly": false,
	"level": "",
	"customerId": "system_customer",
	"_class": "groups"
});

db.groups.insert({
	"_id": "5c7e4f517884583d1ebb6f9195ce6dee372d4da4b7b26274268df1296e29e787",
	"identifier": "118",
	"name": "Groupe Mon compte",
	"description": "Groupe avec seulement l'APP Mon Compte",
	"enabled": true,
	"profileIds": [
		"system_account_profile"
	],
	"readonly": false,
	"level": "",
	"customerId": "system_customer",
	"_class": "groups"
});

db.groups.insert({
	"_id": "5c7e4f617884583d1ebb6f9373fb764c414941ec9d4a7f93d9a83e244d1400e9",
	"identifier": "119",
	"name": "Groupe Organisations",
	"description": "Groupe avec seulement l'APP Organisations",
	"enabled": true,
	"profileIds": [
		"system_customer_profile"
	],
	"readonly": false,
	"level": "",
	"customerId": "system_customer",
	"_class": "groups"
});

db.groups.insert({
	"_id": "5c7e4f987884583d1ebb6f97fdb1292186984af49662d4d235bd6f8ddfdae99c",
	"identifier": "121",
	"name": "Groupe Profils user",
	"description": "Groupe avec seulement l'APP Profils APP Utilisateurs",
	"enabled": true,
	"profileIds": [
		"system_profile_profile"
	],
	"readonly": false,
	"level": "",
	"customerId": "system_customer",
	"_class": "groups"
});

db.groups.insert({
	"_id": "5c7e4fba7884583d1ebb6f9b68989a2357ab44f2bfa8f53b40e829a303a3ef86",
	"identifier": "123",
	"name": "Groupe Subrogation",
	"description": "Groupe avec seulement l'APP Subrogation",
	"enabled": true,
	"profileIds": [
		"system_surrogate_profile"
	],
	"readonly": false,
	"level": "",
	"customerId": "system_customer",
	"_class": "groups"
});


db.groups.insert({
	"_id": "5c7e4ff77884583d1ebb6fa1b6f6542a91fd44a29b0afcec6ca062b02f664677",
	"identifier": "126",
	"name": "Groupe Utilisateur",
	"description": "Groupe avec seulement l'APP Utilisateurs",
	"enabled": true,
	"profileIds": [
		"system_user_profile"
	],
	"readonly": false,
	"level": "",
	"customerId": "system_customer",
	"_class": "groups"
});


// ========================================= USERS =========================================

db.users.insert(
{
	"_id": "5c7e50e47884583d1ebb6fb63e29ffa861654af3b758f05902471f810cb2263d",
	"email": "demohierarchisation@{{ vitamui_platform_informations.default_email_domain }}",
	"firstname": "Demo",
	"identifier": "118",
	"otp": false,
	"subrogeable": false,
	"lastname": "HIERARCHISATION",
	"language": "FRENCH",
	"groupId": "5c7e4ef07884583d1ebb6f8bc378561a559341bc83936231b69696eca8a51192",
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
db.users.insert(
{
	"_id": "5c7e512b7884583d1ebb6fbcf7d480e06d3748a3b36cc55ad9b2d0e242e673fe",
	"email": "demomoncompte@{{ vitamui_platform_informations.default_email_domain }}",
	"firstname": "Demo",
	"identifier": "121",
	"otp": false,
	"subrogeable": false,
	"lastname": "MON COMPTE",
	"language": "FRENCH",
	"groupId": "5c7e4f517884583d1ebb6f9195ce6dee372d4da4b7b26274268df1296e29e787",
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
db.users.insert(
{
	"_id": "5c7e513d7884583d1ebb6fbe48233e218ee64c79bb76a688fba02877c588618c",
	"email": "demoorganisations@{{ vitamui_platform_informations.default_email_domain }}",
	"firstname": "Demo",
	"identifier": "122",
	"otp": false,
	"subrogeable": false,
	"lastname": "ORGANISATIONS",
	"language": "FRENCH",
	"groupId": "5c7e4f617884583d1ebb6f9373fb764c414941ec9d4a7f93d9a83e244d1400e9",
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
db.users.insert(
{
	"_id": "5c7e515e7884583d1ebb6fc29a46e429db5f47d0b9ae210ce2d710b3675c2cec",
	"email": "demoprofilsusers@{{ vitamui_platform_informations.default_email_domain }}",
	"firstname": "Demo",
	"identifier": "124",
	"otp": false,
	"subrogeable": false,
	"lastname": "PROFILS USERS",
	"language": "FRENCH",
	"groupId": "5c7e4f987884583d1ebb6f97fdb1292186984af49662d4d235bd6f8ddfdae99c",
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
db.users.insert(
{
	"_id": "5c7e51a27884583d1ebb6fca0a6d9782f103447bbfe799d10fc615c73520b7d8",
	"email": "demosubrogation@{{ vitamui_platform_informations.default_email_domain }}",
	"firstname": "Demo",
	"identifier": "128",
	"otp": false,
	"subrogeable": false,
	"lastname": "SUBROGATION",
	"language": "FRENCH",
	"groupId": "5c7e4fba7884583d1ebb6f9b68989a2357ab44f2bfa8f53b40e829a303a3ef86",
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
db.users.insert(
{
	"_id": "5c7e51b77884583d1ebb6fccb8dfe9dbca1f40e0aa9d3ee8c46e6a834fc56067",
	"email": "demoutilisateurs@{{ vitamui_platform_informations.default_email_domain }}",
	"firstname": "Demo",
	"identifier": "129",
	"otp": false,
	"subrogeable": false,
	"lastname": "UTILISATEURS",
	"language": "FRENCH",
	"groupId": "5c7e4ff77884583d1ebb6fa1b6f6542a91fd44a29b0afcec6ca062b02f664677",
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

print("END 101_iam_system_plus_demo.js");
