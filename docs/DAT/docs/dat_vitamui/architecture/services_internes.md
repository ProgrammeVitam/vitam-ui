
## Services internes

Les services internes offrent des API REST accessibles en HTTPS uniquement depuis les services externes ou internes. Les API de ces services ne sont donc pas exposées publiquement. Les services internes implémentent les fonctionnalités de base de la solution ainsi que les fonctionnalités métiers. En fonction des besoins, les services internes peuvent être amenés à journaliser des évènements dans le logbook des opérations du socle VITAM.   

Les utilisateurs sont identifiés dans les services internes grâce au token transmis dans les headers des requêtes HTTPS. L'utililisation du protocole HTTPS permet de chiffrer les tokens et les informations sensibles qui sont transportées dans les requêtes. Les services internes peuvent éventuellement vérifier les droits d'accès de l'utilisateur avant d'accéder aux ressources. 

Les services internes s'auto-déclarent au démarrage dans l'annuaire de service Consul.

Les services disposent d'API REST pour suivre leur état et leur activité. 

* API Status pour connaitre la disponibilité du service (utilisé par Consul)
* API Health (basée sur SpringBoot) pour suivre l'activité du service

Les services génèrent les logs techniques dans la solution de log centralisée basée sur ELK. 

### Service iam-internal

* Description : service d’administration des clients, des utilisateurs et des profils, portail
* Contraintes
* API swagger
* Modèle de données

### Service security-internal

* Description : service de gestion de la sécurité applicative
* Contraintes
* API swagger
* Modèle de données

### Service referential-internal

* Description : service interne pour la gestion des référentiels de la solution logicielle VITAM.

Le service de référentiel interne reçoit les requtes du client référentiel externe, et communique avec VITAM via les clients Admin/Access pour la récupération des données.
Le service de référentiel interne est composé de plusieurs points d'APIs:
 - API des contrats d'accès (/referential/accesscontract)
 - API des contrats d'entrées (/referential/ingestcontract)
 - API des contrats de gestion (/referential/managementcontract)
 - API des services agents (/referential/agency)
 - API des formats (/referential/fileformat)
 - API des ontologies (/referential/ontology)
 - API des profils d'archivages (/referential/profile)
 - API des règles de gestion (/referential/profile)
 - API des profils de sécurité (/referential/security-profile)
 - API des contexts applicatifs (/referential/context)
 - API des opérations permettant le lancement différents audits (cohérence, valeur probante ...).
 
 pour plus d'information: voir la documentation des [référentiels](https://www.programmevitam.fr/pages/documentation/pour_archiviste/)

### Service ingest-internal

* Description : service interne pour la gestion des opérations d'entrées d'archives de la solution logicielle VITAM.

Le service d'ingest interne a pour responsabilité la réception, et la communication sécurisée avec les couches externes de VITAM.
Le service d'ingest interne est composé de plusieurs points d'APIs:
 - API de versement des archives permettant la consommation des flux d'archives (/v1/ingest/upload)
 - API de visualisation des journaux d'opération des opérations d'entrées (API /v1/ingest)
 - API de visualisation détaillé d'un journal d'une opération d'entrées (/v1/ingest/{id})
 - API permettant le téléchargement d'un rapport sous forme ODT d'une opération d'entrée (/v1/ingest/odtreport/{id})
 - API commune est utilisé pour le téléchargement du Manifest et de l'ATR (Archival Transfer Reply) d'une opération d'entrée. (Manifest: /logbooks/operations/{id}/download/manifest, ATR: /logbooks/operations/{id}/download/atr)
 
Ce service est configuré pour qu'il puisse communiquer avec la zone d'accès de la solution logicielle VITAM.
Pour aller plus loin: [1](https://www.programmevitam.fr/ressources/DocCourante/raml/externe/ingest.html), [2](https://www.programmevitam.fr/ressources/DocCourante/html/archi/archi-applicative/20-services-list.html#api-externes-ingest-external-et-access-external)

### Service archive-search-internal

* Description : service interne pour la gestion d'accès et la recherche d'archives de la solution logicielle VITAM.

Le service d'archive interne a pour responsabilité la réception, et la communication sécurisée avec les couches externes VITAM.
Le service d'archive interne est composé de plusieurs points d'APIs:
 - API de recherche des archive par requetes (/archive-search/search)
 - API de recherche des unités archivistiques (/archive-search/archiveunit/{id})
 - API de recherche des arbres de positionnement et plans de classement (/archive-search/filingholdingscheme)
 - API de téléchargement des objets (/archive-search/downloadobjectfromunit/{id})
 - API d'export des résultats sous format csv (/export-csv-search)
