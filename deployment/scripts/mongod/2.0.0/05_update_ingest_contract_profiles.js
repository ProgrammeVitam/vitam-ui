db = db.getSiblingDB('iam')

print("START 05_update_ingest_contract_profiles.js");

// ========================================= PROFILES =========================================
db.profiles.updateOne( {
	"_id": "system_ingest_contract"
}, {
    $addToSet: {
        "roles":  {
            $each: [
				{
            		"name": "ROLE_GET_ARCHIVE_PROFILES"
        		}
        	]
        }
    }
});

db.profiles.updateOne( {
	"_id": "system_customer_profile"
}, {
    $addToSet: {
        "roles":  {
            $each: [
				{
            		"name": "ROLE_GET_ARCHIVE_PROFILES"
        		}
        	]
        }
    }
});

print("END 05_update_ingest_contract_profiles.js");
