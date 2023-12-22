print("START 039_REC-1428_add_index_on_date_field_of_subrogations_collection_ref.js");

db = db.getSiblingDB("iam");


var collectionExists = db.getCollectionNames().indexOf('subrogations') > -1;

if (collectionExists) {
  db.subrogations.dropIndex("idx_subrogation_date");

  db.subrogations.createIndex(
    { date: 1 },
    { expireAfterSeconds: 0, background: true, name: "idx_subrogation_date" }
  );
}


print("END 039_REC-1428_add_index_on_date_field_of_subrogations_collection_ref.js");
