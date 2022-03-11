# CINES Application

## Pastis Standalone version

La version Pastis standalone est une abstraction de pastis intégrer dans vitam-ui, elle ne communique dans aucun cas avec vitam.
Elle permet d'editer des profiles d'archivage chargé depuis un poste, il ne permet pas d'éditer les profiles d'archivage dans vitam

## Development

Prérequis :

1. [Maven]
2. [Node.js]
3. [Java 11+]

Pour démarrer l'application en mode développement

    mvn clean install
    mvn clean spring-boot:run

Pour démarrer l'application en mode production

        mvn clean install -Pstandalone
        ./build.sh
        ./run.sh
