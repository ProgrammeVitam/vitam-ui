db = db.getSiblingDB('iam');
dbSecurity = db.getSiblingDB('security')

print("START 57_migration_collect_profiles_roles.js");

// ---- ROLE_UPDATE_UNIT_DESC_METADATA will be added to profiles including COLLECT_APP --- //

db.profiles.updateMany({
   "applicationName": "COLLECT_APP"
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

dbSecurity.contexts.updateOne({
   "_id":"ui_collect_context"
},
{
   $addToSet: {
       "roleNames": "ROLE_UPDATE_UNIT_DESC_METADATA"
   }
});

print("END 57_migration_collect_profiles_roles.js");
