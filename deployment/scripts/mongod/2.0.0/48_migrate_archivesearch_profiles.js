//======== MIGRATE OLD PROFILES FOR APP ARCHIVE_SEARCH_MANAGEMENT_APP ========//
dbIam.groups.aggregate([
    {
        $lookup: {
            from: "profiles",
            localField: "profileIds",
            foreignField: "_id",
            as: "fullProfiles"
        }
    },
    { $match: { "fullProfiles.applicationName": "ARCHIVE_SEARCH_MANAGEMENT_APP" } }
]).forEach(fullGroup => {
    // print(JSON.stringify(fullGroup));
    fullGroup.fullProfiles.forEach(profileToReplace => {
        // print(JSON.stringify(profileToReplace));

        const expectedProfileId = "PROFIL_" + profileToReplace.tenantIdentifier + "-ARCHIVE_SEARCH_MANAGEMENT_APP-CONSULTATION";

        if (profileToReplace.applicationName == "ARCHIVE_SEARCH_MANAGEMENT_APP" && profileToReplace._id != expectedProfileId) {

            print("Original group: " + JSON.stringify(fullGroup));

            // We check if the expected profile exist; otherwise we couldn't associate to it
            const newProfile = dbIam.profiles.findOne({ "_id": expectedProfileId });

            print("Group: " + fullGroup.name);
            print("Migrate profileId: " + profileToReplace._id + " to " + newProfile._id);

            var bulk = dbIam.groups.initializeOrderedBulkOp();
            bulk.find({ "_id": fullGroup._id }).updateOne({ "$addToSet": { "profileIds": newProfile._id } });
            bulk.find({ "_id": fullGroup._id }).updateOne({ "$pull": { "profileIds": profileToReplace._id } });
            bulk.execute();

            print("Updated group: " + JSON.stringify(dbIam.groups.findOne({ "_id": fullGroup._id })));
        }

    });

});

//======== DELETE OLD PROFILES FOR APP ARCHIVE_SEARCH_MANAGEMENT_APP ========//
dbIam.profiles.deleteMany(
    {
        $and: [
            { "applicationName": "ARCHIVE_SEARCH_MANAGEMENT_APP" },
            { "_id": { $not: { $regex: "^PROFIL_" } } }
        ]
    }
);
