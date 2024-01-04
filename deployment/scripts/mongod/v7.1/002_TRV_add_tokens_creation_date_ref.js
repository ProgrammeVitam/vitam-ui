print('START 002_TRV_add_tokens_creation_date_ref');

db = db.getSiblingDB('iam')

db.tokens.updateMany(
  {},
  [
    {
      $set: {
        "createdDate": "$updatedDate"
      }
    }
  ]
);

print('END 002_TRV_add_tokens_creation_date_ref.js');
