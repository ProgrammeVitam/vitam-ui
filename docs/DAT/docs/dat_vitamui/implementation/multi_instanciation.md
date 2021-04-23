
## Multi instanciation des micro services

### Multi instanciation

Les services vitamui multi instanciable à ce jour sont:
  - Service IAM Internal
  - Service IAM External
  - Service UI Identity
  - Service Portal
  - Service Referential Internal
  - Service Referential External
  - Service UI Referential
  - Service Ingest Internal
  - Service Ingest External
  - Service UI Ingest
  - Service Archive Search Internal
  - Service Archive Search External
  - Service UI Archive Search
  - Service Mongod (en cours de mise à niveau/!\\)
 
Un load balancer/reverse proxy (à défaut Consul) est installé et configuré pour la répartition de charge entre 
différentes instances (cette configurtion est en cours de réalisation).

La configuration de la mémoire des services est par défaut: Xms=512m et Xmx=512m.
cette configuration est modifiable, pour plus d'informations (cf: DEX).

### Mono instanciation

Le fameux service mono instanciable dans vitamui est le serveur CAS.

