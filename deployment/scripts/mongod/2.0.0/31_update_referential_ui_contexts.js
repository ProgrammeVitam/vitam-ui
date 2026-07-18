db = db.getSiblingDB('security')

print("START_31_update_referential_ui_contexts.js");

db.contexts.updateOne({
   "_id":"ui_referential_context"
},
{
   $addToSet:{
      "roleNames":{
         $each:[
            "ROLE_GET_FILLING_PLAN_ACCESS"
        	]
        }
    }
});

print("END_31_update_referential_ui_contexts.js");
