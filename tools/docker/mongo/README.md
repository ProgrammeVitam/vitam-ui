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



# Templatage des scripts de DB


## Coloration syntaxique intellij

Si vous utilisez un IDE intellij, le plugin suivant vous offrera la coloration syntaxique adequate ainsi que quelques
fonctionnalité de
https://plugins.jetbrains.com/plugin/7792-yaml-ansible-support

##
