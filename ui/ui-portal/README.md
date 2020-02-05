# IHM PORTAL


## Development

Install :

1. [Maven]
2. [Node.js]


Pour démarrer l'application en mode développement lancer les commandes suivantes dans des consoles différentes

    mvn clean spring-boot:run
    npm start

La commande mvn spring-boot:run utilise le fichier de configuration présent dans src/main/config/application-dev.yml

## Testing

    mvn clean test

## Build in production

    mvn clean install
