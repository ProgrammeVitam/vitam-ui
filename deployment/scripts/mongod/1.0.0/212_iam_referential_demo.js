db = db.getSiblingDB('iam')

print("START 212_iam_referential_demo.js");

// ========================================= GROUPS =========================================

db.groups.updateOne( {
	"_id": "5c79022e7884583d1ebb6e5d0bc0121822684250a3fd2996fd93c04634363363"
}, {
    $addToSet: {
        "profileIds":  {
            $each: [
            	"system_access_contract",
                "system_ingest_contract",
                "system_agencies",
                "system_context",
                "system_security_profile",
                "system_ontology",
                "system_audit",
                "system_secure",
                "system_dsl",
                "system_probative_value",
                "system_logbook_operation"
        	]
        }
    }
});

db.groups.updateOne( {
	"_id": "5caf30f57884585a1dcedc36759ce99393a94722aa3698482ec8fa95a12732d4"
}, {
    $addToSet: {
        "profileIds":  {
            $each: [
            	"system_access_contract",
                "system_ingest_contract",
                "system_agencies",
                "system_context",
                "system_security_profile",
                "system_ontology",
                "system_audit",
                "system_secure",
                "system_dsl",
                "system_probative_value",
                "system_logbook_operation"
        	]
        }
    }
});

db.groups.updateOne( {
    "_id": "5c79026f7884583d1ebb6e5f3c1910a7420244e7ac4638c42383831b2c64ed46"
}, {
    $addToSet: {
        "profileIds":  {
            $each: [
            	"system_access_contract",
                "system_ingest_contract",
                "system_agencies",
                "system_context",
                "system_security_profile",
                "system_ontology",
                "system_audit",
                "system_secure",
                "system_dsl",
                "system_probative_value",
                "system_logbook_operation"
        	]
        }
    }
});

print("END 212_iam_referential_demo.js");
