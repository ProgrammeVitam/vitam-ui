db = db.getSiblingDB('iam')

print("START 015_RABB_fix_missing_collection_ref.js");

// Create collection only if it does not already exists
db.createCollection('externalParameters', { autoIndexId: true });

print("END 015_RABB_fix_missing_collection_ref.js");
