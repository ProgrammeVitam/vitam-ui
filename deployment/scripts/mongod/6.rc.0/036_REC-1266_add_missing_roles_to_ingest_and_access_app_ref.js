print("START 036_REC-1266_add_missing_roles_to_ingest_and_access_app_ref.js");

db = db.getSiblingDB("iam");

db.profiles.updateMany({
	"applicationName": "ACCESS_APP"
}, {
	"$push" : { "roles": 
                { "$each" : 
                        [
                            {"name" : "ROLE_GET_UNITS"},
                            {"name" : "ROLE_GET_EXTERNAL_PARAMS"}
                        ]
                }
	}
});

db.profiles.updateMany({
	"applicationName": "INGEST_APP"
}, {
	"$push" : { "roles": 
                { "$each" : 
                        [
                            {"name" : "ROLE_GET_FILE_FORMATS"},
                            {"name" : "ROLE_GET_UNITS"},
                            {"name" : "ROLE_GET_EXTERNAL_PARAMS"}
                        ]
                }
	}
});

print("END 036_REC-1266_add_missing_roles_to_ingest_and_access_app_ref.js");
