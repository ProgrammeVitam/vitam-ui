# SAE Application


## Development

Install :

1. [Maven]
2. [Node.js]

Ajouter dans votre fichier /etc/hosts la ligne suivante :

	dev.vitamui.com

Pour démarrer l'application en mode développement lancer les commandes suivantes dans des consoles différentes

    mvn clean spring-boot:run
    npm start

Pour démarrer l'application en mode packagé

	mvn clean package -Pwebpack
	java -Dspring.config.additional-location=src/main/config/ui-identity-application-recette.yml -jar target/ui-identity.jar

La commande mvn spring-boot:run utilise le fichier de configuration présent dans src/main/config/application-dev.yml

## Testing

    mvn clean test

## Build in production

    mvn clean package -Pwebpack
