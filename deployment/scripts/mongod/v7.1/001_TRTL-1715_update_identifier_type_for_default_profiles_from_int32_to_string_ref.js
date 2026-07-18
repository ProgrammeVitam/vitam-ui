const bulkOps = [];
dbIam.profiles.find({ "identifier": { $type: 'int' } }).forEach(doc => {
    bulkOps.push({
        updateOne: {
            filter: {
                "_id": doc._id
            },
            update: {
                $set: {
                    "identifier": doc.identifier.toString()
                }
            }
        }
    });
});

dbIam.profiles.bulkWrite(bulkOps);
