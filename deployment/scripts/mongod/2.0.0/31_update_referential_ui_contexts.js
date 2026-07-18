dbSecurity.contexts.updateOne(
   { "_id": "ui_referential_context" },
   {
      $addToSet: {
         "roleNames": {
            $each: [
               "ROLE_GET_FILLING_PLAN_ACCESS"
            ]
         }
      }
   }
);
