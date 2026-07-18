db = db.getSiblingDB('security');

print("START 14_update_collecte_contexts_to_add_abort_reopen_transaction_role.js");



db.contexts.updateOne({
    "_id":"ui_collect_context"
  },
  {
    $addToSet:{
      "roleNames":{
        $each: [
            "ROLE_ABORT_TRANSACTIONS",
            "ROLE_REOPEN_TRANSACTIONS"
        ]
      }
    }
  });


print("END 14_update_collecte_contexts_to_add_abort_reopen_transaction_role.js");
