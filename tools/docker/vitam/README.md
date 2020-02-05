# En Développement
Ce projet est en développement.
Il existe actuellement les versions suivantes :
 * dev qui exécute ansible en local et qui n'installe pas ELK
 * recette qui lance 2 containers, ansible se connecte en ssh et ELK est installé dans le container vitam-admin.

# Pre-requis
    Disposer à minima de 8GO de RAM et de 20 GO de disk

    Centos - Installer docker en dernière version de docker engine
        sudo yum check-update
        curl -fsSL https://get.docker.com/ | sh
        sudo systemctl start docker
        sudo systemctl status docker
        sudo systemctl enable docker (to start automatically)

# Lancement des services VITAM avec docker
Pour lancer VITAM exécuter la commande :

    ./start_XXX.sh

Cette commande permet de démarrer les container et de lancer les services VITAM.


# Arrêt des services VITAM avec docker

Pour arrêter les services VITAM exécuter la commande :

    ./stop_XXX.sh

# URL utiles

 * IHM DEMO : http://172.17.0.2:8002/ihm-demo/
 * IHM DEMO V2 : http://172.17.0.2:8002/ihm-demo-v2/
 * Consul UI : http://172.17.0.2:8500
