print("START 002_TRV-389_add_index_on_updatedDate_field_of_tokens_collection_ref.js");

db = db.getSiblingDB("iam");

db.tokens.dropIndex("updatedDate_1");

db.tokens.createIndex(
  {updatedDate: 1},
  {name: "idx_token_date", expireAfterSeconds: 0, background: true}
);

print("END 002_TRV-389_add_index_on_updatedDate_field_of_tokens_collection_ref.js");
