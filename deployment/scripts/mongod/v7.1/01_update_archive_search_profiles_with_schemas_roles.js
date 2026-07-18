db = db.getSiblingDB('iam')

print("Start - 01_update_archive_search_profiles_with_schemas_roles");

db.profiles.updateMany({
  applicationName: "ARCHIVE_SEARCH_MANAGEMENT_APP"
}, {
  $addToSet: {
    roles:
      { name: "ROLE_GET_SCHEMAS" }
  }
});

print("End - 01_update_archive_search_profiles_with_schemas_roles");
