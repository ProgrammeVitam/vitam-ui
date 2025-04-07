print("START v8.1.0-02_update_archive_search_app.js");

dbIam = db.getSiblingDB('{{ mongodb.iam.db | default('iam') }}');
dbIam.profiles.updateMany({
   "applicationName": "ARCHIVE_SEARCH_MANAGEMENT_APP"
},
   {
      "$addToSet": {
         "roles": {
            $each: [
               { "name": "ROLE_GET_ARCHIVE_PROFILES_UNIT" },
               { "name": "ROLE_GET_AGENCIES" },
               { "name": "ROLE_GET_ARCHIVE_PROFILES_UNIT" },
               { "name": "ROLE_GET_FILLING_PLAN_ACCESS" }
            ]
         }
      }
   });

dbSecurity = db.getSiblingDB('{{ mongodb.security.db | default('security') }}')
dbSecurity.contexts.updateOne({
   "_id": "ui_archive_search_context"
},
   {
      $addToSet: {
         roleNames: {
            $each: [
               "ROLE_GET_ARCHIVE_PROFILES_UNIT",
               "ROLE_GET_AGENCIES",
               "ROLE_GET_ARCHIVE_PROFILES_UNIT",
               "ROLE_GET_FILLING_PLAN_ACCESS"
            ]
         }
      }
   });

print("END v8.1.0-02_update_archive_search_app.js");
