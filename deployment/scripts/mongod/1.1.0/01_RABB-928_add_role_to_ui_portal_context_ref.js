print("START 01_RABB-928_add_role_to_ui_portal_context_ref.js");

db = db.getSiblingDB("security");

db.contexts.updateOne(
  {
    _id: "ui_portal_context",
  },
  {
    $addToSet: { roleNames: { $each: ["ROLE_UPDATE_ME_USERS"] } },
  }
);

print("END 01_RABB-928_add_role_to_ui_portal_context_ref.js");
