
## Services internes

Les services internes offrent des API REST accessibles en HTTPS uniquement depuis les services externes ou internes. Les API de ces services ne sont donc pas exposées publiquement. Les services internes implémentent les fonctionnalités de base de la solution ainsi que les fonctionnalités métiers. En fonction des besoins, les services internes peuvent être amenés à journaliser des évènements dans le logbook des opérations du socle VITAM.   

Les utilisateurs sont identifiés dans les services internes grâce au token transmis dans les headers des requêtes HTTPS. L'utililisation du protocole HTTPS permet de chiffrer les tokens et les informations sensibles qui sont transportées dans les requêtes. Les services internes peut éventuellement vérifier les droits d'accès de l'utilisateur avant d'accéder aux ressources. 

Les services internes s'auto-déclarent au démarrage dans l'annuaire de service Consul.

Les services disposent d'API REST pour suivre leur état et leur activité. 

* API Status pour connaitre la disponibilité du service (utilisé par Consul)
* API Health (basée sur SpringBoot) pour suivre l'activité du service

Les services génèrent les logs techniques dans la solution de log centralisée basée sur ELK. 

### Service identity-internal

* Description : service d’administration des clients, des utilisateurs et des profils, portail
* Contraintes
* API swagger
* Modèle de données

### Service security-internal

* Description : service de gestion de la sécurité applicative
* Contraintes
* API swagger
* Modèle de données