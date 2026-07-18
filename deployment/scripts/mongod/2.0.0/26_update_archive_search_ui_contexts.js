db = db.getSiblingDB('security')

print("START_26_update_archive_search_ui_contexts.js");

db.contexts.updateOne({
   "_id":"ui_archive_search_context"
},
{
   $addToSet:{
      "roleNames":{
         $each:[
            "ROLE_SEARCH_WITH_RULES"
        	]
        }
    }
});

print("END_26_update_archive_search_ui_contexts.js");
