db = db.getSiblingDB('iam');

print("START 0-03_update_archive_administrator_profiles_description.js.j2");

db.profiles.updateMany({
      "name": {$regex : "Archiviste administrateur"} ,
      "applicationName": "ARCHIVE_SEARCH_MANAGEMENT_APP"},
{
$set: {
    "description": "Profil ayant tous les droits de recherche et consultation des archives dont la modification, l'élimination, le transfert et la réattribution"
}
});

print("END 0-03_update_archive_administrator_profiles_description.js.j2");
