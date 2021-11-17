print("START 013_REC-385_fix_user_info_role_duplication_ref.js");

db = db.getSiblingDB("iam");

db.profiles.find({ applicationName: "USERS_APP" }).forEach((profile) => {
  var distinct_roles = [...new Set(profile.roles.map((role) => role.name))].map(
    (role) => {
      return { name: role };
    }
  );

  // Update only roles whith duplication
  if (distinct_roles.length !== profile.roles.length) {
    db.profiles.update(
      { _id: profile._id },
      { $set: { roles: distinct_roles } }
    );
  }
});

print("END 013_REC-385_fix_user_info_role_duplication_ref.js");
