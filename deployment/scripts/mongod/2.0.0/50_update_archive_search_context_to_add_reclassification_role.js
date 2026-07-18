db = db.getSiblingDB('security')

print("START_50_update_archive_search_context_to_add_reclassification_role.js");

db.contexts.updateOne({
   "_id":"ui_archive_search_context"
},
{
   $addToSet:{
      "roleNames":{
         $each:[
            "ROLE_RECLASSIFICATION"
        	]
        }
    }
});

print("END_50_update_archive_search_context_to_add_reclassification_role.js");
