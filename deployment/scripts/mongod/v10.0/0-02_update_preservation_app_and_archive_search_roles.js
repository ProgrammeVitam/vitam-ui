dbIam.applications.updateOne(
    {"identifier": "PRESERVATION_APP"},
    {
        $set: {
            "name": "Griffons et scénarios de préservation"}
    }
);

// Add archive search preservation roles to archive admin profiles
dbIam.profiles.updateMany({
    applicationName: "ARCHIVE_SEARCH_MANAGEMENT_APP",
    name: {
        $regex: "Archiviste administrateur"
    }
}, {
    $addToSet: {
        roles: {
            $each: [
                { name: "ROLE_GET_PRESERVATION_SCENARIOS" },
                { name: "ROLE_LAUNCH_PRESERVATION" }
            ]
        }
    }
});

// Add archive search preservation roles to archive-search-ui context
dbSecurity.contexts.updateOne({
    "_id": "ui_archive_search_context"
}, {
    $addToSet: {
        "roleNames": {
            $each: [
                "ROLE_GET_PRESERVATION_SCENARIOS",
                "ROLE_LAUNCH_PRESERVATION"
            ]
        }
    }
});
