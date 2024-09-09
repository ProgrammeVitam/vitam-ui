db = db.getSiblingDB('security')

print("START_002_update_collect_ui_contexts.js");

db.contexts.updateOne({
   "_id":"ui_collect_context"
},
{
   $addToSet:{
      "roleNames":{
         $each:[
                 "ROLE_COLLECT_UPDATE_BULK_ARCHIVE_UNIT",
                 "ROLE_COLLECT_UPDATE_UNITARY_ARCHIVE_UNIT",
                 "ROLE_COLLECT_GET_ARCHIVE_BINARY",
                 "ROLE_COLLECT_GET_ARCHIVE_SEARCH",
                 "ROLE_GET_ONTOLOGIES",
                 "ROLE_GET_FILLING_PLAN_ACCESS"
        	]
        }
    }
});

db.contexts.updateOne({
   "_id":"ui_collect_context"
},
{
 $pull: {
      roleNames: {
        $in: [
            "ROLE_DELETE_TRANSACTIONS",
            "ROLE_UPDATE_UNIT_DESC_METADATA",
            "ROLE_UPDATE_UNITS_METADATA",
            "ROLE_GET_ARCHIVE_SEARCH"
        ] }
    }
});
print("END_002_update_collect_ui_contexts.js");
