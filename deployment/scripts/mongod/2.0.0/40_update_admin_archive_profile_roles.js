db = db.getSiblingDB('iam')

print("START_40_update_admin_archive_profile_roles.js");

// -------- ARCHIVE_SEARCH ADMINISTRATOR PROFILE -----

db.profiles.updateOne({
   "_id":"system_archive_search_profile_archiviste_administrator"
},
{
   $addToSet:{
      "roles":{
         $each:[
            {
               "name":"ROLE_UPDATE_MANAGEMENT_RULES"
            }
         ]
      }
   }
});

print("END_40_update_admin_archive_profile_roles.js");
