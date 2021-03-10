
## Détail des COTS

### CAS

* Description  
* Contraintes  

### Annuaire de services Consul

* Description  
* Contraintes  

La découverte des services est réalisée avec Consul via l’utilisation du protocole DNS. Le service DNS configuré lors du déploiement doit pouvoir résoudre les noms DNS associés à la fois aux service_id et aux instance_id. Tout hôte portant un service VITAMUI doit utiliser ce service DNS par défaut. L’installation et la configuration du service DNS applicatif sont intégrées à VITAMUI.

La résilience est assurée par l’annuaire de service Consul. Il est partagé avec VITAM.
* Les services sont enregistrés au démarrage dans Consul
* Les clients utilisent Consul (mode DNS) pour localiser les services 
* Consul effectue régulièrement des health checks sur les services enregistrés. Ces informations sont utilisées pour router les demandes des clients sur les services actifs

La solution de DNS applicatif intégrée à VITAMUI et VITAM est présentée plus en détails dans la section dédiée à Consul dans la documentation VITAM. 

### Base NOSQL MongoDB

* Description  
* Contraintes  


