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

print("Update group (test): 5c79022e7884583d1ebb6e5d0bc0121822684250a3fd2996fd93c04634363363");

dbIam.groups.updateOne(
    {
        "_id": "5c79022e7884583d1ebb6e5d0bc0121822684250a3fd2996fd93c04634363363"
    },
    {
        $addToSet: {
            "profileIds": "PROFIL_1-ARCHIVE_SEARCH_MANAGEMENT_APP-ADMIN"
        }
    }
);

dbIam.groups.updateOne(
    {
        "_id": "5c79022e7884583d1ebb6e5d0bc0121822684250a3fd2996fd93c04634363363"
    },
    {
        $pull: {
            "profileIds": "PROFIL_1-ARCHIVE_SEARCH_MANAGEMENT_APP-CONSULTATION"
        }
    }
);
