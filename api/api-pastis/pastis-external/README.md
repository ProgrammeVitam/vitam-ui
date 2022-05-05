# CINES Application


## Development

Prérequis :

1. [Maven]
2. [Node.js]
3. [Java 11+]

Ajouter dans votre fichier /etc/hosts la ligne suivante :

        127.0.0.1   dev.vitamui.com

Pour démarrer l'application en mode développement lancer le commande suivante dans une console

    mvn clean spring-boot:run

Pour démarrer l'application en mode production

        mvn clean install
        ./run.sh

La commande mvn spring-boot:run utilise le fichier de configuration présent dans src/main/resources/application-dev.yml
