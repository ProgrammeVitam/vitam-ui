# Synthèse – Gestion des identités et authentification

## Contexte

L'objectif est de faire évoluer le système d'authentification afin de :

- supporter un IdP spécifique ou tout IdP compatible (OIDC/SAML) ;
- simplifier l'architecture d'authentification ;
- conserver les fonctionnalités métier existantes de Vitam UI ;
- préparer une architecture suffisamment ouverte pour différents modes de déploiement.

Les réflexions portent notamment sur le remplacement de CAS par Spring Security et sur le positionnement éventuel d'une solution IAM (par exemple Keycloak).

---

# Évolutions techniques envisagées

## Support des fournisseurs d'identité (IdP)

Deux niveaux de support sont envisagés :

- support d'un IdP spécifique ;
- support générique de tout IdP compatible OIDC ou SAML.

L'objectif est de disposer d'une couche d'abstraction permettant d'intégrer différents fournisseurs d'identité sans dépendance forte à une implémentation particulière.

---

## Gestion des utilisateurs

Plusieurs approches sont étudiées.

### Connecteur IdP

Mettre en place un connecteur permettant de changer le référentiel d'utilisateurs sans modifier l'application.

**Avantages :**

- indépendance vis-à-vis du fournisseur d'identité ;
- possibilité de remplacer la base utilisateur.

### Référentiel LDAP

Utiliser un annuaire LDAP externe, couplé à Spring Data, afin de conserver la maîtrise du référentiel utilisateur.

---

## Remplacement de CAS

L'hypothèse principale est de remplacer CAS par Spring Security afin de centraliser les mécanismes d'authentification.

Les besoins couverts seraient :

- authentification locale ;
- authentification via IdP OIDC ;
- authentification via IdP SAML ;
- émission de jetons OAuth2/JWT pour les échanges applicatifs.

---

# Fonctionnalités attendues

| Fonctionnalité | État / difficulté |
|----------------|------------------|
| Authentification locale | À conserver |
| Authentification OIDC | Support attendu |
| Authentification SAML | Support attendu |
| OAuth2 / JWT | Nécessaire pour les API |
| Gestion des utilisateurs | Nécessite un accès au référentiel utilisateur |
| Gestion des organisations | Fonction hybride entre métier et identité |
| Cycle de vie des comptes | Déjà pris en charge dans Vitam UI |
| Création de compte par email | Nécessite une gestion des mots de passe |
| OTP | Nécessite une implémentation locale ou un service externe |
| MFA | Nécessite une implémentation locale ou un service externe |
| Subrogation | Nécessite une ré-authentification forte |
| Multi-domaines / multi-organisations | Peu ou pas supporté nativement par les solutions IAM |

---

# Points d'attention

## Subrogation

La subrogation constitue un point sensible.

Une élévation de privilèges doit être précédée d'une vérification forte de l'identité de l'utilisateur (nouvelle authentification avant autorisation de la subrogation).

---

## Gestion des utilisateurs

Si une solution IAM devient le référentiel principal, elle prendra potentiellement en charge :

- la création des comptes ;
- la gestion des mots de passe ;
- l'activation et la désactivation des comptes ;
- les API d'administration (par exemple via les API Keycloak).

La répartition des responsabilités entre Vitam UI et l'IAM reste à définir.

---

## Gestion des droits

Lorsque l'identité provient d'un IdP externe, plusieurs questions restent ouvertes :

- où sont stockés les droits applicatifs ?
- comment associer une identité externe aux rôles et permissions internes ?
- comment maintenir cette correspondance dans le temps ?

---

## Gestion des organisations

La gestion des organisations dépasse le périmètre de l'authentification.

Elle comprend notamment :

- la création d'un administrateur ;
- la sélection d'un tenant ;
- l'association d'un ou plusieurs IdP ;
- l'administration des organisations.

Cette fonctionnalité se situe à l'interface entre les besoins métier et la gestion des identités.

---

# Scénarios étudiés

## Solution 1 — Intégration complète avec Keycloak

Le référentiel d'identité est entièrement délégué à Keycloak.

### Avantages

- solution IAM complète ;
- prise en charge native d'OIDC, SAML, MFA, OTP et fédération d'identités ;
- nombreuses fonctionnalités disponibles immédiatement.

### Inconvénients

- forte dépendance à Keycloak ;
- perte ou adaptation de certaines fonctionnalités métier (notamment le multi-domaine si l'on s'appuie uniquement sur OIDC) ;
- nécessité d'utiliser les interfaces d'authentification du fournisseur (mires de connexion, à l'image de FranceConnect).

---

## Solution 2 — IAM comme référentiel d'identité

L'IAM gère les identités tandis que l'application conserve la gestion des droits et des fonctionnalités métier.

### Avantages

- délégation de la gestion des identités ;
- conservation des spécificités métier.

### Points ouverts

- gestion des utilisateurs ;
- synchronisation entre identité et droits applicatifs ;
- association entre utilisateurs externes et organisations internes.

---

## Solution 3 — Spring Security comme socle

L'application conserve la maîtrise complète de l'authentification.

Spring Security assure :

- authentification locale ;
- authentification OIDC ;
- authentification SAML ;
- émission de JWT.

### Avantages

- indépendance vis-à-vis d'un IAM particulier ;
- meilleure maîtrise des fonctionnalités métier ;
- architecture plus flexible.

### Points à vérifier

- faisabilité technique de l'intégration générique d'IdP OIDC/SAML ;
- coût de développement des fonctionnalités avancées (MFA, OTP, gestion des mots de passe, récupération de compte, etc.).

---

# Comparaison synthétique

| Critère | Full Keycloak | IAM comme référentiel | Spring Security |
|----------|---------------|-----------------------|-----------------|
| Dépendance à un IAM | Forte | Moyenne | Faible |
| Authentification OIDC/SAML | Native | Native | À implémenter/configurer |
| Gestion des utilisateurs | IAM | Partagée | Application |
| Gestion des droits métier | Complexe | Application | Application |
| Multi-domaines | Limité selon les cas | À concevoir | Maîtrisé par l'application |
| Subrogation | À adapter | À adapter | Entièrement maîtrisée |
| Évolutivité | Moyenne | Bonne | Excellente |
| Effort de développement | Faible | Moyen | Élevé |

---

# Conclusion

Trois approches se dégagent :

- **Intégration complète avec un IAM (Keycloak)** : solution riche en fonctionnalités, mais fortement couplée et potentiellement limitante pour certaines fonctionnalités métier.
- **IAM comme référentiel d'identité** : compromis intéressant, qui nécessite de définir clairement la gestion des droits et des utilisateurs.
- **Spring Security comme socle** : solution offrant une maîtrise complète de l'architecture et des fonctionnalités métier, au prix d'un effort de développement plus important.

## Sujets restant à instruire

- stratégie de gestion du référentiel utilisateur ;
- prise en charge des organisations multi-domaines ;
- mécanisme de subrogation avec ré-authentification forte ;
- intégration générique d'IdP OIDC/SAML ;
- implémentation des fonctionnalités de sécurité avancées (OTP, MFA, gestion des mots de passe) ;
- répartition des responsabilités entre l'application et un éventuel IAM.
