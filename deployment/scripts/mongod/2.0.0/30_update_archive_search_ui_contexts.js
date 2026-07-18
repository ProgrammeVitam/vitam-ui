db = db.getSiblingDB('security')

print("START_30_update_archive_search_ui_contexts.js");

db.contexts.updateOne({
   "_id":"ui_archive_search_context"
},
{
   $addToSet:{
      "roleNames":{
         $each:[
            "ROLE_GET_EXTERNAL_PARAMS"
        	]
        }
    }
});

print("END_30_update_archive_search_ui_contexts.js");
