## Les impacts d'avoir plusieurs applications dans un seul micro-service

 **Changement du mécanisme d'implémentation et de fonctionnement**:

Avec un seul micro-service, il faut regrouper les modules développés dans un seul projet au niveau de la partie UI, c'est à dire avoir un seul projet ui-service qui contient les différents services et endpoint rest avec la partie front du service.
Suivre l'exemple de Identity et Referential (2 micro-services avec plusieurs applications).

 **problème de casser l'application d'une manière complète (le service tombe en panne)**:

Avec un seul micro service, il y aura juste un seul service déployé donc si on a un souci de déploiement ou un problème de configuration, l'application VitamUI sera en panne en attendant que le service soit up, ce problème peut etre évité si on a la possibilité de multi-instancier le service sur plusieurs VM et sur plusieurs serveur (scalabilité horizontale et verticale).

 **Refonte des services développés**

Il faut refaire les développements coté UI, pour regrouper l'ensemble des services au niveau d'un seul service, c'est comme le service Identity et referential. Dans ce nouveau service il faut déclarer un ensemble des sous modules pour chaque application déclarée dans ce service.
la partie Front de notre service sera ajoutée dans le projet ui-frontend comme un projet à part et ce projet va contenir des sous-applications Angular selon le nombre des applications déclarées et définies au niveau de notre micro-service.

 **Refonte de la configuration, la partie mongo et aussi Ansible** :

- Il faut modifier les scripts qui alimentent la base de données Mongo lors de l'initialisation/ déploiement de VitamUI sur un environnement. Le fait d'avoir un seul service va automatiquement demander des modifications au niveau des scripts Ansible et Mongo soit pour la partie alimentation de la base de données (Iam, Services...) soit au niveau de la partie configuration.

- Penser à des nouvelles category pour les applications qui seront regroupées.

**Impossible d'implémenter une procédure de déploiement « par service » permettant de ne retrouver sur les serveurs que les packages/le code relatifs a ces services et que le système soit configuré en conséquence**

Avec un seul micro service qui contient plusieurs applications, c'est impossible de choisir quel service à déployer, parce qu'il y a un seul service déployé avec tout ses différents modules (back). Et pour qu'un exploitant a la possibilité d'installer et de déployer des services spécifiques, il faut les services soient séparés comme ça il peut déployer un par un.

## Les étapes pour ajouter une application existante dans un service à part le service initial de l’application

- Déplacer le composant parent de l’application vers le composant du service (dans notre cas archive vers ingest).
- Modifier le fichier app-routing-module.ts de notre service (front) en ajoutant un autre path qui pointe sur le composant parent de l’application qui sera ajoutée dans notre service.
- Prendre les services et les contrôleurs de l’application et les mettre dans les services et les contrôleurs du nouveau service (dans notre cas ui-ingest), on déplace les services et les contrôleurs de ui-archive dans ui-ingest.
- Modifier la partie configuration (fichiers yml).
- Modifier la partie roles et templates pour ajouter la configuration en modifiant le fichier application.yml,j2 (c’est pour générer la partie déploiement).
- Modifier le fichier `security_ref.js.j2` du service concerné en ajoutant les roles de la nouvelles application dans le ui_service_context.
- Modifier le fichier `application_ref.js.j2` de l’application concernée en changeant l’URL.
- Supprimer le contenu des fichiers : `security_application_ref.js.j2` ,  `cas_services_ref.js.j2`, `security.populate_certificates_ref.js.j2`, les 3 fichiers concernent l’application en question (ancien service qu’on souhaite ajouter comme application dans un autre service).
- Modifier le fichier: `security.populate_certificates_ref.js.j2` en supprimant la ligne qui déclare l’ancien service de l’application.
- Modifier les fichiers hosts.local et hosts.vitamui
- Modifier le fichier `vault-keystores.yml`
- Modifier le fichier `vault-certs.yml`.
- Générer les certificats en exécutant le script `generate_certs_dev.sh`
- Générer les stores en exécutant le script `generate_stores_dev.sh`
- Alimenter la base de données en exécutant le script : `restart_dev.sh`