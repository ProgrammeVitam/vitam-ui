// Update collect application position
dbIam.applications.updateOne(
    {
        "identifier": "COLLECT_APP",
        "category": "ingest_and_consultation"
    },
    {
        $set: {
            "position": NumberInt(1)
        }
    }
);

// Update ingest management application position
dbIam.applications.updateOne(
    {
        "identifier": "INGEST_MANAGEMENT_APP",
        "category": "ingest_and_consultation"
    },
    {
        $set: {
            "position": NumberInt(2)
        }
    }
);

// Update archive search management application position
dbIam.applications.updateOne(
    {
        "identifier": "ARCHIVE_SEARCH_MANAGEMENT_APP",
        "category": "ingest_and_consultation"
    },
    {
        $set: {
            "position": NumberInt(3)
        }
    }
);

// Update accession register application position
dbIam.applications.updateOne(
    {
        "identifier": "ACCESSION_REGISTER_APP",
        "category": "ingest_and_consultation"
    },
    {
        $set: {
            "position": NumberInt(4)
        }
    }
);
