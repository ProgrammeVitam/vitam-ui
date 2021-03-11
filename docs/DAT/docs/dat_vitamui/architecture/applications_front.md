
## Applications Web

Les applications Web constituent les IHM de la solution. Elles sont accessibles depuis le portail de la solution. L'authentification d'un utilisateur dans une application cliente se fait par l'intermédiaire de l'IAM CAS. Une application cliente est constituée de 2 parties.

* Interface utilisateur Front (IHM WEB) qui donne accès aux fonctionnalités via un navigateur
* Interface utilisateur Back (Service BackOffice) qui gère la communication avec CAS et les accès aux API externes

Une double authentification est nécessaire pour qu’un utilisateur puissent accéder aux API externes :

* Le service UI Back de l’application cliente doit posséder un certificat reconnu par la solution
* L’utilisateur de l’application cliente doit être authentifié dans la solution (par CAS) et posséder un token valide

Les applications de base :

* portal : application portail donnant accès aux applications
* identity : application pour gérer les organisations, utilisateurs, profils, etc.
