> **Document historique — 27 juillet 2026.**
> Repris et actualisé par `api/auth-gateway/docs/2026-07-31/dossier-arbitrage-socle-authentification.md`, **qui fait foi**.
> Conservé pour la traçabilité.
>
> **Ce qui n'est plus exact dans ce document :**
> - **« 20-30 jours »** pour le précédent CAS 6 → 7 — la valeur consolidée est **25 jours déclarés, 50 à 60 réels** ;
> - les **deux tableaux de complexité vides** sont les gabarits d'origine ; les grilles renseignées sont en annexe A du document de référence.

---

* valider que les idp soient bien gérer isolément des autres idp aux sein d'une même organisation.

Deux idp OIDC, vérifier que le scope applicatif est bien le bon ?

Tenant 5, si quelqu'un vient de avec une authentification valide pour un autre tenant.

A voir avec Lotfi.

* vérifier authentification par certificats.

-> supporté par CAS à la base

----

Problème du maintient de CAS

* Cout récurrent, 20-40 J/H


Faire les solutions:

## garder CAS

- Evolution 1-2 mois de développement, intervention J. Leleu.   
- 20-30 jours avec exemple d'upgrade.   
- prochaine mise à jour, à faire sans exemple.

## utilisation solution IAM

Mode d'utilisation générique:

- Fonctionnalités de base   
- Pas compatible avec la notion d'organisation de Vitam UI    
    * l'enregistrement de plusieurs IdP se fait au scope realm. L'organisation devient un concepte IAM, pas de multidomaine avec même suffixe dans plusieurs organisations.
    * rend caduque les pages de gestion utilisateurs, groups, droits.

> Implique basculement de responsailité du module iam à la solution IAM avec les limitations techniques vis-à-vis du modèle métier vitam ui (organisation multi-domaine, subrogation...). 

Mode d'utilisation découplé:

* support des utilisateurs (identité) uniquement.
* developpement d'un module permettant de mapper une identité aux modèle métier de vitam ui.
* émission d'un token d'accès applicatif qui sert de base de deal avec le reste de l'application en full JWT.

> Le module iam ou un autre module permet de gérer la validation de tokens d'identité émis par la solution IAM.
> Ne répond pas au problème de l'existant, la base de données utilisateurs est côté solution IAM.
> Problèmatiques de gestion des comptes, création de superadmin, compte génériques... Necessite de développer des connecteurs ?
>
> Peu d'intérêt. Mais changement de technologie pour la base d'identité facile tant qu'elle supporte JWT.

Mode d'utilisation intégré:
Si on veut conserver le fonctionnel:
- Mise en place d'un adaptateur pour mongo et la base d'utilisateur.
- Developpement du Webflow spécifique à Vitam dans l'outil IAM.

## Développement Spring Authorization Server

* On est déjà dans l'écosystème spring.
* Permet de conserver le fonctionnel.
* On aura les spécificités Vitam dans le notre base de code.
* A pas d'adhérences à un nouveau COTS
* Nécessite de transposer les vue du flow dans l'application front et de développer les fonctionnalités qui étaitent présentes dans CAS.

---

# Tableau des solutions originales

## Solution remplacer CAS par Keycloak

| Liste des cas d'usages de CAS | Standard Keycloak (oui?/non) | Complexité |
|---|---|---:|
| Authentification login / mot de passe |  |  |
| Authentification déléguée SAML2 |  |  |
| Authentification déléguée OpenID Connect (OIDC) |  |  |
| Authentification par certificat client (X509) |  |  |
| Authentification multi-facteurs (MFA) |  |  |
| Subrogation admin (de l'utilisateur générique d'organisation, sans validation) |  |  |
| Subrogation (d'un utilisateur "non générique", avec validation par l'utilisateur) |  |  |
| Gestion du mot de passe |  |  |
| Gestion multi-domaine sur organisation |  |  |
| SSO et déconnexion globale (Single Logout) |  |  |
| Autres oubliés ???? |  |  |
| **Sous total complexité reprise des cas d'usages** |  | **0** |
| Packaging, intégration initiale |  |  |
| Déploiement / configuration / supervision |  |  |
| Tests d'intégration généraux |  |  |
| **Grand Total** |  | **0** |

## Solution montée sur la dernière version de CAS en faisant des travaux d'architecture pour mieux l'intégrer (impacté par MDV)

| Liste des cas d'usages à adapter pour montée de version CAS | oui?/non | Complexité |
|---|---|---:|
| Authentification login / mot de passe |  |  |
| Authentification déléguée SAML2 |  |  |
| Authentification déléguée OpenID Connect (OIDC) |  |  |
| Authentification par certificat client (X509) |  |  |
| Authentification multi-facteurs (MFA) |  |  |
| Subrogation admin (de l'utilisateur générique d'organisation, sans validation) |  |  |
| Subrogation (d'un utilisateur "non générique", avec validation par l'utilisateur) |  |  |
| Gestion du mot de passe |  |  |
| Gestion multi-domaine sur organisation |  |  |
| SSO et déconnexion globale (Single Logout) |  |  |
| Exemple - refactorer le workflow de connexion |  |  |
| Exemple - refactorer le chargement des fournisseurs d'identité |  |  |
| Exemple - refactorer le changement de mot de passe |  |  |
| Exemple - refactorer la gestion multi-domaine |  |  |
| Autres refactorisations... |  |  |
| **Sous total complexité reprise des cas d'usages** |  | **0** |
| Packaging, intégration initiale |  |  |
| Déploiement / configuration / supervision |  |  |
| Tests d'intégration généraux |  |  |
| **Grand Total** |  | **0** |
