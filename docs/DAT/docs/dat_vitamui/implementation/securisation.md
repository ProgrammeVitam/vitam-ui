
## Sécurisation

### Sécurisation des accès aux services externes

Les services exposants publiquement des API REST implémentent les mesures de sécurité suivantes :

* mise en place de filtres dans les applications IHM pour contrer les attaques de type CSRF et XSS

* utilisation du protocole HTTPS. Par défaut, la configuration suivante est appliquée (Protocoles exclus : TLS 1.0, TLS 1.1, SSLv2, SSLv3 & Ciphers exclus : .*NULL.*, .*RC4.*, .*MD5.*, .*DES.*, .*DSS.*)

* authentification par certificat X509 requise des applications externes (authentification M2M) basée sur une liste blanche de certificats valides

* mise à jour des droits utilisateurs grâce aux contextes applicatifs, associés certificats clients, stockés dans la collections XXX de base MongoDb gérée par le service SECURITY INTERNAL. 

* un service batch contrôle régulièrement l'expéritaion des certificats stockés dans le truststore des services et dans le référentiel de certificats clients (MongoDb) géré par le service SECURITY INTERNAL. 

### Sécurisation des communications internes

Les communications internes sont sécurisées par le protocole HTTPS. D’autre part, dans chaque requête, le header X-Auth-Token est positionné. Il contient le token initialisé par CAS à la connexion de l’utilisateur.

A chaque requête le service VITAMUI internal procède aux contrôles suivants :

* vérification de l'existence du header X-Auth-Token
* vérification de la validité (non expiré) du token extrait du header

En cas d’échec, la requête est refusée et la connexion est fermée.

### Sécurisation des accès aux bases de données

Les bases de données de MongoDB sont sécurisées via un cloisonnement physique (réseau) et/ou logique (compte utilisateur) des différentes bases de données qui les constituent. 

### Sécurisation des secrets de déploiement

Les secrets de l’intégralité de la solution VITAM déployée sont tous présents sur le serveur de déploiement ; par conséquent, ils doivent y être stockés de manière sécurisée, avec les principes suivants :

* Les mot de passe et token utilisés par ansible doivent être stockés dans des fichiers d’inventaire chiffrés par ansible-vault ;
* Les clés privées des certificats doivent être protégées par des mot de passe complexes et doivent suivre la règle précédente.

### Liste des secrets

Les secrets nécessaires au bon déploiement de VITAMUI sont les suivants :

* Certificat ou mot de passe de connexion SSH à un compte sudoers sur les serveurs cibles (pour le déploiement)

* Certificats x509 serveur (comprenant la clé privée) pour les modules de la zone d’accès (services *-external), ainsi que les CA (finales et intermédiaires) et CRL associées. Ces certificats seront déployés dans des keystores java en tant qu’élément de configuration de ces services

* Certificats x509 client pour les clients du SAE (ex: les applications métier, le service ihm-admin), ainsi que les CA (finales et intermédiaires) et CRL associées. Ces certificats seront déployés dans des keystores java en tant qu’élément de configuration de ces services 

Les secrets définis lors de l’installation de VITAM sont les suivants :

* Mots de passe des keystores ;
* Mots de passe des administrateurs fonctionnels de l’application VITAMUI 
* Mots de passe d’administration de base de données MongoDB ;
* Mots de passe des comptes d’accès aux bases de données MongoDB.

Note. Les secrets de VITAMUI sont différents de ceux VITAM

### Authentification du compte SSH

Il existe plusieurs méthodes envisageables pour authentifier le compte utilisateur utilisé pour la connexion SSH :
* par clé SSH avec passphrase
* par login/mot de passe
* par clé SSH sans passphrase

La méthode d’authentification retenue dépend de plusieurs paramètres :
* criticité des serveurs (services)
* zone de confiance
* technologie de déploiement

Dans un contexte sensible, il est fortement recommandé d'utiliser un bastion logiciel (par ex. https://www.wallix.com/bastion-privileged-access-management/) pour authentifier et tracer les actions des administrateurs du système.  

### Authentification des hôtes

Pour éviter les attaques de type MitM, le client SSH cherche à authentifier le serveur sur lequel il se connecte. Ceci se base généralement sur le stockage des clés publiques des serveurs auxquels il faut faire confiance (~/.ssh/known_hosts).

Il existe différentes méthodes pour remplir ce fichier (vérification humaine à la première connexion, gestion centralisée, DNSSEC). La gestion du fichier known_hosts  est un  pré-requis pour le lancement d’ansible.

### Elévation de privilèges

Plusieurs solutions sont envisageables :

* par sudo avec mot de passe
    * Au lancement de la commande ansible, le mot de passe sera demandé par sudo
* par su 
    * Au lancement de la commande ansible, le mot de passe root sera demandé
    * par sudo sans mot de passe