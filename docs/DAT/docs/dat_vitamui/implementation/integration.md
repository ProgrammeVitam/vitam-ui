
## Intégration système

### Utilisateurs et groupes d’exécution

La segmentation des droits utilisateurs permet de respecter les contraintes suivantes :

* Assurer une séparation des utilisateurs humains du système et des utilisateurs système sous lesquels tournent les processus 	
* Séparer les droits des rôles d’exploitation différents suivants :
    * Les administrateurs système (OS) ;
    * Les administrateurs techniques des logiciels VITAMUI;
    * Les administrateurs des bases de données VITAMUI

Les utilisateurs et groupes décrits dans les paragraphes suivants doivent être ajoutés par les scripts d’installation de la solution VITAMUI. En outre, les règles de sudoer associées aux groupes vitamui*-admin doivent également être mis en place par les scripts d’installation.

Les sudoers sont paramétrés en mode NOPASSWD, c’est à dire qu’aucun mot de passe n’est demandé à l’utilisateur faisant partie du groupe vitamui*-admin pour lancer les commandes d’arrêt relance des applicatifs vitamui.

Les fichiers de règles sudoers des groupes vitamui-admin et vitamuidb-admin sont systématiquement écrasés à chaque installation des paquets (rpm) déclarant les utilisateurs VITAMUI. (Un backup de l’ancien fichier est cependant effectué).

#### Utilisateurs

Les utilisateurs suivant sont définis :

* vitamui(UID : 4000) : user pour les services ne stockant pas les données
* vitamuidb (UID : 4001) : user pour les services stockant des données (Ex : MongoDB)

Les processus VITAMUI tournent sous ces utilisateurs. Leurs logins sont désactivés. 

#### Groupes

Les groupes suivant sont définis : 

* vitamui(GID : 4000) : groupe primaire des utilisateurs de service
* vitamui-admin (GID : 5000) : groupe d’utilisateurs ayant les droits “sudo” permettant le lancement des services VITAMUI
* vitamuidb-admin (GID : 5001) : groupe d’utilisateurs ayant les droits “sudo” permettant le lancement des services VITAMUI stockant de la donnée.

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

A l’intérieur de ces dossiers, les droits par défaut sont les suivants :

* Fichiers standards :
    * Owner : vitamui (ou vitamuidb)
    * Group owner : vitamui
    * Droits : 0640

* Fichiers exécutables et répertoires :
    * Owner : vitamui (ou vitamuidb)
    * Group owner : vitamui
    * Droits : 0750

Cette arborescence ne doit pas contenir de caractère spécial. Les éléments du chemin (notamment le service_id) doivent respecter l’expression régulière suivante : [0-9A-Za-z-_]+

Le système de déploiement et de gestion de configuration de la solution est responsable de la bonne définition de cette arborescence (tant dans sa structure que dans les droits utilisateurs associés).

### Intégration au service d'initialisation Systemd

L’intégration est réalisée par l’utilisation du système d’initialisation systemd. La configuration se fait de la manière suivante :

* /usr/lib/systemd/system/ : répertoire racine des définitions de units systemd de type “service”
* <service_id>.service : fichier de définition du service systemd associé au service VITAMUI

Les COTS utilisent la même nomenclature de répertoires et utilisateurs que les services VITAMUI, à l’exception des fichiers binaires et bibliothèques qui utilisent les dossiers de l’installation du paquet natif.