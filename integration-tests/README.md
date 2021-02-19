# Tests d'intégration

## Pré-requis

Installer les composants suivants :
 * Docker version > 17.x
 * Docker-compose > 1.18
 * Maven 3
 * Java 1.8

Les tests d'intégrations lancent une instance de VITAM via docker.
L'IP du docker VITAM est 172.17.0.2


## Execution

Lancement des tests d'intégration :

    cd integration-tests
    mvn clean integration-test -Pintegration-tests

Les instances docker sont démarrées et arrêtées automatiquement via l'utilisation du profile integration-tests.

Il est nécessaire de démarrer les services utilisés par les tests d'intégration avant leur exécution en local.

Par exemple, l'exécution des tests d'intégration Referential back-end nécessite : Referential external, Referential internal, IAM external, IAM internal, CAS, et Security

## Execution en développement

Pour éviter de lancer et arrếter les instances docker et les services à chaque exécution, vous pouvez démarrer manuellement :

    ./start_vitamui.sh

Vous pouvez ensuite démarrer les tests via votre IDE.

Pour arrêter les instances Docker et les services, exécuter la commande :

    ./stop_vitamui.sh

## Execution de tests cibles

Utilisez les tags existants dans les Features pour lancer tous ses scénarios (@NomDuTag), ou ajoutez un tag au dessus d'un scénario particulier à exécuter.

Pour un lancement via Eclipse, ajoutez le(s) tag(s) aux options du Runner, puis exécutez le Runner comme une classe de tests JUnit :

	@CucumberOptions(tags = "@NomDuTag1, @NomDuTag2", features = "...

Pour un lancement en ligne de commande, ajoutez l'option Cucumber à la fin comme dans cet exemple :

	mvn clean verify -P iam -Dcucumber.options="--tags @ApiIam"

Pour lancer tous les tests d'intégration remplacer iam par dev-it

	mvn clean verify -P dev-it


## Generation generic-it.jks
Utiliser nginx.jks de l'envionnement rabbit et le renommer.
