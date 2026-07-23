* un IdP en particulier supporté
* n'import quel IdP

Commen gérer les utilisateurs:

* Connecteur IdP pour changer la base de données de l'IdP
* Utilisation d'un système tier LDAP + Spring DATA

* Virer CAS pour du spring security ?
    * Connexion locale
    * Connexion IdP OIDC et SAML
    * OAuth2 / JWT pour le jeton applicatif

Demande, lister les fonctionnalités de manière exhaustive pour présentation.

* Subrogation: point dur authentification forcée pour réaliser la subrogation
* Organization multi domaines: point dur pas supporté par les solutions IAM
* Email de création de compte: point dur mise en oeuvre d'une page de gestion des password
* Cycle de vie des comptes: déjà géré dans Vitam UI
* OTP: point dur nécessite un système externe ou implementation locale
* MFA: point dur nécessite un système externe ou implementation locale
* Gestion des utilisateurs: Nécessite d'avoir accès à la base d'utilisateur
* Gestion des organisations: Création d'un compte administrateur, sélection de tenant, association d'IdP (concepte à cheval entre du métier et de l'identité)


Point d'attention:
* Pouvoir vérifier l'identité pour les élévations des prilèges comme la subrogation. (demande de login avaec d'effectuer)
* Si modèle avec solution IAM comme base d'identité, gestion des utilisateurs creation, password... si keycloak, utilisation api keycloak ?

Pour l'instant plusieurs solutions:
* Intégration full Keycloak, non indépendant, perte de feature comme le multi-domaine si full OIDC, nécessite des mires d'authentification cf. France Connect
* IAM comme base d'utilisateurs, problème de gestion des utilisateurs, comment bind les droits avec l'identité externe ?
* Full spring-boot security, nécessite de vérifier la faisabilité de la connexion OIDC/SAML d'identités externes.

