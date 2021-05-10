db = db.getSiblingDB('iam')

print("START 01_TRTL-952_update_super_admin_group_name_and_description_ref.js");

db.groups.update({
  "_id": "super_admin_group",
}, {
  $set: {
    "name": "Groupe de l'administrateur de l'instance",
    "description": "Groupe de l'administrateur de l'instance",
  },
});
print("END 01_TRTL-952_update_super_admin_group_name_and_description_ref.js");
