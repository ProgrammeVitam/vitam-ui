# Architecture VITAMUI
Dans ce fichier, nous expliquerons l'architecture de VITAM-UI en tant qu'application, ainsi que tous les modules et services utilisés par cette application.

### Les Services de VITAM-UI
- **IAM :**  <br/> Un service qui permet de gérer les utilisateurs, les organisations et les droits au niveau d’accès en communiquant directement avec une couche de VITAM.
- **Ingest :**  <br/> C’est le service qui permet de gérer les entrées et les versements (versement de SIP), il permet aussi de gérer la partie analyse sur chaque versement fait sur un tenant donné ⇒ affichage d’un dashboard bien détaillé.
- **Archive-search :**  <br/> C’est le service qui permet de gérer les unités archivistiques.
- **Référentiel :**  <br/>  Un service qui permet de gérer les référentiels de VITAM (gestion complète),
- **Portal :**  <br/> Service global pour accès aux applications Vitam-UI.
- **Service CAS :**  <br/>  Projet qui permet de gérer la partie authentification, c’est un service nécessaire pour démarrer l’application de VITAM-UI.


### Les modules maven coté backend :
- **Modules internes :**  <br/>Continent 3 applications maven (iam-internal, ingest-internal, archive-search-internal et referential-internal) : ils représentent la couche interne qui permet de communiquer directement avec la base de données de VITAM-UI (MongoDB) et aussi avec les couches externes de VITAM.
- **Modules externes :**  <br/>Contient les applications (iam-external, ingest-external, archive-search-external et referential-external), ils représentent la couche externe qui permet de faire des vérifications des droits au niveau des différentes requêtes envoyées par les utilisateurs, et en se basant sur le profil de chaque utilisateur.
- **Les modules UI :**  <br/>Content les applications (ui-identity, ui-portal, ui-ingest, ui-archive-search, ui-referential et ui-commons), ils sont des services servant les applications Front et aussi les API vers la couche externe de VITAM-UI, tout cela en utilisant toujours par ui-commons.


### Les Applications FrontEnd :

- **Modules Front :**  <br/>Ces services sont placés dans le projet (ui/ui-frontend/projects), c’est un multi-project Angular qui contient l’ensemble des applications Front de VITAM-UI (Applications : Portal, Identity, Ingest, Referential).
- **Projet ui-frontend-common :**  <br/> C’est la bibliothèque Angular utilisé par l’ensemble des applications Front de VITAM-UI. En local, il peut être packagé en tar pour mettre à jour ce dernière en dépendances du ui-frontend. Sinon, il est publié sur Nexus.
