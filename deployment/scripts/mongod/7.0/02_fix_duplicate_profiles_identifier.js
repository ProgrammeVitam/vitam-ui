const duplicates = dbIam.profiles.aggregate([
    {
        $group: {
            _id: "$identifier",
            count: { $sum: 1 },
            docs: { $push: "$_id" }
        }
    },
    {
        $match: { count: { $gt: 1 } }
    }
]).toArray();

var maxIdProfile = dbIam.getCollection('sequences').findOne({ '_id': 'profile_identifier' }).sequence;

duplicates.forEach(dup => {
    dup.docs.slice(1).forEach((docId, index) => { // Skip first, update the rest
        dbIam.profiles.updateOne(
            { "_id": docId },
            {
                $set: {
                    "identifier": NumberInt(maxIdProfile++)
                }
            }
        );
    });
});

dbIam.sequences.updateOne(
    { "_id": "profile_identifier" },
    {
        $set: {
            "sequence": NumberInt(maxIdProfile)
        }
    }
);
