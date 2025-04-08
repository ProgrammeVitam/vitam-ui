print("START 02_fix_duplicate_profiles_identifier.js");

db = db.getSiblingDB('{{ mongodb.iam.db }}')

const duplicates = db.profiles.aggregate([
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

var maxIdProfile = db.getCollection('sequences').findOne({'_id': 'profile_identifier'}).sequence;

duplicates.forEach(dup => {
  dup.docs.slice(1).forEach((docId, index) => { // Skip first, update the rest
    db.profiles.updateOne(
      { _id: docId },
      { $set: { identifier: NumberInt(maxIdProfile++) } }
    );
  });
});

db.sequences.updateOne({
    "_id": "profile_identifier"
}, {
	$set: {
		"sequence": NumberInt(maxIdProfile)
	}
});

print("END 02_fix_duplicate_profiles_identifier.js");
