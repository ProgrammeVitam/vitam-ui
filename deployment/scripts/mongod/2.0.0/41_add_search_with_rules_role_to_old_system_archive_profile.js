// -------- ADD SEARCH WITH RULES TO OLD ARCHIVE PROFILE-----
dbIam.profiles.updateOne(
    { "applicationName": "ARCHIVE_SEARCH_MANAGEMENT_APP" },
    {
        $addToSet: {
            "roles": {
                $each: [
                    { "name": "ROLE_SEARCH_WITH_RULES" }
                ]
            }
        }
    }
);

// ---- DELETE EXISTING ROLES ROLE_EXPORT_DIP AND ROLE_ELIMINATION TO BE CONFORM WITH CONSULTATION PROFILE FOR EXISTING USERS
dbIam.profiles.updateOne(
    { "_id": "system_archive_search_profile" },
    {
        $pull: {
            "roles":
                { "name": "ROLE_EXPORT_DIP" }
        }
    }
);

dbIam.profiles.updateOne(
    { "_id": "system_archive_search_profile" },
    {
        $pull: {
            "roles":
                { "name": "ROLE_ELIMINATION" }
        }
    }
);

// ---- DELETE UNUSED PROFILE
dbIam.profiles.deleteOne({ "_id": "system_archive_search_profile_without_rules" })
dbIam.profiles.deleteOne({ "_id": "system_archive_search_profile_without_rules_with_export_and_with_elimination" })
dbIam.profiles.deleteOne({ "_id": "system_archive_search_profile_with_rules_without_export" })
dbIam.profiles.deleteOne({ "_id": "system_archive_search_profile_without_rules_with_export" })
