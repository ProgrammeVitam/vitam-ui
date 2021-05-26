db = db.getSiblingDB("iam");

print("Start 10_update_external_param_profile.js");
db.groups.updateOne(
  {
    _id: "admin_group",
    profileIds: { $nin: ["system_access-external-param-profile"] },
  },
  {
    $addToSet: { profileIds: "system_access-external-param-profile" },
  },
  {
    upsert: false,
  }
);

db.groups.updateOne(
  {
    _id: "5c79022e7884583d1ebb6e5d0bc0121822684250a3fd2996fd93c04634363363",
  },
  {
    $addToSet: {
      profileIds: {
        $each: ["system_access-external-param-profile"],
      },
    },
  }
);
print("End 10_update_external_param_profile.js");
