##Application avec plusieurs profils
Au niveau de chaque application on a la possibilité d'avoir plusieurs profils, pour distinguer entre les utilisateurs qui ont droits à certaines fonctionnalitées et d'autres qui n'ont pas les droits. <br/>
L'objectif du document est de présenter les deux profils créés pour l'application Archive-Search.
- Un profil "****Profil pour la recherche et consultation des archives avec mises à jour des règles****" : <br/>
  droit d'accès à l'ensemble des fonctionnalités de recherche des règles en plus des fonctionnalités actuelles. 
  Par défaut, l'administrateur ADMIN_ROOT créé à l'initialisation de l'organisation possède ce profil pour cette APP. <br/>
- Un profil "****Profil pour la recherche et consultation des archives sans mises à jour des règles****" : <br/>
  aucun droit d'accès sur les fonctionnalités de recherche des règles

### Procèdure d'implémentation 
Pour l'implémentation de cette évolution il faut ajouter un deuxième profil au niveau de la BD avec une liste des roles specifiques et d'autres informations qui concernent ce profil.
Après il faut modifier le fichier customer-init.yml en ajoutant le deuxième profil créé.
Au niveau Angular on peut utiliser la directive : **HasRoleDirective** prend comme entrée un objet qui contient 3 informations : 
- L'Id de l'application.
- L'identifiant du tenant (de type number).
- Le role en question (une liste des roles est initialisée au niveau de l'enum ***VitamuiRoles***).

- Example pour afficher un composant pour les users qui ont le role ROLE_SEARCH_WITH_RULES <br/>
  `<ng-template [vitamuiCommonHasRole]="dataToSearchWithRules">`
  `<button> <div class="btn-primary">click search with rules</div>
  </button></ng-template>`
  
Au niveau du fichier ts :
`dataToSearchWithRules = {
appId: "ApplicationId",
tenantIdentifier: 1,
role: 'ROLE_SEARCH_WITH_RULES',
};`

