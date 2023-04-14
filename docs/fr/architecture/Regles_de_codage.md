# Règles de codage

Un projet GIT de tools sera créé pour contenir les projets style cas-app1-springsecurity, vitam-import ...

Lors de la correction de bugs, des tests unitaires doivent être attachés à la correction afin de garantir qu'il n'y aura pas de régression.

## Configuration Editeur Java

Fichier checkstyle + saveactions / cleanup / formatter + findbugs + pmd disponibles dans le dossier Drive configuration editeur

## Git

Nous fonctionnons en différentes équipes de développement et en pull requests.

Chaque développeur est lié à un groupe de 3 ou 4 personnes max.
Ce groupe doit organiser au moins une fois par semaine.
une réunion afin de discuter des points fonctionnels sur lequels les membres travaillent, de partager un peu plus en détail les features en cours de développement ou de faire des reviews sur les codes respectifs des membres du groupe.
Un responsable de groupe est désigné afin de booster l'organisation avec la réservation de salles, la mise en place de l'ordre du jour des points de groupes.

Avant d'attaquer une US ou une TS ou un FIX, le développeur crée une branche :
**feature_nomteam_numJira**

Au 16/07/2019 Les noms d'équipes sont **turtles, rabbits, foxes**

Lors que les développements sont finis, une pull request doit être créée en mettant les membres du groupe et le techlead de l'équipe qui in fine valide la pull request, émet des commentaires, demande des modifications si nécessaires, vérifie le taux de couverture de tests unitaires, les tests d'intégration Spring et Cucumber.

Le commit doit suivre le format indiqué si dessous.
Le commentaire permet de faire un descriptif succint du commit.

1. Si le développement est lié à une US (User Story)
[US ID_JIRA] Titre sommaire(en quelques mots sur une ligne)
Description (sur une 2e ligne)

2. Si le développement est lié à une TS (Technical Story)
[TS ID_JIRA] Titre sommaire
Description

3. Si le développement est lié à une anomalie
[FIX ID_JIRA] Titre sommaire
Description

4. Si le développement est plus sur une partie technique du socle
[TECH] Titre sommaire
Description

5. Si le développement est plus lié à la partie test : ajout de tests, corrections de tests
[TEST] Titre sommaire
Description

6. Si les modifications sont liées à la partie devops / infra
[DEVOPS] Titre sommaire
Description

## Structure des packages

Package fonctionnel
ex: `fr.gouv.vitamui.profil, fr.gouv.vitamui.profilegroup`
Chaque package fonctionnel contiendra des sous packages dao, domain et service.
Les packages sont au singulier

Les clients REST qui seront exposés sont dans un module client.
Exemple : `api-iam-client`

Pas de dépendances circulaires entre les packages (à vérifier dans les code reviews)
Mettre underscores à la place des tirets pour les noms de package.

Fichier `messages.yml` pour les messages
Se poser la question pour les propriétés si c’est une propriété interne au projet qui ne change pas ou c’est lié à l’environnement. Valeur dev, recette, préprod ou prod.
Fichier `application-dev.yml` dans `src/main/resources/config`. Les éléments configurés dans ce fichier sont surchargées lors du déploiement avec Ansible.

## API

Les API et leurs structures doivent être validées par le comité API afin de garder une cohérence.
cf <https://restfulapi.net/resource-naming/>

## Maven

Définir le groupId pour les modules Maven pour définir le parent direct.

Définir les dépendances et les versions des dépendances dans le pom parent.

L'ajout ou la modification des dépendances doivent être validés par les techleads.

## Code

Commentaires en anglais, code en anglais.
Injection de dépendances via le constructeur.
Favoriser les classes immutables
Eviter les méthodes statiques autant que possible

Ordre des modifiers dans Java :

1. Annotations
2. public
3. protected
4. private
5. abstract
6. static
7. final
8. transient
9. volatile
10. synchronized
11. native
12. strictfp

Remplacer Lambda par une méthode référence :

```java
dto -> processAfterQuery(dto)           => this::processAfterQuery
protected D processAfterQuery(final D dto) {
        return dto;
    }
```


Utiliser des classes de constantes le plus possible pour définir les constantes et mutualiser ces éléments.

Lombok : utiliser les getters, requireargsconstructor, setters, tostring, hashcode.
Eviter les setters dans la mesure du possible
pour l’annotation @Data privilégier les members à final
**L'utilisation des annotations de logs style @Slf4j est interdite**
Le logger de VITAMUI doit être utilisé afin de garder un format de log uniforme dans le système.

## Gestion des exceptions

Pré-requis : Le module SpringBoot doit étendre les modules commons/api et commons/rest.

Dans la classe ContextConfiguration du projet SpringBoot, il faut :
Étendre la classe AbstractContextConfiguration qui se trouve dans le package fr.gouv.vitamui.commons.api.application
Importer la classe RestExceptionHandler suit se trouve dans le package fr.gouv.vitamui.commons.rest

Ces deux actions permettent d’abord de définir un Dispatcher central pour les Controllers et les Handlers de requêtes HTTP, par exemple de lever une Exception lorsqu’au Controller ne répond à une requête donnée. Ensuite ce Dispatcher définit des règles d’interception d’exceptions afin de définir la réponse adaptée à une exception dans VITAMUI.
Les exceptions Spring sont interceptées également et transformées en Exceptions VITAMUI.
Cette réponse correspond à un format bien défini :
key : cette propriétée contient une clé qui permettra à l’application appelante d’identifier l’exception et de la mapper par exemple à un fonctionnement bien déterminé. Par exemple afficher une page d’authentification dans le cas d’une authentification invalide.
message : cette propriété contient le message correspond à l’Exception. Chaque module SpringBoot définit ses propres messages d’erreurs en ajoutant le fichier errors.yml dans le dossier src/main/resources  et en changeant les messages pour les adapter à son contexte.
args : définit une liste qui peut correspondre par exemple à une liste de noms de paramètres invalides ou aux éléments d’une requête introuvable.

Exemple :

```json
{
    "args": [
        "GET",
        "/sae/v1/admin/checkExternalStatus/toto/1"
    ],
    "message": "The path 'GET /sae/v1/admin/checkExternalStatus/toto/1' was not found on this server.",
    "key": "apierror.routenotfound"
}
```

Une classe utilitaire ApiErrorGenerator permet de générer une Exception VITAMUI et de donner des arguments qui peuvent être utiliser pour générer le message correspondant.
Nos classes de Service ou Controllers doivent se baser sur ApiErrorGenerator  pour générer leurs exceptions.

Les modules qui n’ont pas SpringBoot peuvent lever directement une exception sans passer par cette classe utilitaire. Les modules SpringBoot qui utilisent ces modules de base vont gérer automatiquement le format de la réponse correspondante à l’exception.

## Gestion des  Logs

La gestion des logs est assez similaire à Vitam pour être cohérent à la solution globale.
Chaque composant ou application ou module SpringBoot doit être identifié clairement avec un nom(identityRole), un nom et un identifiant de serveur sur lequel tourne cette application (identityName et identitySiteId). Cette identification correspond à un ServerIdentity.
Via de l’auto-configuration et des données de configuration de l’application, le ServerIdentity est créé et adossé au Logger.

Le Logger que nous utilisons est Logback. Logback via un rsyslog va envoyer les logs dans l’ELK de VItam.
Un format de logs a été créé et est similaire au format de Logs de Vitam. Le fichier logback.xml contient les configurations du log avec des fichiers tournant et le format de log.
Un rsyslog est déployé sur les machines hébergeant les composants VITAMUI  et envoie les logs applicatifs syslog vers un serveur de centralisation de logs (via facility local0)
Un serveur de centralisation de logs est utilisé et ce serveur se base sur l’ELK Vitam:
un mono-noeud (au minimum, ou multi-noeuds) Elasticsearch
un moteur logstash, parsant les messages VITAM
 un afficheur de rendu/aggrégation de données Kibana

Pour créer un logger, on peut utiliser une propriété statique dans la classe avec :
private static final VitamUILogger `LOGGER = VitamUILoggerFactory.getInstance(SAEApplication.class);`

Attention : L’utilisation de Logger est assez restreinte. Le projet doit utiliser SpringBoot et le ServerIdentity doit être initialisé avant de pouvoir logger. Sinon une exception de type InternalServerException est levée.
Il faut par conséquent faire attention à l’ordre d’initialisation des beans et des classes de configuration.

Ajouter dans le code Java
    private static final VitamUILogger `LOGGER = VitamUILoggerFactory.getInstance(SAEApplication.class);`

Ajouter dans `src/main/application.yml` les informations du server-identity à la racine

```yml
server-identity:
  identityName: vitamui
  identityRole: sae-app
```

Ajouter dans `src/main/config/application-dev.yml` les informations du server-identity à la racine

```yml
server-identity:
  identityServerId: 1
```

## FIXME & TODO

Les FIXME et TODO doivent être utilisés avec parcimonie.
Le point doit être remonté au techlead et au PO si nécessare et en fonction du degrè d'urgence de la demande, le point doit être traité dans la foulée ou une TS associée doit être créée et ajouter dans le prochain sprint.

## Configuration Sonar

Tous les problèmes affichés dans Sonar doivent être des problèmes pertinents pour éviter que nous soyons pollués par d’autres problèmes que nous n’allons pas prendre en compte.
Les classes Entity de Mongo et leurs DTO correspondants ne sont pas analysés par rapport à la duplication de code. Cela évite d’avoir à afficher les erreurs de duplication sur des cas que nous avons bien voulu définir ainsi.
Un fichier lombok.config a été créé dans le projet VITAMUI pour éviter que Sonar marque les annotations Lombok comme non couvertes.
