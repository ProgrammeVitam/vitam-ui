db = db.getSiblingDB('iam');

print("START 08_update_archive_administrator_profiles_to_add_transfer_acknowledgment_role.js");

db.profiles.updateMany({
      "name": {$regex : "Archiviste administrateur"} ,
      "applicationName": "ARCHIVE_SEARCH_MANAGEMENT_APP"},
{
$addToSet:{
    "roles":{
         "name":"ROLE_TRANSFER_ACKNOWLEDGMENT"
         }
},
$set: {
    "description": "Profil pour la recherche et consultation des archives dans Vitam avec mise à jour des règles, export DIP, opérations d'élimination, reclassement, demande de transfert et acquittement de transfert"
}
});

print("END 08_update_archive_administrator_profiles_to_add_transfer_acknowledgment_role.js");





