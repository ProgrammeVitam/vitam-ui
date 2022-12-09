db = db.getSiblingDB('iam');

print("START 56_migration_archive_search_profiles_roles.js");

db.profiles.updateMany({
   "name": {$regex : "^Archiviste administrateur"},
   "applicationName": "ARCHIVE_SEARCH_MANAGEMENT_APP"
},
{
   "$addToSet": {
      "roles":{
           $each: [
              {
              "name": "ROLE_RECLASSIFICATION"
              },
              {
              "name": "ROLE_UPDATE_MANAGEMENT_RULES"
              },
              {
              "name": "ROLE_COMPUTED_INHERITED_RULES"
              }
          ]
      }
   }
});

// ---- le role ROLE_UPDATE_UNIT_DESC_METADATA sera ajouté pour l'ensemble des profiles archviste et archiviste administrateur --- //

db.profiles.updateMany({
   "name": {$regex : "^Archiviste"},
   "applicationName": "ARCHIVE_SEARCH_MANAGEMENT_APP"
},
{
   "$addToSet": {
      "roles":{
           $each: [
              {
              "name":"ROLE_UPDATE_UNIT_DESC_METADATA"
              }
          ]
      }
   }
});

print("END 56_migration_archive_search_profiles_roles.js");
