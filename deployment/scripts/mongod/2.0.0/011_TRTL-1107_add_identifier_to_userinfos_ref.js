dbIam.sequences.insertOne(
    {
        "_id": "user_infos_identifier",
        "name": "userInfosIdentifier",
        "sequence": NumberInt(100)
    }
);

var maxIdentifier = dbIam.getCollection('sequences').findOne({ '_id': 'user_infos_identifier' }).sequence;

dbIam.userInfos.find({ identifier: { $eq: null } }).forEach(userInfos => {

    dbIam.userInfos.updateOne(
        { "_id": userInfos._id },
        {
            $set: {
                "identifier": NumberInt(maxIdentifier + 1)
            }
        }
    );
    maxIdentifier++;
});

dbIam.sequences.updateOne(
    { "_id": "user_infos_identifier" },
    {
        $set: {
            "sequence": NumberInt(maxIdentifier)
        }
    }
);
