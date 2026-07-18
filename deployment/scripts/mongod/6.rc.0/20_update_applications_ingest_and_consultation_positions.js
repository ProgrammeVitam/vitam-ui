db = db.getSiblingDB('iam');

print("START 20_update_applications_ingest_and_consultation_positions.js");

// Update collect application position

db.applications.updateOne({
    "identifier": "COLLECT_APP",
    "category" : "ingest_and_consultation"
}, {
	$set: {
		"position": NumberInt(1)
	}
});

// Update ingest management application position

db.applications.updateOne({
    "identifier": "INGEST_MANAGEMENT_APP",
    "category" : "ingest_and_consultation"
}, {
	$set: {
		"position": NumberInt(2)
	}
});

// Update archive search management application position

db.applications.updateOne({
    "identifier": "ARCHIVE_SEARCH_MANAGEMENT_APP",
    "category" : "ingest_and_consultation"
}, {
	$set: {
		"position": NumberInt(3)
	}
});

// Update accession register application position

db.applications.updateOne({
    "identifier": "ACCESSION_REGISTER_APP",
    "category" : "ingest_and_consultation"
}, {
	$set: {
		"position": NumberInt(4)
	}
});

print("END 20_update_applications_ingest_and_consultation_positions.js");
