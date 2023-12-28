print('START 001_TRTL-1715_update_identifier_type_for_default_profiles_from_int32_to_string_ref.js');

db = db.getSiblingDB('iam');

const bulkOps = [];
db.profiles.find({ "identifier": { $type: 'int' } }).forEach(doc => {
  bulkOps.push({
    updateOne: {
      filter: { _id: doc._id },
      update: { $set: { "identifier": doc.identifier.toString() } }
    }
  });
});
db.profiles.bulkWrite(bulkOps);

print('END 001_TRTL-1715_update_identifier_type_for_default_profiles_from_int32_to_string_ref.js');
