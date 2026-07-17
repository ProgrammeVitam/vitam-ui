dbIam.groups.updateOne(
    { "_id": "admin_group" },
    {
        $addToSet: {
            "profileIds": {
                $each: [
                    "system_archive_search_profile_archiviste_administrator"
                ]
            }
        }
    }
);

dbIam.groups.updateOne(
    { "_id": "admin_group" },
    {
        $pull: {
            "profileIds": "system_archive_search_profile"
        }
    }
);
