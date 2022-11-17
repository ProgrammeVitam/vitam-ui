print("START 012_REC-385_update_user_info_role_ref.js");

db = db.getSiblingDB("iam");

db.profiles.updateMany(
  { applicationName: "USERS_APP" },
  { $addToSet: { roles: { $each: [{ name: "ROLE_GET_USER_INFOS" }] } } }
);

db.profiles.updateMany(
  { applicationName: "USERS_APP", roles: { name: "ROLE_CREATE_USERS" } },
  { $addToSet: { roles: { $each: [{ name: "ROLE_CREATE_USER_INFOS" }] } } }
);

db.profiles.updateMany(
  { applicationName: "USERS_APP", roles: { name: "ROLE_UPDATE_USERS" } },
  { $addToSet: { roles: { $each: [{ name: "ROLE_UPDATE_USER_INFOS" }] } } }
);

print("END 012_REC-385_update_user_info_role_ref.js");
