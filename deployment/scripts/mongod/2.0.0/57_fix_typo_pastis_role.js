print("START 57_fix_typo_pastis_role.js");

dbIam = db.getSiblingDB('{{ mongodb.iam.db | default('iam') }}')

dbIam.profiles.updateMany(
  { "roles.name": "ROL_GET_PASTIS" },
  { $set: { "roles.$[elem].name": "ROLE_GET_PASTIS" } },
  {
    arrayFilters: [
      { "elem.name": "ROL_GET_PASTIS" }
    ]
  }
)

print("END 57_fix_typo_pastis_role.js");
