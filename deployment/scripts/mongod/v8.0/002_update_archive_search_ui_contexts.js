db = db.getSiblingDB('security')

print("START_002_update_archive_search_ui_contexts.js");

db.contexts.updateOne({
   "_id":"ui_archive_search_context"
},
{
   $addToSet:{
      "roleNames":{
         $each:[
            "ROLE_ARCHIVE_SEARCH_GET_ARCHIVE_SEARCH",
            "ROLE_ARCHIVE_SEARCH_GET_ARCHIVE_BINARY",
            "ROLE_ARCHIVE_SEARCH_UPDATE_ARCHIVE_UNIT",
            "ROLE_GET_ONTOLOGIES"
        	]
        }
    }
});

db.contexts.updateOne({
   "_id":"ui_archive_search_context"
},
{
 $pull: {
      roleNames: {
        $in: [
        "ROLE_GET_ALL_ARCHIVE_SEARCH",
        "ROLE_SEARCH_WITH_RULES",
        "ROLE_CREATE_ARCHIVE_SEARCH",
        "ROLE_UPDATE_UNIT_DESC_METADATA",
        "ROLE_GET_ARCHIVE_SEARCH"
        ] }
    }
});
print("END_002_update_archive_search_ui_contexts.js");
