# En Développement
Ce projet est en développement.

# Pre-requis
    Disposer à minima de 8GO de RAM et de 20 GO de disk

    Centos - Installer docker en dernière version de docker engine
        sudo yum check-update
        curl -fsSL https://get.docker.com/ | sh
        sudo systemctl start docker
        sudo systemctl status docker
        sudo systemctl enable docker (to start automatically)

# Lancement des services Mongo avec docker
Pour lancer Mongo exécuter la commande :

    ./start_XXX.sh

Cette commande permet de démarrer les containers et de lancer les services Mongo.


# Arrêt des services Mongo avec docker

Pour arrêter les services Mongo exécuter la commande :

    ./stop_XXX.sh

Aucune donnée n'est sauvegardée à l'arrêt de la base.

# Suppression du container Mongo

Pour détruire le container vitamui-mongo :

    docker rm vitamui-mongo

# Ajouter un utilisateur Admin pour mongo

$ docker exec -it vitamui-mongo mongo admin
connecting to: admin

    > db.createUser({ user: 'jsmith', pwd: 'some-initial-password', roles: [ { role: "userAdminAnyDatabase", db: "admin" } ] });
    Successfully added user: {
        "user" : "jsmith",
        "roles" : [
            {
                "role" : "userAdminAnyDatabase",
                "db" : "admin"
            }
        ]
    }



# Initialisation de la base Mongo

Afin de mutualiser le code et les scripts Mongo exploités en developpement ou lors du déploiement sur un environnement, l'intelligence a été répartie de la manière suivante:

- Les scripts Mongo figurent à l'emplacement suivant: **~/deployment/scripts/mongod**
- Le role Ansible d'initialisation figure à l'emplacement suivant: **~/deployment/roles/mongo_init**

Le role d'initialisation de la base de données a plusieurs fonctionnalités: ordonnancement des scripts, templatage et versioning.

## Templatage des scripts de DB

La structure du dossier **mongod** est simple:
```txt
> mongod
  > 0.0.0
     > 01_script.js
     > 02_script.js
  > 0.1
  > 1.0
  > 1.0.5
  > 1.0.10
```

Deux niveaux de répertoires sont pris en compte:

- 1er niveau: la version de l'application

  Lors du templatage, un premier tri par version est exécuté afin de trier ces dernières par ordre croissant.

- 2ème niveau: les scripts à exécuter par version

  Lors du templatage, les scripts sont triés par ordre d'index (X_nomDuScript) afin d'assurer l'ordonnacement de l'exécution des scripts. Si aucun index n'est renseigné, l'ordre alphabétique s'applique.

  Par convention, les catégories d'indexes sont:
  - 0-99: scripts de référence à passer quelque soit les environnements
  - 100-199: scripts de démo
  - 200-299: Scripts de developpement.

Les scripts de templatage utilisés sont ceux du déploiement. Lors du packaging du projet, l'ensemble des scripts est copié et intégré à l'archive de déploiement.

Afin d'injecter les bonnes valeurs aux variables des templates, le fichier **mongo_vars_dev.yml** est présent.

Afin d'intégrer une surcharge extérieure des variables par défaut, il est possible de renseigner le chemin d'un autre fichier de variable à travers la variable d'environnements **ADDITIONNAL_VITAMUI_CONFIG_FILE**.
Attention, toute variable définie dans ce fichier additionnel écrasera la valeur existante.

## Versioning des scripts Mongo

Avant l'exécution d'un script, on vérifie que ce dernier a été exécuté. Pour ce faire, nous nous basons sur deux éléments:
- le nom du fichier
- le checkum du fichier

Lors de l'initialisation de la base de données, la base de données **versioning** est créée et la collection suivante est initialisée:
```
Changelog {
  id: Identifiant interne mongo
  filename: nom du fichier
  date: date d'éxécution du script
  version: version de l'application associée au script
  checksum: hash du fichier
}
```

Avant l'exécution de chaque script, on effectue une recherche d'exécution (sur les champs filename et checksum):
- si le script a déjà été exécuté, aucun action n'est effectuée
- si le script n'a pas été exécuté, ce dernier est exécuté et une entrée est rajoutée dans la collection *changelog*

Toute erreur lors du processus entraine l'arrêt de l'initialisation de la base Mongo

Dans le cas d'une migration d'une version de VitamUI précédent le versioning, il suffit de définir la variable Ansible **mock_insert_data**. Dans le cas présent, l'ensemble des scripts sera joué mais non exécutés, seules les entrées dans la collection **changelog** seront ajoutées.

## Coloration syntaxique intellij

Si vous utilisez un IDE intellij, le plugin suivant vous offrera la coloration syntaxique adéquate ainsi que quelques
fonctionnalités utiles pour le YAML, Ansible et les templates jinja2 :
https://plugins.jetbrains.com/plugin/7792-yaml-ansible-support
