// Add new roles for all archive search profiles
dbIam.profiles.updateMany(
    { "applicationName": "ARCHIVE_SEARCH_MANAGEMENT_APP" },
    {
        $addToSet: {
            "roles": {
                $each: [
                    { "name": "ROLE_ARCHIVE_SEARCH_GET_ARCHIVE_SEARCH" },
                    { "name": "ROLE_ARCHIVE_SEARCH_GET_ARCHIVE_BINARY" }
                ]
            }
        }
    }
);

// Add new roles on profiles having roles for updating units
dbIam.profiles.updateMany(
    {
        "applicationName": "ARCHIVE_SEARCH_MANAGEMENT_APP",
        "roles.name": "ROLE_UPDATE_UNIT_DESC_METADATA"
    },
    {
        $addToSet: {
            "roles": {
                $each: [
                    {
                        "name": "ROLE_ARCHIVE_SEARCH_UPDATE_ARCHIVE_UNIT"
                    }
                ]
            }
        }
    }
);

// Remove old roles from archive search profiles
dbIam.profiles.updateMany(
    { "applicationName": "ARCHIVE_SEARCH_MANAGEMENT_APP" },
    {
        $pull: {
            "roles": {
                "name": {
                    $in:
                        [
                            "ROLE_GET_ALL_ARCHIVE_SEARCH",
                            "ROLE_SEARCH_WITH_RULES",
                            "ROLE_CREATE_ARCHIVE_SEARCH",
                            "ROLE_GET_ARCHIVE_SEARCH",
                            "ROLE_UPDATE_UNIT_DESC_METADATA"
                        ]
                }
            }
        }
    }
);
