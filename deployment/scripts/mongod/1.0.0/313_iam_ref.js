db = db.getSiblingDB('iam')

print("START 313_iam_ref.js");

// ========================================= PROFILES =========================================
db.profiles.updateOne( {
	"_id": "system_dsl"
}, {
    $addToSet: {
        "roles":  {
            $each: [
				{
            		"name": "ROLE_GET_UNITS"
        		}
        	]
        }
    }
});

print("END 313_iam_ref.js");