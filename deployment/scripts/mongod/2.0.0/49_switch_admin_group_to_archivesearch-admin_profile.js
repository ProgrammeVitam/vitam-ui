print("Update group: admin_group");

dbIam.groups.updateOne(
    { "_id": "admin_group" },
    {
        $addToSet: {
            "profileIds": "PROFIL_1-ARCHIVE_SEARCH_MANAGEMENT_APP-ADMIN"
        }
    }
);

dbIam.groups.updateOne(
    { "_id": "admin_group" },
    {
        $pull: {
            "profileIds": "PROFIL_1-ARCHIVE_SEARCH_MANAGEMENT_APP-CONSULTATION"
        }
    }
);
