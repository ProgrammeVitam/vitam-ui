
## Services externes

Les services externes exposent des API REST publiques accessibles en HTTPS. Ces services constituent une porte d'accès aux services internes et assurent principalement un rôle de sécurisation des ressources internes.  
 
 La connexion d'une application cliente à un service externe nécessite le partage de certificats X509 client et serveur dans le cadre d'un processus d'authentification mutuel (Machine To Machine/M2M). Dans la solution VITAMUI, les certificats des clients sont associés à un contexte de sécurité stocké dans une collection MongoDb gérée par le service security_internal. D'autre part, les utilisateurs clients sont identifiés et authentifiés dans les services externes par le token fourni par CAS et transmis dans les headers des requêtes REST en HTTPS.  
 
 Le service externe a pour responsabilité de sécuriser les accès en effectuant les différentes étapes de vérifications des droits (générale, tenant, rôles, groupes, etc.) et de déterminer les droits résultants du client à l'origine de la requête, en réalisant l'intersection des droits applicatifs, définis dans le contexte de sécurité, avec les droits issus des profils de l'utilisateur. Le service externe s'assure ensuite que le client possède bien les droits pour accéder à la ressource demandée.  

Les services externes s'auto-déclarent au démarrage dans l'annuaire de service Consul.  

Les services disposent d'API REST pour suivre leur état et leur activité. Ces API ne sont pas accessibles publiquement.  

* API Status pour connaitre la disponibilité du service (utilisé par Consul)
* API Health (basée sur SpringBoot) pour suivre l'activité

Les services génèrent les logs techniques dans la solution de log centralisée basée sur ELK. 

### Service iam-external

* Description : service externe pour la gestion des organisations, utilisateurs, profils, etc.
* Contraintes
* API swagger

### Service cas-server

* Description : service d’authentification nécessaire et accessible uniquement par l'IAM CAS
* Contraintes
* API swagger

### Service referential-external

* Description : service externe pour la gestion des référentiels de la solution logicielle VITAM.
* Contraintes
* API swagger

### Service ingest-external

* Description : service externe pour la gestion des opérations d'entrées d'archives de la solution logicielle VITAM.
* Contraintes
* API swagger

### Service archive-search-external

* Description : service externe pour la gestion d'accès et la recherche d'archives de la solution logicielle VITAM.
* Contraintes
* API swagger
