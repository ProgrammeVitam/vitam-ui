 
# Implémentation

## Technologies 

### Briques techniques 

La solution est développée principalement avec les briques technologies suivantes :

* Java 1.8+ (Java 11) 
* Angular 8 : framework front 
* Spring Boot 2 : framework applicatif
* MongoDB : base de données NoSQL
* Swagger : documentation API

### COTS

Les composants suivant sont utilisés dans la solution :

* CAS : gestionnaire d'authentification centralisé (IAM) 
* VITAM : socle d'archivage développé par le programme VITAM
* MongoDB : base de données orientée documents
* Curator : maintenance des index d’elasticsearch
* ELK : agrégation et traitement des logs et dashboards et recherche des logs techniques
* Consul : annuaire de services

Les solutions CAS et VITAM sont également développées en Java dans des technologies proches ou similaires. 

En fonction du choix de l'implémentation de la solution, il est possible de partager des dépendances logicielles avec la solution VITAM.

