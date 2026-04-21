# Implémentation

## Technologies

### Briques techniques

La solution est développée principalement avec les briques technologies suivantes :

* Java 21
* Angular 19 : framework front
* Spring Boot 3 : framework applicatif
* MongoDB : base de données NoSQL
* Swagger : documentation API

### COTS

Les composants suivant sont utilisés dans la solution :

* CAS : gestionnaire d'authentification centralisé (IAM)
* VITAM : socle d'archivage développé par le programme VITAM
* MongoDB : base de données orientée documents
* ELK : agrégation et traitement des logs et dashboards et recherche des logs techniques
* Consul : annuaire de services

Les solutions CAS et VITAM sont également développées en Java dans des technologies proches ou similaires.

En fonction du choix de l'implémentation de la solution, il est possible de partager des dépendances logicielles avec la solution VITAM.

---

## Services

La solution est bâtie selon une architecture de type micro-services. Ces services communiquent entre eux en HTTPS via des API REST.

* Les services externes exposés publiquement sont sécurisés par la mise en oeuvre d'un protocole M2M nécessitant l'utilisation de certificats X509 client et serveur reconnus mutuellement lors de la connexion.

* Les services internes, ne sont jamais exposés publiquement. Ils sont accessibles uniquement en HTTPS par les services externes ou par d'autres services internes.

* Les accès aux bases de données MongoDb ou aux socles techniques externes (ie. VITAM) se font uniquement via les services internes.

* Les utilisateurs sont authentifiés via CAS et disposent d'un token, validé à chaque appel, qui les identifient durant toute la chaîne de traitement des requêtes.

### Identification des services

Il est primordial que chaque service de la solution puisse être identifié de manière unique sur le système. À cet effet, les services disposent des différents identifiants suivant :

* ID de service (ou service_id) : c’est une chaîne de caractères qui nomme de manière unique un service. Cette chaîne de caractère doit respecter l’expression régulière suivante : `[a-z][a-z-]*`. Chaque cluster de service possède un ID unique de service.

* ID d’instance (ou instance_id) : c’est l’ID d’un service instancié dans un environnement ; ainsi, pour un même service, il peut exister plusieurs instances de manière concurrente dans un environnement donné. Cet ID a la forme suivante : `<service_id>-<instance_number>`, avec `<instance_number>` respectant l’expression régulière suivante : `[0-9]{2}`. Chaque instance dans ce cluster possède un id d’instance (instance_id).

* ID de package (ou package_id) : il est de la forme `vitamui-<service_id>`. C’est le nom du package à déployer.

### Communications inter-services

Les services VITAMUI suivent les principes suivants lors d’un appel entre deux composants :

1. Le composant amont effectue un appel (de type DNS) à l’annuaire de service en indiquant le service_id du service qu’il souhaite appeler

2. L’annuaire de service lui retourne une liste ordonnée d’instance_id. C’est de la responsabilité de l’annuaire de service de trier cette liste dans l’ordre préférentiel d’appel (en fonction de l’état des différents services, et avec un algorithme d’équilibrage dont il a la charge)

3. Le composant amont appelle la première instance présente dans la liste. En cas d’échec de cet appel, il recommence depuis le point 1. La communication vers une instance cible de type Service API utilise nécessairement le protocole sécurisé HTTPS.

Ces principes ont pour but de garantir les trois points suivants :

* Les clients des services doivent être agnostiques de la topologie de déploiement, et notamment du nombre d’instances de chaque service dans chaque cluster. La connaissance de cette topologie est déléguée à l’annuaire de service.

* Le choix de l’instance cible d’un appel doit être décorrélé de l’appel effectif afin d’optimiser les performances et la résilience.

* La garantie de la confidentialité des informations transmises entre les services (hors COTS)

Dans le cas des COTS, la gestion de l’équilibrage de charge et de la haute disponibilité doit être intégrée de manière native dans le COTS utilisé. D'autre part, la sécurisation de la transmission dépend du COTS. Dans le cas où le chiffrement des données transmises n'est pas assuré, il est alors recommandé d'isoler le COTS dans une zone réseau spécifique.

### Cloisonnement des services

Le cloisonnement applicatif permet de séparer les services de manière physique (subnet/port) et ainsi limiter la portée d’une attaque en cas d’intrusion dans une des zones. Ce cloisonnement applique le principe de défense en profondeur préconisé par l’ANSSI.

Chaque zone héberge des clusters de services. Un cluster doit être présent en entier dans une zone, et ne peut par conséquent pas être réparti dans deux zones différentes. Chaque noeud d’un cluster applicatif doit être installé sur un hôte (OS) distinct (la colocalisation de deux instances d’un même service n’étant pas supporté). La mise en oeuvre d’une infrastructure virtualisée impose de placer deux noeuds d’un même cluster applicatif sur deux serveurs physiques différents.

Un exemple de découpage en zones applicative est fourni ci-dessous. Ce découpage repose sur une logique assez classique adapté à une infrastructure de type VmWare ESX. Pour une architecture reposant sur une technologie de type Docker, il serait envisageable de découper plus finement les zones jusqu'à envisager une zone pour chaque cluster de service.

Dans cet exemple, il est prévu pour respecter les contraintes de flux inter-zones suivants :

* les utilisateurs de la zone USERS communiquent avec les services de la zone IHM
* les administrateurs de la zone ADMIN communiquent avec les services de la zone IHM ADMIN
* les services de la zone IHM et IHM-ADMIN communiquent avec les services de la zone API
* les services de la zone API communiquent avec les services de la zone DATA
* les services de toutes les zones communiquent avec les services déployés dans la zone INFRA
* les exploitants techniques accédent aux services de la zone EXPLOITATION pour intervenir dans toutes les zones

#### Les différentes zones

##### Zone IHM

La zone IHM se compose de plusieurs services:

* UI Identity
* UI Portal
* UI Referential
* UI Ingest
* UI Archive Search
* UI Collect
* UI Pastis

##### Zone API

La zone API se compose de plusieurs services:

* IAM
* SECURITY
* API GATEWAY
* REFERENTIAL
* INGEST
* ARCHIVE SEARCH
* COLLECT
* PASTIS

##### Zone DATA

La zone stockage: MongoDB

##### Zone INFRA

Les services consul, kibana, elk etc..

Tous les serveurs cibles doivent avoir accès aux dépôts de binaires contenant les paquets des logiciels VITAMUI et des composants externes requis pour l’installation. Les autres éléments d’installation (playbook ansible, ...) doivent être disponibles sur la machine ansible orchestrant le déploiement de la solution dans la zone INFRA.

Schéma de zoning :

![Architecture IAM CAS](../images/dat_zoning.png)

---

## Intégration système

### Utilisateurs et groupes d’exécution

La segmentation des droits utilisateurs permet de respecter les contraintes suivantes :

* Assurer une séparation des utilisateurs humains du système et des utilisateurs système sous lesquels tournent les processus
* Séparer les droits des rôles d’exploitation différents suivants :
  * Les administrateurs système (OS)
  * Les administrateurs techniques des logiciels VitamUI
  * Les administrateurs des bases de données VitamUI

Les utilisateurs et groupes décrits dans les paragraphes suivants doivent être ajoutés par les scripts d’installation de la solution VitamUI. En outre, les règles de sudoer associées aux groupes vitamui*-admin doivent également être mis en place par les scripts d’installation.

Les sudoers sont paramétrés en mode NOPASSWD, c’est à dire qu’aucun mot de passe n’est demandé à l’utilisateur faisant partie du groupe vitamui*-admin pour lancer les commandes d’arrêt relance des applicatifs vitamui.

Les fichiers de règles sudoers des groupes vitamui-admin et vitamuidb-admin sont systématiquement écrasés à chaque installation des paquets (rpm) déclarant les utilisateurs VitamUI (Un backup de l’ancien fichier est cependant effectué).

#### Utilisateurs

Les utilisateurs suivant sont définis :

* vitamui : user pour les services ne stockant pas les données
* vitamuidb : user pour les services stockant des données (Ex : MongoDB)

Les processus VitamUI tournent sous ces utilisateurs. Leurs logins sont désactivés.

#### Groupes

Les groupes suivant sont définis :

* vitamui : groupe primaire des utilisateurs de service
* vitamui-admin : groupe d’utilisateurs ayant les droits “sudo” permettant le lancement des services VITAMUI
* vitamuidb-admin : groupe d’utilisateurs ayant les droits “sudo” permettant le lancement des services VITAMUI stockant de la donnée.

### Arborescence de fichiers

L’arborescence /vitamui héberge les fichiers propres aux différents services. Elle est normalisée selon le pattern suivant : /vitamui/<folder_type>/<service_id> où :

Pour un service d’id service_id, les fichiers et dossiers impactés par VITAMUI sont les suivants.

* service_id est l’id du service auquel appartient les fichiers
* folder-type est le type de fichiers contenu par le dossier :
  * app : fichiers de ressources (non-jar) requis pour l’application (ex: .war)
  * bin : binaires (le cas échéant)
  * script : Répertoire des scripts d’exploitation du module (start/stop/status/backup)
  * conf : Fichiers de configuration
  * lib : Fichiers binaires (ex: jar)
  * log : Logs du composant
  * data : Données sauvegardes du composant
  * tmp : Données temporaires produites par l’application

Les dossiers /vitamui et /vitamui/<folder_type> ont les droits suivants :

* Owner : root
* Group owner : root
* Droits : 0555

À l’intérieur de ces dossiers, les droits par défaut sont les suivants :

* Fichiers standards :
  * Owner : vitamui (ou vitamuidb)
  * Group owner : vitamui
  * Droits : 0440

* Fichiers exécutables et répertoires :
  * Owner : vitamui (ou vitamuidb)
  * Group owner : vitamui
  * Droits : 0750

Cette arborescence ne doit pas contenir de caractère spécial. Les éléments du chemin (notamment le service_id) doivent respecter l’expression régulière suivante : `[0-9A-Za-z-_]+`

Le système de déploiement et de gestion de configuration de la solution est responsable de la bonne définition de cette arborescence (tant dans sa structure que dans les droits utilisateurs associés).

### Intégration au service d'initialisation Systemd

L’intégration est réalisée par l’utilisation du système d’initialisation systemd. La configuration se fait de la manière suivante :

* /usr/lib/systemd/system/ : répertoire racine des définitions de units systemd de type “service”
* <service_id>.service : fichier de définition du service systemd associé au service VITAMUI

Les COTS utilisent la même nomenclature de répertoires et utilisateurs que les services VITAMUI, à l’exception des fichiers binaires et bibliothèques qui utilisent les dossiers de l’installation du paquet natif.

---

## Sécurisation

### Sécurisation des accès aux services externes

Les services exposants publiquement des API REST implémentent les mesures de sécurité suivantes :

* mise en place de filtres dans les applications IHM pour contrer les attaques de type CSRF et XSS

* utilisation du protocole HTTPS. Par défaut, la configuration suivante est appliquée (Protocoles exclus : TLS 1.0, TLS1.1, SSLv2, SSLv3 & Ciphers exclus : .*NULL.*, .*RC4.*, .*MD5.*, .*DES.*, .*DSS.*)

* authentification par certificat X509 requise des applications externes (authentification M2M) basée sur une liste blanche de certificats valides

* mise à jour des droits utilisateurs grâce aux contextes applicatifs, associés certificats clients, stockés dans la collections XXX de base MongoDB gérée par le service SECURITY INTERNAL.

* un service batch contrôle régulièrement l'expiration des certificats stockés dans le truststore des services et dans le référentiel de certificats clients (MongoDB) géré par le service SECURITY INTERNAL.

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

Les secrets nécessaires au bon déploiement de VitamUI sont les suivants :

* Certificat ou mot de passe de connexion SSH à un compte sudoers sur les serveurs cibles (pour le déploiement)

* Certificats x509 serveur (comprenant la clé privée) pour les modules de la zone d’accès (services *-external), ainsi que les CA (finales et intermédiaires) et CRL associées. Ces certificats seront déployés dans des keystores java en tant qu’élément de configuration de ces services

* Certificats x509 client pour les clients du SAE (ex: les applications métier, le service ihm-admin), ainsi que les CA (finales et intermédiaires) et CRL associées. Ces certificats seront déployés dans des keystores java en tant qu’élément de configuration de ces services

Les secrets définis lors de l’installation de Vitam sont les suivants :

* Mots de passe des keystores ;
* Mots de passe des administrateurs fonctionnels de l’application VITAMUI
* Mots de passe d’administration de base de données MongoDB ;
* Mots de passe des comptes d’accès aux bases de données MongoDB.

> Note: Les secrets de VitamUI sont différents de ceux Vitam

### Authentification du compte SSH

Il existe plusieurs méthodes envisageables pour authentifier le compte utilisateur utilisé pour la connexion SSH :

* par clé SSH avec passphrase
* par login/mot de passe
* par clé SSH sans passphrase

La méthode d’authentification retenue dépend de plusieurs paramètres :

* criticité des serveurs (services)
* zone de confiance
* technologie de déploiement

Dans un contexte sensible, il est fortement recommandé d'utiliser un bastion logiciel (par ex. <https://www.wallix.com/bastion-privileged-access-management/>) pour authentifier et tracer les actions des administrateurs du système.

### Authentification des hôtes

Pour éviter les attaques de type MitM, le client SSH cherche à authentifier le serveur sur lequel il se connecte. Ceci se base généralement sur le stockage des clés publiques des serveurs auxquels il faut faire confiance (~/.ssh/known_hosts).

Il existe différentes méthodes pour remplir ce fichier (vérification humaine à la première connexion, gestion centralisée, DNSSEC). La gestion du fichier known_hosts est un pré-requis pour le lancement d’ansible.

### Élévation de privilèges

Plusieurs solutions sont envisageables :

* par sudo avec mot de passe
  * Au lancement de la commande ansible, le mot de passe sera demandé par sudo
  * par su
  * Au lancement de la commande ansible, le mot de passe root sera demandé
  * par sudo sans mot de passe

---

## Certificats et PKI

La PKI (Public Key Infrastructure) permet de gérer de manière robuste les certificats de la solution VITAMUI. C'est une architecture de confiance qui encadre l'authentification des entités, la sécurisation des échanges et qui gère le cycle de vie des certificats numériques (émission, déploiement et révocation).

### Architecture et Schéma de communication

Avant d'aborder la génération et la structure de la PKI, il est essentiel de comprendre comment les différents composants de VITAMUI interagissent entre eux, ainsi qu'avec le backend VITAM. L'authentification mutuelle (mTLS) garantit le cloisonnement applicatif et la sécurisation des flux privilégiés.

Voici le schéma de communication global illustrant cette architecture de confiance :

![Certificate Communication Diagram ](../images/dat_certificate_communication_diagram.png)

Comme l'illustre ce schéma :

* **Accès externe** : Les utilisateurs et systèmes accèdent aux services Web via un Reverse Proxy (RP).
* **IHM (VitamUI-UI)** : Le composant RP transmet les flux applicatifs vers les services UI (en http).
* **Passerelle (API Gateway)** : Les interfaces IHM communiquent avec le point d'entrée central des APIs internes (`api-gateway`) au travers d'une connexion obligatoirement en `mTLS` (authentification mutuelle via certificats chiffrés).
* **Services Internes (VitamUI Services)** : L'api-gateway retransmet les requêtes aux services internes spécifiques en transférant les informations d'identité initiales (`x-ssl-cert`) de manière sécurisée en HTTPS simple.
* **Sécurité & IAM** : Les différents services communiquent avec l'IAM en `mTLS` (ex: pour instancier des sessions applicatives). IAM lui même communique en mTLS avec le CAS.

### Principes et Cycle de vie

La logique de fonctionnement des PKI et les types de scripts d'infrastructures utilisés dans VITAMUI sont très similaires à ceux de la solution VITAM.

1. **Émission** : Les dates de création et de fin de validité des CAs sont générées dans cette phase afin d'être partagées à l'ensemble des acteurs.
2. **Déploiement** : Génération des répertoires de magasins par composant (fichiers `.p12`) afin de consolider la correspondance clé/certificat pour le déploiement sur les instances applicatives via Ansible.
3. **Gestion** : Suivi fin de publication et de révocation des identités.

Schéma des principes d'administration :

![PKI](../images/dat_pki_1.png)

(Pour retrouver des détails encore plus bas et structurants de la base : [Documentation d'intégration de PKI Vitam](http://www.programmevitam.fr/ressources/DocCourante/html/installation/annexes/10-overview_certificats.html)).

---

### Arborescence et Zones de Confiance

L'arborescence des certificats générée par les scripts PKI dans l'environnement est partitionnée pour structurer très distinctement le rôle des différents types d'identité et de certificats existants :

```text
environments/certs/
├── client-vitam
│   ├── ca
│   │   ├── ca-intermediate.crt
│   │   ├── ca-root.crt
│   │   ├── vitam_ca-intermediate.crt
│   │   └── vitam_ca-root.crt
│   └── clients
│       └── vitamui
│           ├── vitamui.crt
│           └── vitamui.key
├── vault-ca.yml
├── vault-certs.yml
└── vitamui-services
    ├── ca
    │   ├── ca-intermediate.crt
    │   └── ca-root.crt
    ├── clients
    │   ├── api-gateway
    │   │   ├── api-gateway.crt
    │   │   └── api-gateway.key
    │   ├── archive-search
    │   │   ├── archive-search.crt
    │   │   └── archive-search.key
    │   ├── cas-server
    │   │   ├── cas-server.crt
    │   │   ├── cas-server.key
    │   │   └── cas-server.pem
    │   ├── collect
    │   │   ├── collect.crt
    │   │   └── collect.key
    │   ├── iam
    │   │   ├── iam.crt
    │   │   └── iam.key
    │   ├── ingest
    │   │   ├── ingest.crt
    │   │   └── ingest.key
    │   ├── pastis
    │   │   ├── pastis.crt
    │   │   └── pastis.key
    │   ├── referential
    │   │   ├── referential.crt
    │   │   └── referential.key
    │   ├── ui-archive-search
    │   │   ├── ui-archive-search.crt
    │   │   ├── ui-archive-search.key
    │   │   └── ui-archive-search.pem
    │   ├── ui-collect
    │   │   ├── ui-collect.crt
    │   │   ├── ui-collect.key
    │   │   └── ui-collect.pem
    │   ├── ui-identity
    │   │   ├── ui-identity.crt
    │   │   ├── ui-identity.key
    │   │   └── ui-identity.pem
    │   ├── ui-identity-admin
    │   │   ├── ui-identity-admin.crt
    │   │   ├── ui-identity-admin.key
    │   │   └── ui-identity-admin.pem
    │   ├── ui-ingest
    │   │   ├── ui-ingest.crt
    │   │   ├── ui-ingest.key
    │   │   └── ui-ingest.pem
    │   ├── ui-pastis
    │   │   ├── ui-pastis.crt
    │   │   ├── ui-pastis.key
    │   │   └── ui-pastis.pem
    │   ├── ui-portal
    │   │   ├── ui-portal.crt
    │   │   ├── ui-portal.key
    │   │   └── ui-portal.pem
    │   └── ui-referential
    │       ├── ui-referential.crt
    │       ├── ui-referential.key
    │       └── ui-referential.pem
    └── servers
        ├── api-gateway
        │   ├── api-gateway.crt
        │   └── api-gateway.key
        ├── archive-search
        │   ├── archive-search.crt
        │   └── archive-search.key
        ├── cas-server
        │   ├── cas-server.crt
        │   └── cas-server.key
        ├── collect
        │   ├── collect.crt
        │   └── collect.key
        ├── iam
        │   ├── iam.crt
        │   └── iam.key
        ├── ingest
        │   ├── ingest.crt
        │   └── ingest.key
        ├── pastis
        │   ├── pastis.crt
        │   └── pastis.key
        ├── referential
        │   ├── referential.crt
        │   └── referential.key
        ├── reverse
        │   ├── reverse.crt
        │   └── reverse.key
        └── security
            ├── security.crt
            └── security.key

environments/keystores/
├── client-vitam
│   ├── clients
│   │   └── keystore_vitamui.p12
│   └── truststore_client-vitam.p12
└── vitamui-services
    ├── clients
    │   ├── keystore_api-gateway.p12
    │   ├── keystore_archive-search.p12
    │   ├── keystore_cas-server.p12
    │   ├── keystore_collect.p12
    │   ├── keystore_iam.p12
    │   ├── keystore_ingest.p12
    │   ├── keystore_pastis.p12
    │   └── keystore_referential.p12
    ├── servers
    │   ├── keystore_api-gateway.p12
    │   ├── keystore_archive-search.p12
    │   ├── keystore_cas-server.p12
    │   ├── keystore_collect.p12
    │   ├── keystore_iam.p12
    │   ├── keystore_ingest.p12
    │   ├── keystore_pastis.p12
    │   ├── keystore_referential.p12
    │   └── keystore_security.p12
    └── truststore_vitamui-services.p12
```

Cette structure (`environments/certs`) définit trois **grandes zones d'autorité** :

1. **`vitamui-services`** : L'ensemble des certificats serveurs et clients permettant la communication HTTPS / mTLS entre les différentes applications (services internes, gateway, cass...) **au sein** de VITAMUI.
2. **`client-vitam`** : Accueille le point d'authentification qu'utilise l'application VITAMUI pour attaquer les APIs front de la solution tierce VITAM, ainsi que pour valider les échanges liés.
3. **`client-external`** : Autorités qui seront valables vis-à-vis des composants de l'extérieur du socle pour l'ensemble des sollicitations d'APIs externes.

---

### Scripts de génération des certificats

Le dossier `deployment/pki/scripts` contient de nombreux scripts qui permettent d'entretenir ce cycle pour le déploiement sur les environnements :

* **`generate_ca.sh`** : Permet de générer les certificats d'autorité (CA) initiaux de base.
  * L'argument `ERASE=true` permet de supprimer tous les résultats d'autorités CAs existantes et de les regénérer.
* **`generate_certs.sh <ENVIRONMENT_FILE>`** : Génère automatiquement (par rapport à sa configuration) l'ensemble de la nomenclature pour chaque client / serveurs de certificats (`crt`, `key`, etc.). Un fichier contenant les informations de variables d'environnement cibles doit être défini.
  * L'argument `ERASE=true` permet de supprimer tous les résultats d'autorités CAs existantes et de les regénérer.

> *Note : Les scripts suffixés par `_dev` concernent plutôt l'environnement très spécifique pour un lancement d'application à vide en environnement de développement via des tests.*

### Liste des magasins d'identités (keystores) par Composant applicatif

À chaque module applicatif on retrouvera les configurations liées (générées via `generate_stores.sh`). Le tableau ci-dessous explicite quels domaines de magasins et truststores sont exploités de manière unitaire par service pour correspondre au design du flux précédent.

| Composants                  | Keystores (Identité)                               | Truststores (Autorités de confiance)    |
|-----------------------------|----------------------------------------------------|-----------------------------------------|
| **cas-server**              | cas-server.crt, cas-server.key                     | ca-root.crt, ca-intermediate.crt        |
| **iam**                     | iam.crt, iam.key                                   | ca-root.crt                             |
| **referential**             | referential.crt, referential.key                   | ca-root.crt                             |
| **ingest**                  | ingest.crt, ingest.key                             | ca-root.crt                             |
| **archive-search**          | archive-search.crt, archive-search.key             | ca-root.crt                             |
| **collect**                 | collect.crt, collect.key                           | ca-root.crt                             |
| **pastis**                  | pastis.crt, pastis.key                             | ca-root.crt                             |
| **security**                | security.crt, security.key                         | ca-root.crt                             |

*(NB. La liste détaillée des propriétés et de la validation pure VITAM est présentée dans sa [Documentation complète de sécurité](http://www.programmevitam.fr/ressources/DocCourante/html/archi/securite/20-certificates.html))*

---

### Cas Pratiques d'Intégration Systèmes

#### 1. Communication croisée : Solution VitamUI <-> Vitam

La dépendance asymétrique impose un couplage de certificats croisé des deux parties :

**Dans le sens Requêtes VitamUI vers APIs Vitam :**

* Décliner et copier le CA source de VitamUI (`{vitamui_inventory_dir}/certs/client-vitam/ca/ca-*.crt`) => vers `{vitam_inventory_dir}/certs/client-external/ca`.
* Distribuer ce même client VITAMUI (`{vitamui_inventory_dir}/certs/client-vitam/clients/vitamui/vitamui.crt`) => vers l'endroit exact de validation `{vitam_inventory_dir}/certs/client-external/clients/external`.
* Renouveler les sources Vitam côté applicatif : lancement de `./generate_stores.sh` + run du playbook `ansible-playbook ansible-vitam/vitam.yml --tags update_vitam_certificates`.
* Assigner contextuellement une ouverture de sécurité du client à un contrat métier (Profil d'API) et rattacher le fameux point :
  * Exemple `postinstall_param.yml` (partie contexte sécurité et profil de restriction de confiance d'identifiant) :

  ```yaml
    vitam_additional_securityprofiles:
    - name: vitamui-security-profile
      identifier: vitamui-security-profile
      hasFullAccess: true
      permissions: "null"
      contexts:
        - name: vitamui-context
          identifier: vitamui-context
          status: ACTIVE
          enable_control: false
          permissions: "[ { \"tenant\": 0, \"AccessContracts\": [], \"IngestContracts\": [] }, { \"tenant\": 1, \"AccessContracts\": [], \"IngestContracts\": [] }]"
          certificates: ['external/vitamui.crt']
  ```

  * Activer via le playbook Vitam `ansible-vitam-exploitation/add_contexts.yml`

**Dans le sens Retour Vitam vers VitamUI :**

* À l'inverse, l'identité (CA) serveur de la solution appelante (Vitam) `{vitam_inventory_dir}/certs/client-vitam/ca` => descendre celle-ci vers vos espaces cibles `{vitamui_inventory_dir}/certs/client-vitam/ca/` (Afin que VitamUI la valide bien à temps pour son retour).
* Remonter la PKI UI et les TrustStores associés des serveurs cibles : lancement de `./generate_stores.sh` puis `ansible-playbook vitamui_apps.yml --tags update_vitamui_certificates`.

*(A noter que la gestion des relations avec "Le monde extérieur" suit un couplage manuel sur un modèle relativement identique mais non encore entièrement provisionné (WIP)).*

#### 2. Procédure d’ajout d’un certificat client externe

Afin d'enregistrer et de faire propager un client `M2M` externe supplémentaire jusqu'aux instances de vérification (comme les Truststores des APIs external de VITAMUI) :

1. Déposez l'autorité `CA(s)` de ce nouveau profil vers `deployment/environment/certs/client-external/ca`.
2. Déposez son propre certificat généré final vers `deployment/environment/certs/client-external/clients/external/`.
3. Assurez immédiatement le regroupement global via `deployment/generate_stores.sh`.
4. Transitez cette nouvelle vérification jusqu'aux machines en poussant les instances (Redéploiement Ansible) :

   ```sh
   ansible-playbook vitamui_apps.yml -i environments/hosts --vault-password-file vault_pass.txt --tags update_vitamui_certificates
   ```

> Attention : Il vous restera à lier applicativement l'usage via l'empreinte identité de ce composant du côté de la DataBase "MongoDB de l'IAM & SECURITY" en associant ses autorisations de sécurité et permissions sur des contextes.

---

## Clusterisation

### Multi-instanciation du service iam

Si le service `iam` est déployé sur plusieurs machines, les timers permettant la journalisation des évènements métiers de Vitam-UI seront lancés sur la première instance du groupe `[hosts_vitamui_iam]`. Seul une machine doit être déclarée primaire afin d'éviter la duplication des actions de journalisation.

Pour plus d'informations, se référer au DEX.

---

## Détail des services

### Service 1

* Description
* Contraintes

### Service 2

* Description
* Contraintes

---

## Détail des COTS

### Guidelines

Les COTS, software utlises par les solutions VITAMUI et VITAM, sont tous open-source. Pour des besoins de maintenabilite et de securites, ils sont entierement repackages au format RPM puis publies sur les repository yum VITAMUI.

VITAMUI s'appuie sur deux types de COTS dans son architecture:

* les COTS fournit par vitam auxquels VITAMUI se branche.
* les COTS utilises uniquement par VITAMUI

Le packaging des COTS suivront les principes suivants:

* Les noms de package cots seront de la forme vitamui|vitam-COTS_NAME. Les services systemd installes sur les systemes suivront la meme convention de nommage
* Les fichiers repackages permettront d'appliquer la protection de droits users system vitamui,vitam,vitamuidb,vitamdb
* Dans la mesure du possible les packages COTS rpm pourront contenir l'ensemble des fichiers du software
* Dans le cas contraire, le packages COTS VITAMUI|VITAM contiendrons des dependances vers les packages RPM officiels.
  Ils fourniront comme fichiers l'unit systemd du service COTS et des fichiers de configurations stockes dans le système de fichiers VITAMUI/VITAM permettant de proteger le lancement du service par les droits users systeme linux.

Le packaging specifique des cots VITAMUI contiendra un Makefile dedie pour chaque afin d'adapter la generation du contenu des packages. Ils pourront egalement contenir des templates de packaging dedies pouvant redefinir les fichiers unit systemd, les fichiers de configurations, les scripts d'installation RPM executes.

Le repackaging entier des COTS est la technique a priviligier pour les raisons suivantes:

* l'installation / désinstallation des fichiers pourra se faire entièrement dans les scripts RPM
* aucune dependance RPM donc pas d'etapes d'installation / desinstallation supplementaire et pas de gestion de repository supplementaire si la dependance n'est pas dans les repository officiels RedHat ou epel-release.
* Eventuellement, les sources/binaires des cots pourront etre conserves dans le repository vitamui.

### Liste des COTS Vitam

* consul
* mongo*
* mongo-express
* syslog
* elasticsearch
* curator
* siegfried
* cerebro
* logstash
* kibana
* apache

### Liste des COTS VitamUI

* consul
* logstash
* syslog
* mongo*
* nginx

## Packaging des COTS VITAMUI

### vitamui-mongo-express

Le package rpm vitamui-mongo-express est entierement repackage a partir de de l'installation via **npm**. Le package contient toutes les sources de mongo-express installee dans /vitamui/app/mongo-express et le ficher unit systemd de vitamui-mongo-express.

#### Mise a jour de la version de mongo-express

Pour modifier la version de mongo express, editez la dans le fichier `cots/vitamui-mongo-express/package.json`.

### Annuaire de services Consul

La découverte des services est réalisée avec Consul via l’utilisation du protocole DNS. Le service DNS configuré lors du déploiement doit pouvoir résoudre les noms DNS associés à la fois aux service_id et aux instance_id. Tout hôte portant un service VITAMUI doit utiliser ce service DNS par défaut.

L’installation et la configuration du service DNS applicatif sont intégrées à VITAMUI.

La résilience est assurée par l’annuaire de service Consul. Il est partagé avec VITAM.

* Les services sont enregistrés au démarrage dans Consul
* Les clients utilisent Consul (mode DNS) pour localiser les services
* Consul effectue régulièrement des health checks sur les services enregistrés. Ces informations sont utilisées pour router les demandes des clients sur les services actifs

La solution de DNS applicatif intégrée à VITAMUI et VITAM est présentée plus en détails dans la section dédiée à Consul dans la documentation VITAM.

---

## Multi instanciation des micro services

### Multi instanciation

Les services vitamui multi instanciable à ce jour sont :

* Service IAM
* Service UI Identity
* Service Portal
* Service Referential
* Service UI Referential
* Service Ingest
* Service UI Ingest
* Service Archive Search
* Service UI Archive Search
* Service Collect
* Service UI Collect
* Service Pastis
* Service UI Pastis
* Service Security
* Service Mongod

Un load balancer/reverse proxy (à défaut Consul) est installé et configuré pour la répartition de charge entre différentes instances.

La configuration de la mémoire des services est par défaut:

```conf
Xms=128m Xmx=512m
```

Cette configuration est modifiable dans les jvm_opts de l'ansiblerie, pour plus d'informations (cf: DEX).

### Mono instanciation

Le service CAS est actuellement mono-instanciable.
