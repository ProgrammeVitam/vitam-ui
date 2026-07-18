db = db.getSiblingDB('iam')

print("START_44_update_archive_administrator_profile_to_add_computed_inherited_role.js");

db.profiles.updateOne({
   "_id":"system_archive_search_profile_archiviste_administrator"
},
{
   $addToSet:{
      "roles":{
         $each:[
             {
               "name" : "ROLE_COMPUTED_INHERITED_RULES"
             }
        	]
        }
    }
});

print("END_44_update_archive_administrator_profile_to_add_computed_inherited_role.js");
