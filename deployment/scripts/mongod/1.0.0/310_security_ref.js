db = db.getSiblingDB('security')

print("START 310_security_ref.js");

db.contexts.updateOne( {
	"_id": "ui_referential_context"
}, {
    $addToSet: {
        "roleNames":  {
            $each: [
            	"ROLE_GET_RULES", "ROLE_CREATE_RULES", "ROLE_UPDATE_RULES", "ROLE_DELETE_RULES",
				"ROLE_IMPORT_AGENCIES",
				"ROLE_IMPORT_FILE_FORMATS",
				"ROLE_IMPORT_ONTOLOGIES"
        	]
        }
    }
});

print("END 310_security_ref.js");