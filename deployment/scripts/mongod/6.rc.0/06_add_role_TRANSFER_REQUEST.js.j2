print("START 6.rc.0/06_add_role_TRANSFER_REQUEST.js");

print("   add role TRANSFER_REQUEST to archive search UI context");
db = db.getSiblingDB('security')
db.contexts.updateOne({"_id":"ui_archive_search_context"},
{
$addToSet:{
    "roleNames":"ROLE_TRANSFER_REQUEST"
}
});

print("   add role TRANSFER_REQUEST to Archivist Admin roles");
db = db.getSiblingDB('iam');
db.profiles.updateMany({"name": {$regex : "Archiviste administrateur"} ,"applicationName": "ARCHIVE_SEARCH_MANAGEMENT_APP"},
{
$addToSet:{
    "roles":{"name":"ROLE_TRANSFER_REQUEST"}
},
$set: {
    "description": "Profil pour la recherche et consultation des archives dans Vitam sans mises à jour des règles de gestion, avec requette de transfert, export DIP et sans élimination"
}
});

print("END 6.rc.0/06_add_role_TRANSFER_REQUEST.js");
