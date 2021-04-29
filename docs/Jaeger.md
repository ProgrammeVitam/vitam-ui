#Introduction
#OpenTracing :
Une méthode permettant de monitorer des applications dans un contexte micro-services, elle permet d'analyser les erreurs ou les problèmes de performances.

#Jager : 
est un système open source de "Open Tracing", ça inclut :
 1. Surveillance distribuée des transactions
 2. Optimisation des performances et de la latence
 3. Analyse des causes originelles des anomalies
 4. Analyse de la dépendance des services
 5. Propagation de contexte distribué
 
#Principaux composants de Jaeger
## Jaeger Client Libraries:
Les clients Jaeger sont des implémentations spécifiques du langage de l'API OpenTracing.

##Agent
L'agent Jaeger est un agent réseau "daemon" qui écoute les "spans" envoyés via UDP, il les regroupe et envoie au collecteur. Il est conçu pour être déployé sur tous les hôtes en tant que composant d'infrastructure. L'agent fait abstraction du routage et de la découverte des collecteurs du client.


##Collector
Le collecteur Jaeger reçoit des traces des agents Jaeger et les fait passer par un processus de traitements. 
Actuellement, les étapes sont : valider les traces, les indexer, effectuer des transformations et, enfin, les stocker. 
Le stockage de Jaeger est un composant pluggable qui supporte actuellement en charge Cassandra, Elasticsearch et Kafka.

##Query
La requête est un service qui récupère les traces du stockage et héberge une interface utilisateur pour les afficher.

##Ingester
Ingester est un service qui lit à partir de la rubrique Kafka et écrit dans un autre backend de stockage (Cassandra, Elasticsearch).


#Tests en local :
Pour pouvoir tester le fonctionnement de Jaeger en local, un docker compose a été fait dans ../tools/docker/jaeger/jaeger-docker-compose.yml , puis lancer dans le navigateur : http://localhost:8090
    
