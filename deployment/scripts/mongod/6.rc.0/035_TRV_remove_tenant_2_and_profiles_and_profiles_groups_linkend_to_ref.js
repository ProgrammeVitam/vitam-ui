print("START 035_TRV_remove_tenant_2_and_profiles_and_profiles_groups_linkend_to_ref.js");

db = db.getSiblingDB("iam");

// Remove all profiles on tenants 2
// Remove profiles from groups
db.profiles
  .find({
    tenantIdentifier: NumberInt(2),
  })
  .forEach(function (profile) {
    db.groups.updateMany(
      { profileIds: profile._id },

      {
        $pull: { profileIds: profile._id },
      }
    );
  });

// Remove profiles
db.profiles.deleteMany({
  tenantIdentifier: NumberInt(2),
});
print("END 035_TRV_remove_tenant_2_and_profiles_and_profiles_groups_linkend_to_ref.js");
