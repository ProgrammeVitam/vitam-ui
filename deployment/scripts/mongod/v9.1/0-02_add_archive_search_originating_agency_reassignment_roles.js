// Add archive search originating agency reassignment roles to archive admin profiles
dbIam.profiles.updateMany(
    {
        applicationName: "ARCHIVE_SEARCH_MANAGEMENT_APP",
        name: {
            $regex: "Archiviste administrateur"
        }
    },
    {
        $addToSet: {
            roles: { name: "ROLE_ORIGINATING_AGENCY_REASSIGNMENT" }
        }
    }
);

// Add archive search originating agency reassignment roles to archive-search-ui context
dbSecurity.contexts.updateOne(
    { "_id": "ui_archive_search_context" },
    {
        $addToSet: {
            "roleNames": "ROLE_ORIGINATING_AGENCY_REASSIGNMENT"
        }
    }
);
