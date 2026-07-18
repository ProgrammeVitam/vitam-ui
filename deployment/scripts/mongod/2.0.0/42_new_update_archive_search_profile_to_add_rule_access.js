db = db.getSiblingDB('iam')

print("42_new_update_archive_search_profile_to_add_rule_access.js");

db.profiles.updateOne({
   "_id":"system_archive_search_profile_archiviste_administrator"
},
{
   $addToSet:{
      "roles":{
         $each:[
             {
               "name" : "ROLE_GET_RULES"
             }
        	]
        }
    }
});

db.profiles.updateOne({
   "_id":"system_archive_search_profile_consultation"
},
{
   $addToSet:{
      "roles":{
         $each:[
             {
               "name" : "ROLE_GET_RULES"
             }
        	]
        }
    }
});

db.profiles.updateOne({
   "_id":"system_archive_search_profile_archiviste"
},
{
   $addToSet:{
      "roles":{
         $each:[
             {
               "name" : "ROLE_GET_RULES"
             }
        	]
        }
    }
});

print("42_new_update_archive_search_profile_to_add_rule_access.js");
