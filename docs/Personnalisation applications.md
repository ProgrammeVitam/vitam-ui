# Personnalisation d'applications

## Suppression d'application

Pour la suppression d'une application d'un utilisateur donné, nous avons 2 situations : 

1) L'application est une application simple, et elle est n'est pas référencée dans d'autres services.
2) L'application est référencée par d'autres applications.

* #### Procédure de suppression en cas d'application simple :

1) Identifier le profil lié à l'application à supprimer, dans la bd iam, la liste des profils (collection 'profiles') ayant le champ applicationName égal à l'application en question, .
2) Pour l'ensemble des groupes de profils (collections 'groups'), enlever de l'attribut 'profileIds' les profils de l'application en question (identifiés précédement).
3) S'il y a d'autres profils qui ont des roles liés à cette application, enlever de ces listes les roles en question.
4) Supprimer le document lié à l'application de la collection "applications"
5) Au niveau des instances, dans le fichier de configuration (conf/iam-internal/customer-init.yml), on supprime la rubrique liée à l'application en question.
6) Redémarrer le service iam-internal
7) Dans la collection security/contexts : enlever le role lié à l'application des contextes.
8) Dans chaque utilisateur (collection users), enlever l'application en question de la liste des applications récemment utilisées (l'attribut : analytics -> applications) 

* #### Procédure de suppression en cas d'application référencée par d'autres applications :

1) Identifier le profil lié à l'application à supprimer, dans la bd iam, la liste des profils (collection 'profiles') ayant le champ applicationName égal à l'application en question, .
2) Pour l'ensemble des groupes de profils (collections 'groups'), enlever de l'attribut 'profileIds' les profils de l'application en question (identifiés précédement).
3) Au niveau des instances, dans le fichier de configuration (conf/iam-internal/customer-init.yml), on supprime la rubrique liée à l'application en question.
4) Redémarrer le service iam-internal
5) Dans chaque utilisateur (collection users), enlever l'application en question de la liste des applications récemment utilisées (l'attribut : analytics -> applications) 
