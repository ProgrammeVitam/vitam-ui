print('START 001_TRTL-1715_update_identifier_type_for_default_profiles_from_int32_to_string_ref.js');

db = db.getSiblingDB('iam');

db.profiles.updateMany(
  { "identifier": { $type: 'int' } },
  {
    $set: {
      "identifier": { $toString: '$identifier' }
    }
  }
);

print('END 001_TRTL-1715_update_identifier_type_for_default_profiles_from_int32_to_string_ref.js');
