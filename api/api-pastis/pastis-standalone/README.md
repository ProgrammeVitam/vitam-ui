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
    ./build.sh
    cd api/api-pastis/pastis standalone
    mvn clean spring-boot:run
    cd ui/ui-frontend
    npm run start

Pour démarrer l'application en mode production

        mvn clean install
        ./build.sh
        ./run.sh (pour tester pastis standalone)
        
        l'executable windows peut etre récupéré dans api/api-pastis/pastis-standalone/target/pastis-standalone-{verion}-package.zip
