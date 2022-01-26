
# Orientations techniques

Les orientations techniques présentées ci-après ont présidé à la conception de la solution VITAMUI. Ces orientations ont été en grande partie basées sur les choix effectués pour le développement du socle d'archivage VITAM.  

## Composants open-source

Afin d'assurer la transparence et la pérennité du code, la solution utilise principalement des logiciels et des bibliothèques Open Source.

## Architecture micro-service

Pour bénéficier d’une dépendance faible entre les différents composants du système, l’implémentation de la solution est basée sur une architecture applicative micro-service. Cette architecture permet de répondre aux enjeux de sécurité, de scalabilité et d’évolutivité de la solution. L'annuaire de service intégré dans la solution permet la localisation des services dans l'infrastructure et simplifie la communication entre les différents services. 

## Protocole REST  

Les composants externes et internes de la solution communiquent entre eux via des appels Rest afin d’assurer un découplage fort entre les différents modules.

## Sécurité

La sécurité a été prise en compte dès la conception (Security By Design & Privacy By Design) de la solution. 

L’authentification des utilisateurs est basée sur la solution IAM(Identity Access Management) CAS. La délégation d'authentification des utilisateurs repose sur des protocoles fiables et standards (SAML v2, Oauth, CAS, etc.)

L'authentification des applications clientes est basée sur une authentification TLS mutuelle utilisant des certificats (pour les composants de la couche accès). 

## Exploitation

Pour bénéficier d’une exploitation simplifiée, la solution dispose d’un outillage permettant de suivre l’activité du système global. Ce outillage permet d'assurer :

* la gestion des logs centralisés
* le suivi des métriques

Chaque service est en capacité de fournir les logs et les informations internes reflétant son état et son fonctionnement.

## Déploiement
Pour faciliter l’évolution et la mise des différents composants de la solution, une chaîne de déploiement continue est mise en oeuvre.

## Continuité de service
La solution est conçue pour être installée sur plusieurs sites et pour assurer la sécurité et les accès aux données en cas d'indisponibilité d’un des sites. 

