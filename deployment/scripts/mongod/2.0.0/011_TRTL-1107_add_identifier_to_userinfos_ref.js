db = db.getSiblingDB('iam')

print("START 011_TRTL-1107_add_identifier_to_userinfos_ref");

db.sequences.insert({
    "_id": "user_infos_identifier",
    "name": "userInfosIdentifier",
    "sequence": NumberInt(100)
});
  
var maxIdentifier = db.getCollection('sequences').findOne({'_id': 'user_infos_identifier'}).sequence;

db.userInfos.find({identifier: {$eq: null}}).forEach(userInfos => {

    var result = db.userInfos.update(
        {_id: userInfos._id},
        {
            "$set": {"identifier": NumberInt(maxIdentifier+1)},
        }
    );
    maxIdentifier++;
});

db.sequences.updateOne({
    "_id": "user_infos_identifier"
  }, {
  $set: {
    "sequence": NumberInt(maxIdentifier)
  }
});

print("END 011_TRTL-1107_add_identifier_to_userinfos_ref");

