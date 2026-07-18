db = db.getSiblingDB('security')

print("START_41_update_ui_archive_context.js");

db.contexts.updateOne({
   "_id":"ui_archive_search_context"
},
{
   $addToSet:{
      "roleNames":{
         $each:[
            "ROLE_UPDATE_MANAGEMENT_RULES"
        	]
        }
    }
});

print("END_41_update_ui_archive_context.js");
