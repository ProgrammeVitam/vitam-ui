dbIam = db.getSiblingDB('{{ mongodb.iam.db | default('iam') }}')
dbSecurity = db.getSiblingDB('{{ mongodb.security.db | default('security') }}')

print("START v9.1.0-02_add_archive_search_originating_agency_reassignment_roles");

// Add archive search originating agency reassignment roles to archive admin profiles
dbIam.profiles.updateMany({
    applicationName: "ARCHIVE_SEARCH_MANAGEMENT_APP",
     name: {
                $regex : "Archiviste administrateur"
            }
}, {
    $addToSet: {
        roles: { name: "ROLE_ORIGINATING_AGENCY_REASSIGNMENT" }
    }
});

// Add archive search originating agency reassignment roles to archive-search-ui context
dbSecurity.contexts.updateOne({
    "_id": "ui_archive_search_context"
}, {
    $addToSet: {
        "roleNames": "ROLE_ORIGINATING_AGENCY_REASSIGNMENT"
    }
});

print("END v9.1.0-02_add_archive_search_originating_agency_reassignment_roles");
