# Architecture Vitam-UI

Dans ce fichier, nous expliquerons l'architecture de VITAM-UI en tant qu'application, ainsi que tous les modules et services utilisés par cette application.

## Les Services de VITAM-UI

* **IAM**: Un service qui permet de gérer les utilisateurs, les organisations et les droits au niveau d’accès en communiquant directement avec une couche de VITAM.
* **Ingest**: C’est le service qui permet de gérer les entrées et les versements (versement de SIP), il permet aussi de gérer la partie analyse sur chaque versement fait sur un tenant donné ⇒ affichage d’un dashboard bien détaillé.
* **Archive-search**: C’est le service qui permet de gérer les unités archivistiques.
* **Referential**: Un service qui permet de gérer les référentiels de VITAM (gestion complète),
* **Portal**: Service global pour accès aux applications Vitam-UI.
* **CAS-Server**: Projet qui permet de gérer la partie authentification, c’est un service nécessaire pour démarrer l’application de VITAM-UI.

## Les modules maven coté backend

* **Modules internes**: Contient 3 applications maven (iam-internal) : ils représentent la couche interne qui permet de communiquer directement avec la base de données de VITAM-UI (MongoDB) et aussi avec les couches externes de VITAM.
* **Modules externes**: Contient les applications (iam-external, ingest-external, archive-search-external et referential-external), ils représentent la couche externe qui permet de faire des vérifications des droits au niveau des différentes requêtes envoyées par les utilisateurs, et en se basant sur le profil de chaque utilisateur.

### Les Applications FrontEnd

* **Modules Front**: Services placés dans le projet (ui/ui-frontend/projects), c’est un multi-project Angular qui contient l’ensemble des applications Front de VITAM-UI (Applications : Portal, Identity, Ingest, Referential).
* **Projet vitamui-library**: Bibliothèque Angular utilisée par l’ensemble des applications Front de VITAM-UI.
