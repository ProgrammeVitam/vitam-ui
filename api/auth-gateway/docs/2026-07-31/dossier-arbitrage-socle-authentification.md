# Dossier d'arbitrage — socle d'authentification VitamUI

*31 juillet 2026 — document de référence — COPIL, direction technique, équipe*

---

> ## Objet
>
> Ce document est le **document de référence unique** pour l'arbitrage du socle d'authentification.
> Il reprend, consolide et actualise les huit documents produits du 27 au 30 juillet 2026, qui sont
> conservés à titre historique et portent désormais un bandeau renvoyant ici.
>
> Il pose, dans l'ordre : **l'existant** (§2), **ce qu'aucun produit ne couvre** (§3), **les
> produits** (§4), **les trajectoires** (§5 et §6), **la charge** (§7), **les risques** (§8), puis
> **la décision proposée** (§10).
>
> ### La hiérarchie à respecter en le lisant
>
> **Les charges du §7 sont des estimations : aucune des trois trajectoires n'a été réalisée.** Les
> risques du §8 sont des **constats** — établis par test, par lecture de code, ou par la
> documentation de l'éditeur.
>
> **En cas de contradiction entre les deux, ce sont les constats qui doivent emporter la décision.**
> Une estimation se rediscute et se re-pose ; un mode de défaillance silencieux, une politique de
> maintenance ou un résultat de test négatif ne se re-posent pas.
>
> ### Périmètre analysé
>
> Branche `discovery_16332` — module `cas/cas-server` (Apereo CAS **7.0.10.1**, overlay WAR),
> module `api/api-iam`, module `api/auth-server` (travaux de faisabilité), configuration de
> déploiement `deployment/roles/vitamui/templates/cas-server/application.yml.j2`.
>
> Les faits produit (§4) ont été relevés en ligne les 29 et 30/07/2026 et portent leur source.
>
> ### Convention d'unité
>
> Ce document emploie **une seule unité de chiffrage : le point de charge** (§7). Les **points de
> complexité** sur échelle Fibonacci, qui ont servi aux travaux du 27 au 29/07, sont conservés en
> **annexe A** à titre historique. **Les deux ne sont pas convertibles l'un dans l'autre** et ne
> mesurent pas la même chose. Le mot « points » n'est jamais employé seul dans ce document.

---

## 1. Pourquoi ce dossier existe

Trois faits indépendants les uns des autres.

**Nous sommes sans correctifs de sécurité.** La version d'Apereo CAS exploitée en production
(7.0.10.1) est en fin de vie. L'éditeur ne publie plus rien pour elle, quelle que soit la gravité
d'une faille éventuelle.

**Le problème se reproduira.** L'éditeur assure **6 mois de support complet, puis 6 mois de
correctifs de sécurité seuls**, et écrit ne pas pouvoir offrir de version à support long. Rester
sur ce produit revient à accepter une montée de version obligatoire à chaque cycle. Le coût
récurrent de maintien est estimé entre **20 et 40 jours-homme par an**.

**Ce composant n'est pas planifiable.** Depuis l'origine, aucune intervention sur ce composant —
évolution fonctionnelle ou montée de version — n'a tenu dans un sprint de trois semaines.

> **Le seul étalon mesuré du dossier.** La dernière montée de version, CAS 6 → 7, a été
> **déclarée à 25 jours au ticket** et a **réellement coûté 50 à 60 jours**, l'écart correspondant
> à la recette et aux extras. Elle a de surcroît été réalisée **avec une application déjà migrée
> en exemple**, ce qui ne se reproduira pas.
>
> Ce chiffre commande deux choses dans ce document : le calibrage de la ligne de recette (§7.1) et
> le **taux d'erreur d'estimation observé sur ce composant — × 2 à × 2,4** (§8, réserve 2).
> *(Les documents antérieurs au 30/07 ne retiennent que les 25 jours du ticket.)*

Une brique dont chaque évolution déborde du cycle de développement ne peut être ni engagée en début
d'itération, ni découpée en incréments livrables, ni arbitrée à parité face à d'autres sujets. Elle
impose son rythme au projet.

---

## 2. Ce que porte le socle aujourd'hui

### 2.1 Répartition `cas/cas-server` / `api-iam`

La question instruite ici n'est pas « que sait faire CAS ? », mais **« qui fait quoi aujourd'hui ? »**.
Trois colonnes : ce que fournit le produit CAS lui-même, ce que nous avons écrit dans l'overlay, et
ce que porte le module de gestion des identités.

> ⚠️ **Périmètre : le système en production uniquement.** Les travaux de faisabilité menés sur
> `api/auth-server` ont ajouté à IAM **5 endpoints `/iam/v1/cas/*`** qui ne sont appelés par aucun
> code de `cas-server`. Ils sont **exclus de ce tableau** et identifiés séparément en §2.3.

**Légende** — ✅ natif · ⚙️ natif mais configuration non triviale · ❌ absent du produit · — sans objet

| # | Fonctionnalité | Produit CAS | Spécifique `cas/cas-server` | Part `api-iam` |
|---|---|:--:|---|---|
| 1 | Authentification login / mot de passe | ✅ | `LoginPwdAuthenticationHandler`, `UserPrincipalResolver` (515 l.) | **Vérification du secret** — `POST /cas/login`. CAS ne détient aucun mot de passe |
| 2 | Base d'identités | ⚙️ | — (*stateless* sur l'identité) | **Source de vérité** — Mongo `users`, `customers`, `groups`, `profiles`, `tenants` |
| 3 | Résolution HRD (e-mail → organisation + IdP) | ❌ | Orchestration : `DispatcherAction` (220 l.) | **Règles et données** — `patterns` sur `IdentityProvider`, appariement par `IdentityProviderHelper` (bibliothèque partagée `iam-commons`). **Le filtrage s'exécute dans `cas-server`** |
| 4 | Sélection d'organisation (même e-mail sur N organisations) | ❌ | `ListCustomersAction`, `CustomerSelectedAction`, `customerForm.html` | `GET /cas/customers` |
| 5 | Multi-domaine à suffixe partagé | ❌ | Consommation du résultat | **Modèle** — `patterns: List<String>` sur `IdentityProvider` |
| 6 | Webflow de login multi-étapes | ⚙️ | `VitamLoginWebflowConfigurer` (261 l.), `DispatcherAction`, `WebflowConfig`, 4 vues | — |
| 7 | Délégation **OIDC** entrante | ✅ (pac4j) | `ProvidersService`, `CustomDelegatedIdentityProviders`, `CustomDelegatedClientAuthenticationAction` — rechargement dynamique (1 min) | **Registre des IdP** — API standard `identity-providers`, stockage Mongo |
| 8 | Délégation **SAML 2** entrante | ✅ (pac4j + OpenSAML) | Idem, mécanisme mutualisé | `keystoreBase64`, `keystorePassword`, `idpMetadata` en base |
| 9 | Authentification par certificat **X.509** | ✅ | `CustomRequestHeaderX509CertificateExtractor`, `CertificateParser`, `X509AttributeMapping`, `X509CasDelegatingWebflowEventResolver` | Résolution de l'utilisateur via `GET /cas/users` |
| 10 | MFA — OTP par SMS *(activation conditionnelle, non maintenue)* | ✅ (`simple-mfa` + `sms-smsmode`) | `VitamMfaWebflowConfigurer`, `CustomSendTokenAction`, `CheckMfaTokenAction` | Numéro de mobile porté par l'utilisateur |
| 11 | MFA — TOTP / WebAuthn / passkeys | ⚙️ | ❌ non implémenté | — |
| 12 | **Subrogation** compte générique | ⚙️ (`surrogate-authentication`) | `IamSurrogateAuthenticationService`, `CustomSurrogateInitialAuthenticationAction`, `InitializeSubrogationAction` | `GET /cas/subrogations` |
| 13 | **Subrogation** nominative **avec validation** | ❌ | Orchestration webflow, propagation `superUserId` | **Processus métier complet** — entité `Subrogation` (`CREATED` → `ACCEPTED`). **CAS ne lit que les subrogations déjà acceptées** |
| 14 | Réinitialisation de mot de passe par e-mail | ✅ (`pm-webflow`) | `IamPasswordManagementService`, `ResetPasswordController`, `I18NSendPasswordResetInstructionsAction` — **hack** `UserLoginModel` encodé en JSON dans `username` | `POST /cas/password/change` |
| 15 | Changement de mot de passe forcé | ✅ | `TriggerChangePasswordAction` + réécriture du `ticketGrantingTicketCheck` | Statut et date d'expiration du compte |
| 16 | Politique de mot de passe (ANSSI / custom) | ⚙️ | `InitPasswordConstraintsConfiguration`, messages fr/en/de | Contraintes servies par IAM ; validateur mutualisé `commons/commons-security` |
| 17 | Historique des mots de passe | ⚙️ | — | `max-old-password`, `CasService.updatePassword` |
| 18 | Provisioning **JIT** après fédération | ⚙️ | Déclenchement après authentification déléguée | `GET /cas/users/provisioning` — `autoProvisioningEnabled`, `defaultGroupId`. **`cas-server` ne référence aucun code de JIT** |
| 19 | Provisioning depuis un annuaire externe | ❌ | — | `GET /cas/users/provisioning` + `ProvisioningClient` par IdP |
| 20 | SSO inter-applications (8 SPA) | ✅ (TGT + cookie TGC) | `DynamicTicketGrantingTicketFactory` — TTL variable | Type de compte (`NOMINATIVE` / `GENERIC`) |
| 21 | Émission du **jeton applicatif `TOK-<UUID>`** | ❌ | `CustomOAuth20DefaultAccessTokenFactory` | **Émission et persistance** — `GET /cas/users/provisioning` (`embedded=AUTHTOKEN`), collection `tokens` |
| 22 | Serveur OIDC pour les 8 SPA | ✅ (`support-oidc`) | `CustomOidcCasClientRedirectActionBuilder`, `CustomOidcRevocationEndpointController`, `CustomCorsProcessor` | — |
| 23 | Registre des clients / services persistant | ✅ (`mongo-service-registry`) | Configuration ; collection `services` | — |
| 24 | Logout global / Single Logout | ✅ | `TerminateApiSessionAction`, `CustomDelegatedAuthenticationClientLogoutAction`, flag `propagateLogout` | `GET /cas/logout` — invalidation des jetons, purge de la subrogation |
| 25 | Anti-brute force / throttling | ✅ | `cas.authn.throttle.*` (seuil 2 sur 3 s) | Compteur `nbFailedAttempts`, seuil, `BLOCKED` |
| 26 | Blocage / désactivation de compte | ⚙️ | Contrôle dans `DispatcherAction`, vue `casAccountDisabledView` | `UserStatusEnum`, `checkStatus` |
| 27 | Thème & i18n de la mire (fr/en/de) | ⚙️ (Thymeleaf) | 20 fichiers surchargés (1 067 l.) + `overriden_messages_{fr,en,de}.properties` | — |
| 28 | Journalisation des connexions (logbook `EXT_VITAMUI_*`) | ⚙️ (Inspektr) | — | **Écriture des événements** — `IamLogbookService.loginEvent` |
| 29 | Modèle d'habilitations (profils, groupes, tenants, contrats) | ❌ | Portage dans le principal | **Entièrement IAM** — hors périmètre de l'IdP dans les trois trajectoires |
| 30 | Console d'administration des utilisateurs | ❌ | — | IAM + SPA `identity` / `identityadmin` |

### 2.2 Volumétrie du spécifique

| Élément | Volume |
|---|---:|
| Classes Java `cas/cas-server/src/main/java` | **49 classes / 7 737 lignes** |
| Fichiers Thymeleaf surchargés | **20 fichiers / 1 067 lignes** (19 vues + 1 gabarit) |
| Exclusions Maven dans `cas-server/pom.xml` | **25** |
| `CasController` + `CasService` (côté IAM, sur `develop`) | **1 049 lignes** |
| Endpoints IAM dédiés **consommés par `cas-server`** | **7** |
| Points d'extension accrochés à des internes CAS / pac4j | **28** |

> **Note — 7 endpoints, et non 12 ou 13.** Les documents du 27 et du 28/07 annoncent « 13 endpoints »
> en prose et en énumèrent 12 en annexe. Les deux chiffres sont faux, pour deux raisons distinctes.
>
> **1. Le décompte brut est de 12, pas 13.** `CasController` porte 12 annotations de *mapping* et
> `RestApi` 12 constantes `CAS_*_PATH`.
>
> **2. Surtout, 5 de ces 12 endpoints n'existent que depuis les travaux de faisabilité.** Le
> `git diff develop...HEAD` sur `RestApi.java` montre l'ajout de `CAS_TOKENS_PATH`, `CAS_HRD_PATH`,
> `CAS_SUBROGATION_VALIDATE_PATH`, `CAS_IDP_PATH` et `CAS_USERS_JIT_PATH`. **`cas-server` n'en
> appelle aucun.**
>
> Le socle en production s'appuie sur **7 endpoints / 1 049 lignes**. Les travaux de faisabilité y
> ont ajouté **+405 lignes** et **+5 endpoints** — ce qui donne les 1 454 lignes citées dans les
> documents antérieurs. **Ce total mélange l'existant et le prototype**, il ne mesure pas le
> couplage CAS ↔ IAM. Cette correction est corroborée par une analyse indépendante de la branche
> `develop` (annexe C).

### 2.3 Les 7 points d'échange du socle actuel

| Endpoint | Rôle |
|---|---|
| `POST /cas/login` | Vérification du mot de passe, compteur d'échecs, statut de compte, événement de connexion |
| `POST /cas/password/change` | Changement de mot de passe + historique |
| `GET /cas/users?email=` | Recherche multi-organisation (écran de sélection) |
| `GET /cas/users/provisioning` | Lookup, provisioning à la volée, **et émission du `TOK-<UUID>`** |
| `GET /cas/subrogations` | Subrogations **déjà acceptées** pour un super-utilisateur |
| `GET /cas/logout` | Invalidation du jeton et purge de la subrogation en cours |
| `GET /cas/customers` | Résolution des organisations |

> **Les 5 endpoints ajoutés par les travaux de faisabilité** — `GET /cas/hrd`, `POST /cas/tokens`,
> `POST /cas/subrogations/validate`, `GET /cas/idp/{id}`, `POST /cas/users/jit`. Appelés
> **uniquement** par `api/auth-server`. Les faire figurer dans la répartition du socle actuel
> donnerait à la trajectoire C un existant qu'elle n'a pas hérité : **elle l'a construit.**

### 2.4 Le fait structurant : le métier est déjà hors de CAS

**CAS ne détient pas le métier. Il l'orchestre.** Ce qui vit dans `cas/cas-server` est une séquence
d'états de webflow ; ce qui décide est dans `api-iam`. Vérifié fonction par fonction :

| Fonction | Où vit réellement la décision |
|---|---|
| Authentification mot de passe | **IAM** — `POST /cas/login`. CAS ne détient aucun mot de passe |
| Base d'identités | **IAM** — Mongo. CAS est sans état sur l'identité |
| Résolution d'organisation (HRD) | **IAM** — `patterns` sur `IdentityProvider`, bibliothèque partagée |
| Subrogation | **IAM** — subrogations et acceptation |
| Jeton d'accès VitamUI | **IAM** — émission du `TOK-<UUID>` |
| Statut de compte, verrouillage | **IAM** — compteur, seuil, `BLOCKED`, audit, dans `CasController.login()` |
| Politique et historique de mot de passe | **IAM** — `maxOldPassword`, `passwordRevocationDelay` ; validateur en bibliothèque partagée |
| Journalisation des connexions | **IAM** — `IamLogbookService.loginEvent` |
| Habilitations | **IAM** — jamais dans CAS |
| Référentiel des IdP, organisations, subrogations | **IAM** — administré par l'application Identity |

**Trois exceptions, à ne pas gommer.**

1. **La résolution HRD** — les données et les règles vivent hors de CAS, mais dans la bibliothèque
   *partagée* `iam-commons`, et **le filtrage s'exécute dans `cas-server`**.
2. **La décision d'expiration du mot de passe** — `mustChangePassword()` est dans
   `LoginPwdAuthenticationHandler`, côté CAS ; la donnée `passwordExpirationDate` est en IAM.
3. **Le rechargement à chaud des fournisseurs d'identité** — `ProvidersService` recharge la liste
   depuis IAM toutes les 60 secondes (`@Scheduled`) et reconstruit les clients pac4j. Conséquence
   d'exploitation réelle : **on ajoute ou modifie un IdP sans redémarrer le serveur
   d'authentification.**

**Ce que cela change pour la décision.** Les fonctions ne sont pas toutes « à reprendre » : pour la
plupart, **le moteur est déjà écrit et ne bouge dans aucune trajectoire**. Ce qui se déplace, c'est
le **point d'orchestration**. C'est pourquoi les travaux de faisabilité ont pu valider sept cas
d'usage en réutilisant les points d'accès existants — le métier était là, il manquait le contrat
d'accès.

**La contrepartie, à ne pas taire.** Ce constat coupe dans les deux sens. Il réduit le coût de C,
mais il explique aussi pourquoi **aucun produit sur étagère ne se pliera au modèle** : ce n'est pas
CAS qui est mal utilisé, c'est le modèle VitamUI qui n'est pas celui d'un serveur d'authentification
sur étagère.

---

## 3. Les cinq fonctions qu'aucun produit sur étagère ne couvre

C'est le cœur de la contrainte d'iso-fonctionnel.

| Fonctionnalité | Pourquoi aucun produit ne la couvre |
|---|---|
| Se connecter avec la même adresse e-mail dans **plusieurs organisations** | Les standards du marché supposent qu'une adresse identifie une seule personne dans un seul périmètre |
| Plusieurs organisations **partageant un même domaine** de messagerie | **Vérifié par test le 27/07** : le modèle *Organizations* de Keycloak n'autorise pas deux organisations sur un même domaine |
| **Subrogation avec accord** de la personne concernée | C'est un processus métier — demande, acceptation, traçabilité — et non un mécanisme d'authentification. L'*impersonation* des produits est un acte administratif unilatéral |
| **Jeton d'accès applicatif propriétaire** `TOK-<UUID>` | Format spécifique à VitamUI, lu par une dizaine de composants de la plateforme |
| **Modèle d'habilitations VitamUI** | Profils, groupes, organisations, contrats d'accès : propres au produit |

**Conséquence directe** : quelle que soit la trajectoire retenue, ces fonctionnalités resteront du
développement spécifique. **Le scénario « on remplace le produit et c'est réglé » n'existe pas.**

*Le modèle d'habilitations fait exception dans le raisonnement qui suit : il reste dans `api-iam`
dans les trois trajectoires, et ne constitue donc pas un poste d'arbitrage.*

**Un sixième comportement, à ne pas perdre de vue.** Un e-mail inconnu dont le **domaine**
correspond à un IdP est routé silencieusement vers la page de connexion de ce fournisseur : le
parcours **ne révèle jamais si le compte existe**. Ce comportement de non-divulgation est facile à
casser par inadvertance en réécrivant le parcours d'entrée, et une régression sur ce point est un
défaut de sécurité, pas un défaut d'ergonomie. **À inscrire au plan de recette des trois
trajectoires.**

---

## 4. Comparatif produit — CAS et Keycloak

### 4.1 État des deux produits

Faits relevés en ligne les 29 et 30/07/2026 ; sources en annexe C.

| Fait |
|---|
| **CAS 8.0.0 est disponible depuis le 18/07/2026** |
| **Aucune ligne documentaire `8.0.x` n'est publiée** — le sélecteur de version ne propose que `7.3.x` et `Development` |
| **8.0.1 n'est pas publiée** (jalon dû le 14/08/2026) ; **8.1.0-RC1 est à 94 %**, échéance 21/08/2026 |
| **Politique de maintenance : 6 mois de support complet, puis 6 mois de correctifs de sécurité seuls** |
| **Absence de LTS assumée** : *« the CAS project can not offer LTS releases in a practical and sustainable sense »* |
| **7.3.x est en correctifs de sécurité seuls depuis le 30/06/2026, EOL au 31/12/2026.** Toute version absente du tableau de maintenance est déclarée EOL — ce qui inclut **notre 7.0.10.1**, ainsi que 7.1.x et 7.2.x |
| **CAS 7.1, 7.2 et 7.3 requièrent JDK 21. Seule CAS 8.0 impose JDK 25.** Le monorepo VitamUI est en Java 21 |
| **Keycloak 26.7.0 publiée le 09/07/2026** — la ligne **26.x** est ouverte depuis le **04/10/2024** |

**Une correction au dossier.** Les documents antérieurs annonçaient « 12 mois de support ». C'est
exact comme durée totale, mais **6 mois seulement** sont en support complet. Et l'absence de LTS
n'est plus une inférence de notre part : c'est une position affichée par l'éditeur, citable telle
quelle.

**Un contraste de rythme, mesurable.** Keycloak tient sa ligne majeure **26.x depuis 21 mois**. Sur
la même période, CAS a imposé l'enchaînement **7.0 → 7.1 → 7.2 → 7.3 → 8.0**, et prépare déjà 8.1.
Ce n'est pas un jugement sur la qualité des deux produits : c'est une différence de contrat de
maintenance, et elle est structurelle.

### 4.2 Fonctionnalités des deux produits

Comparatif **produit à produit**, indépendamment de VitamUI.

**Légende** — ✅ natif · ⚙️ natif mais configuration non triviale · ⚠️ partiel ou sémantique
différente · ❌ absent

| Fonction | CAS 8.0 | Keycloak 26.7 | Niveau de preuve |
|---|:--:|:--:|---|
| Protocole CAS (`/serviceValidate`, proxy tickets) | ✅ | ❌ | doc éditeur |
| Fournisseur OIDC / OAuth 2 | ✅ | ✅ | doc éditeur |
| Fournisseur SAML 2 (IdP) | ✅ | ✅ | doc éditeur |
| Délégation OIDC entrante (*identity brokering*) | ✅ | ✅ | doc éditeur |
| Délégation SAML 2 entrante | ✅ | ✅ | doc éditeur |
| Authentification X.509 | ✅ | ✅ | doc éditeur |
| MFA — TOTP | ⚙️ | ✅ | doc éditeur |
| MFA — WebAuthn / passkeys | ⚙️ | ✅ | doc éditeur |
| MFA — **OTP par SMS** | ✅ (10 fournisseurs) | ⚠️ **non natif** — `Authenticator SPI`, implémentations tierces courantes | doc éditeur |
| MFA — Duo, FIDO2 | ✅ | ⚠️ (pas de Duo natif) | doc éditeur |
| Gestion du mot de passe (reset, changement forcé) | ✅ | ✅ | doc éditeur |
| Politique de mot de passe déclarative | ⚙️ (regex) | ✅ (politiques nommées) | doc éditeur |
| Historique des mots de passe | ⚙️ | ✅ | doc éditeur |
| **Subrogation / impersonation** | ✅ (`surrogate-*`) | ⚠️ **acte administratif unilatéral**, sans consentement de la cible | doc éditeur |
| Provisioning JIT après fédération | ⚙️ | ✅ (*first broker login*) | doc éditeur |
| Stockage d'identités externe | ⚙️ (LDAP, JDBC, REST) | ✅ (**User Storage SPI**) | doc éditeur |
| Registre de services / clients persistant | ✅ | ✅ | doc éditeur |
| Console d'administration | ❌ | ✅ | doc éditeur |
| API d'administration | ⚙️ | ✅ (Admin REST complète) | doc éditeur |
| SCIM | ⚙️ | ✅ (26.7, *preview*) | doc éditeur |
| Anti-brute force | ✅ | ✅ | doc éditeur |
| Audit / événements extensibles | ⚙️ (Inspektr) | ⚙️ (Event Listener SPI) | doc éditeur |
| Thème & i18n de la mire | ⚙️ (Thymeleaf) | ⚙️ (FreeMarker, *login theme v2*) | doc éditeur |
| **Notion d'organisation** | ❌ | ⚠️ *Organizations* — **N organisations ne peuvent pas partager un domaine** | **testé le 27/07, négatif** |
| Modèle de déploiement | ⚠️ **overlay WAR à recompiler** | ✅ distribution binaire versionnée | doc éditeur |
| **Modèle d'extension** | ⚠️ **écrasement de beans internes** | ✅ **SPI déclarées et documentées** | analyse de code, §4.3 |
| **Contrat documentaire sur l'extension** | ❌ | ✅ | analyse, §4.3 |
| Cadence de version majeure | cycle imposé, pas de LTS | ligne 26.x ouverte depuis 21 mois | vérifié 29/07 |

### 4.3 La ligne décisive : comment on étend ces deux produits

Les deux avant-dernières lignes du tableau ne sont pas des fonctionnalités. Ce sont les deux qui
pèsent le plus sur le coût réel, et elles opposent les produits **par nature**, pas par degré.

**CAS s'étend par collision de noms de beans.** Les points d'extension ne passent pas par une
interface déclarée : ils passent par le **nom des méthodes `@Bean`**, qui doit correspondre
exactement à un nom de bean interne du produit.

```java
// cas/cas-server/.../config/AppConfig.java
@Bean public PrincipalResolver defaultPrincipalResolver(...)              // ← nom interne CAS
@Bean public TicketGrantingTicketFactory defaultTicketGrantingTicketFactory(...)
@Bean public OAuth20AccessTokenFactory defaultAccessTokenFactory(...)
@Bean public PasswordManagementService passwordChangeService(...)
@Bean public DelegatedIdentityProviders delegatedIdentityProviders(...)
```

Aucun `@ConditionalOnMissingBean`, aucun `@Primary`, aucun `@Bean(name = …)` explicite. Et pour que
le mécanisme fonctionne, la protection de Spring contre les définitions dupliquées doit être
**désactivée globalement** :

```properties
# cas/cas-server/src/main/resources/application.properties:62
spring.main.allow-bean-definition-overriding=true
```

CAS documente exhaustivement ses milliers de propriétés `cas.*`, et **pas sa surface d'extension
Java**. Nos 7 737 lignes reposent sur **28 classes internes** — une surface que le projet ne
s'engage ni à documenter, ni à stabiliser.

**Keycloak s'étend par SPI.** `Authenticator SPI`, `User Storage SPI`, `Event Listener SPI` : des
interfaces déclarées, nommées, versionnées et documentées. Une SPI qui change le fait visiblement.

**Pourquoi cet écart compte plus que les lignes fonctionnelles.** Il ne change pas *ce qu'on peut
faire* avec chaque produit — il change **ce qui se passe quand le produit monte de version**. Une
SPI modifiée casse la compilation, bruyamment et tout de suite. Un bean renommé ne casse rien du
tout : c'est le mode de défaillance silencieux du §8, réserve 5.

**Ce que cet écart ne dit pas.** Il ne désigne pas Keycloak comme la bonne cible pour VitamUI. Il
dit que **si le choix se réduisait à « quel produit plier ? », Keycloak serait le meilleur
candidat**. C'est précisément ce qui rend le résultat du test du 27/07 aussi lourd de conséquences :
**le meilleur candidat à la personnalisation ne couvre pas davantage le modèle VitamUI que celui que
nous exploitons.**

---

## 5. Les trois trajectoires

| | **A — Montée de version CAS** | **B — Transposition Keycloak** | **C — Spring Authorization Server** |
|---|---|---|---|
| Nature | rester sur le produit, changer de version | changer de produit sur étagère | internaliser le composant |
| Cible retenue | **ligne 7.3.x** (JDK 21) | Keycloak 26.x | module `api/auth-server` |
| Ce qu'on garde | tout — iso-fonctionnel par construction | le produit, pas le parcours | le modèle métier VitamUI |
| Ce qu'on écrit | adaptation des 28 points d'extension | SPI Keycloak pour les spécificités | le serveur d'autorisation |
| Ce que ça achète | **du support, jusqu'au 31/12/2026** | une cible durable, sur un produit tiers | une cible durable, sur du code maîtrisé |
| **Migration de données d'identités** | **aucune** | **oui — question non tranchée** | **aucune** |
| Risque de régression sur l'existant | **oui** — le composant de production est modifié en place | oui — il est remplacé | **non** — module distinct, bascule réversible |
| Éditeur | Apereo | Red Hat / CNCF | nous |
| Statut proposé | **mesure conservatoire** (§10.2) | **écarté** (§10.1) | **trajectoire cible** (§10.3) |

---

## 6. Matrice fonctionnelle macro

Quinze fonctions macro — le niveau du COPIL, pas celui de l'implémentation. Le détail à 30 lignes
est au §2.1.

**Légende** — ✅ acquis, sans travail · 🔧 à porter ou reconfigurer, sans risque fonctionnel ·
🔨 à développer · ⚠️ couvert mais **dégradé ou de sémantique différente** · ❌ **non couvert —
fonction abandonnée**

| # | Fonction macro | Aujourd'hui | A — CAS 7.3 | B — Keycloak 26 | C — SAS |
|---|---|:--:|:--:|:--:|:--:|
| 1 | Authentification identifiant / mot de passe sur la base VitamUI | ✅ | 🔧 | 🔨 | ✅ |
| 2 | Parcours d'entrée multi-organisation (e-mail → organisation) | ✅ | 🔧 | ⚠️ 🔨 | ✅ |
| 3 | Fédération entrante OIDC / SAML 2 | ✅ | 🔧 | ✅ | ✅ |
| 4 | Provisioning à la volée des utilisateurs fédérés | ✅ | 🔧 | ✅ | ✅ |
| 5 | Subrogation **validée** (avec consentement de la cible) | ✅ | 🔧 | ⚠️ 🔨 | ✅ |
| 6 | Jeton d'accès VitamUI, SSO et déconnexion globale | ✅ | 🔧 | 🔨 | ⚠️ |
| 7 | Habilitations VitamUI (tenants, profils, groupes) | ✅ | ✅ | ⚠️ | ✅ |
| 8 | Authentification par **certificat client X.509** | ✅ | 🔧 | 🔧 | ❌ |
| 9 | **MFA sur la base d'utilisateurs interne** | ✅ | 🔧 | 🔧 | ❌ |
| 10 | Gestion du mot de passe (politique, expiration, réinitialisation) | ✅ | 🔧 | ✅ | ✅ 🔨 ¹ |
| 11 | Protection anti-force brute / verrouillage de compte | ✅ | 🔧 | ✅ | ✅ 🔧 ² |
| 12 | Mire de connexion — thème, i18n, ergonomie | ✅ | 🔧 | ⚠️ 🔨 | 🔨 |
| 13 | **Journalisation des connexions** (logbook VitamUI) | ✅ | 🔧 | 🔧 | ✅ ³ |
| 14 | **Administration à chaud des fournisseurs d'identité**, sans redémarrage | ✅ | 🔧 | ✅ | 🔨 ⁴ |
| 15 | Administration des organisations, IdP et subrogations (application Identity) | ✅ | ✅ | ⚠️ | ✅ |

**¹ Mot de passe en trajectoire C — une partie est acquise, mais pas la totalité.**

| Composant | Où il vit | Acquis en C ? |
|---|---|---|
| Historique des anciens mots de passe | **IAM** — `maxOldPassword`, `saveCurrentPasswordInOldPasswords` | **oui** |
| Délai de révocation | **IAM** — `passwordRevocationDelay` sur `Customer` | **oui** |
| Changement effectif | **IAM** — `POST /cas/password/change` | **oui** |
| Validateur de complexité | **bibliothèque partagée** `commons-security`, déclaré en bean **des deux côtés** | **oui** — déjà câblé en IAM |
| **Décision d'expiration** | **CAS** — `mustChangePassword()` ; la donnée est en IAM, la règle est en CAS | **non** — règle courte à réimplémenter |
| **Parcours de réinitialisation** | **CAS** — jeton transitoire, e-mail localisé, écrans (≈ 840 l.) | **non** — c'est le vrai reste à faire |

Ce qui reste à écrire en C est le **parcours**, pas le moteur de politique.

**² Anti-force brute en C — déjà acquis pour l'essentiel.** Le compteur de tentatives, le seuil, le
passage en `BLOCKED`, la fenêtre de réarmement et l'événement d'audit sont **implémentés dans IAM**,
dans `CasController.login()`. Ils s'exécutent à **chaque appel de `POST /cas/login`** — y compris
quand l'appelant est le serveur d'autorisation. **C en hérite sans écrire une ligne.** Reste la
limitation de débit par requête, aujourd'hui native CAS, qui relève de l'infrastructure.

**³ Journalisation des connexions — acquise, avec une lacune préexistante.** L'événement est produit
par IAM (`IamLogbookService.loginEvent`), appelé depuis `CasController.login()` en succès comme en
échec. **Mais `loginEvent` n'est appelé que là** : les connexions par fédération OIDC et SAML, qui
passent par `getUser`, **ne produisent aucun événement de connexion aujourd'hui**. C'est une lacune
du socle actuel, pas une perte de C — signalée ici parce qu'une revue de sécurité la relèvera,
quelle que soit la trajectoire.

**⁴ Administration à chaud des IdP — à reproduire en C.** Capacité d'exploitation réelle (§2.4),
absente des grilles antérieures, ni difficile ni volumineuse, mais qui ne s'obtient pas
gratuitement — et dont l'oubli se découvrirait en exploitation, pas en recette.

### 6.1 Ce que ce tableau fait apparaître

**La colonne A ne comporte aucun ❌ et seulement deux ✅** — les habilitations et l'administration
fonctionnelle, qui vivent toutes deux hors de CAS. Ce n'est pas un hasard, c'est sa définition :
**une montée de version n'apporte aucune fonction et n'en retire aucune.** Treize cases sur quinze
sont du travail d'adaptation à coût non nul et à bénéfice fonctionnel nul. Ce que A achète n'est pas
dans ce tableau — c'est du support éditeur, et il est borné au 31/12/2026.

**La colonne B porte quatre ⚠️, dont trois sur le parcours d'entrée.** Ce sont les fonctions où le
produit couvre le besoin *avec une sémantique différente de la nôtre*. **Un ⚠️ coûte plus cher qu'un
🔨 : il faut d'abord défaire le comportement natif.**

**La colonne C porte deux ❌, et ce sont les seuls du tableau.** X.509 et MFA interne sont
**abandonnés**, non reportés. C'est le seul endroit du dossier où une trajectoire retire une
fonction au lieu d'en changer l'implémentation. Le §9 lui est consacré.

### 6.2 Ce que cette matrice ne peut pas montrer

> ⚠️ **Le poste le plus lourd de la trajectoire C n'apparaît dans aucune ligne ci-dessus.**
>
> La **sécurisation du canal entre le serveur d'autorisation et IAM** est le **premier poste de
> toute la matrice de charge** (§7.2). Elle est absente de ce tableau parce qu'elle **ne correspond
> à aucune fonction visible par l'utilisateur** : rien n'est ajouté, rien n'est retiré, le service
> rendu est identique avant et après.
>
> Elle est pourtant **bloquante avant toute mise en service**. Lire le §6 sans le §7 conduirait à
> conclure que la trajectoire C ne coûte presque rien.
>
> Deux autres chantiers sont dans le même cas : le **découplage** du parcours de connexion (§12) et
> la **recette end-to-end**.
>
> **Une matrice fonctionnelle mesure le service rendu, pas le travail à faire.** Les deux ne se
> recouvrent pas, et l'écart est ici la ligne la plus chère du dossier.

---

## 7. Charge estimée

### 7.1 Méthode et statut des chiffres

**Unité : le point de charge.** Ce n'est pas la conversion des points de complexité de l'annexe A —
les deux ne sont pas convertibles. Ce sont **deux mesures indépendantes**, et le §7.4 signale
explicitement là où elles divergent.

**Comment ces nombres sont construits.** Trois opérations, dans cet ordre :

1. **Les totaux de développement viennent de l'équipe**, posés le 30/07 : hors recette, 35–75 pour
   A, 55–105 pour B, 35–85 pour C.
2. **La ligne de recette est recalée sur le seul précédent mesuré** (§1). Sur CAS 6 → 7, la part
   « recette et extras » a représenté **25 à 35 jours**, là où les grilles antérieures budgétaient
   un forfait de 15 dans les trois colonnes. C'est cette valeur mesurée qui est retenue, et non le
   forfait.
3. **L'ensemble est rebasé d'un facteur 0,7**, uniformément, sur décision de l'équipe.

> ⚠️ **Trois réserves sur la ligne de recette.**
>
> **Le volume.** Recetter quinze fonctions sur six protocoles avec de vrais fournisseurs d'identité
> n'est tenable dans un forfait que s'il s'agit de recette *pure*, hors correction des défauts
> trouvés. Le retour d'expérience du 24/07 recense **13 anomalies sur les seuls OIDC et SAML**.
>
> **L'uniformité.** Elle est conservée ici faute d'élément permettant de la lever, mais elle n'est
> pas juste : **A revalide un comportement qui fonctionnait la veille — on y cherche des
> régressions. C éprouve un comportement qui n'a jamais existé — on y cherche des défauts.** Ce
> n'est pas le même travail. Une valeur identique dans les trois colonnes reste une commodité de
> présentation.
>
> **Le précédent lui-même.** Il portait sur une montée de version, donc sur un travail de type A. Le
> transposer à B et C est une hypothèse, pas une mesure.

> ⚠️ **Le taux d'erreur d'estimation observé sur ce composant est de × 2 à × 2,4.** Ce n'est pas une
> opinion sur la rigueur de l'équipe : c'est ce qu'a produit **le même processus d'estimation que
> celui qui a produit ces fourchettes**. Rien n'indique qu'il se soit amélioré depuis. **Cet
> avertissement vaut pour les trois colonnes.**

### 7.2 Synthèse

| | **A — CAS 7.3** | **B — Keycloak 26** | **C — SAS** |
|---|---:|---:|---:|
| **Développement** | 25 – 53 | 39 – 74 | 25 – 60 |
| **Recette et extras** | 17 – 24 | 17 – 24 | 17 – 24 |
| **TOTAL** | **42 – 77** | **56 – 98** | **42 – 84** |
| Effort relatif au plancher | **1 ×** | 1,3 × | **1 ×** |
| Amplitude de la fourchette | × 1,8 | × 1,8 | × 2,0 |
| Risque de faisabilité | non — chemin déjà parcouru | **oui, identifié par test** | **levé** sur 7 cas d'usage |
| Iso-ergonomie de la mire | acquise | **proche, non identique** | maîtrisée (code interne) |
| Durée de vie de l'investissement | **bornée au 31/12/2026** | durable | durable |

**Deux lectures s'imposent, et elles ne disent pas la même chose.**

**A et C ne se départagent pas sur le coût.** Elles sont à égalité au plancher, et C a un plafond
plus haut. **Le critère de décision se déplace donc sur la durée de vie de l'investissement et sur
les risques**, pas sur la charge. C'est exactement ce que conclut le §10.

**Le chiffre de A porte une réserve dans le sens de la baisse.** Ces 25–53 chiffrent une montée de
version **majeure**, telle que posée par l'équipe pour CAS 8.0. La cible retenue au §10.2 est la
**ligne 7.3.x**, qui supprime le saut de JDK et le saut Spring Boot : elle se situe **au plancher de
la colonne, voire en deçà**. Elle n'est pas chiffrée séparément faute de cadrage. À reposer une fois
la cible confirmée.

### 7.3 Ventilation par fonction macro

> ⚠️ **À lire avant le tableau — d'où viennent ces nombres.**
>
> **L'équipe a posé trois totaux**, pas quarante-cinq valeurs de détail. La ventilation ci-dessous
> est une **proposition de décomposition**, construite à partir de la matrice du §6, du volume de
> code par domaine et des constats de code établis les 29 et 30/07.
>
> **Elle est donc contrainte, pas mesurée** : chaque colonne somme exactement au total de
> développement du §7.2. Sa valeur est de montrer **où va la charge**, pas de fixer un prix par
> fonction. Chaque ligne est à re-poser en atelier.
>
> Les fourchettes basses **ne sont pas atteignables ensemble** sur toutes les lignes — le plancher
> d'une colonne suppose que tout se passe bien partout, ce qui n'arrive pas. **Lire les colonnes,
> pas les cellules isolées.**

Les cases à `0` signifient « rien à faire dans cette trajectoire », et non « négligeable ».

| # | Fonction macro | **A — CAS** | **B — Keycloak** | **C — SAS** |
|---|---|---:|---:|---:|
| — | **Socle et plateforme** — montée de dépendances (A), déploiement du produit et des realms (B), plateforme déjà en place (C) | 6 – 11 | 6 – 10 | **0** |
| 1 | Authentification identifiant / mot de passe | 1 – 3 | 6 – 10 | **0** |
| 2 | Parcours d'entrée multi-organisation | 2 – 4 | **6 – 11** | **0** |
| 3 | Fédération entrante OIDC / SAML 2 | 4 – 7 | 1 – 3 | 0 – 1 |
| 4 | Provisioning à la volée des utilisateurs fédérés | 1 – 1 | 1 – 1 | **0** |
| 5 | Subrogation validée | 2 – 4 | 4 – 8 | 1 – 1 |
| 6 | Jeton d'accès VitamUI, SSO et déconnexion globale | 2 – 4 | 4 – 7 | **4 – 7** |
| 7 | Habilitations VitamUI | **0** | 2 – 3 | **0** |
| 8 | Certificat client X.509 | 1 – 3 | 1 – 3 | **0** *(abandonné)* |
| 9 | MFA sur la base interne | 1 – 3 | 1 – 2 | **0** *(abandonné)* |
| 10 | Gestion du mot de passe | 2 – 4 | 2 – 2 | 3 – 6 |
| 11 | Anti-force brute / verrouillage de compte | 1 – 1 | 0 – 1 | 1 – 1 |
| 12 | Mire de connexion — thème, i18n, ergonomie | 1 – 2 | 3 – 6 | **5 – 12** |
| 13 | Journalisation des connexions | 0 – 1 | 1 – 2 | 0 – 1 |
| 14 | Administration à chaud des fournisseurs d'identité | 1 – 2 | 0 – 1 | 1 – 3 |
| 15 | Administration des organisations, IdP et subrogations | **0** | 0 – 1 | **0** |
| — | **Sécurisation du canal SAS ↔ IAM et dette du prototype** | — | — | **6 – 17** |
| — | **Registre de clients et persistance des autorisations** | — | — | 1 – 4 |
| — | **Packaging, déploiement, supervision** | 0 – 3 | 1 – 3 | 3 – 7 |
| | **Total développement** | **25 – 53** | **39 – 74** | **25 – 60** |
| | **Recette et extras** | 17 – 24 | 17 – 24 | 17 – 24 |
| | **TOTAL** | **42 – 77** | **56 – 98** | **42 – 84** |

### 7.4 Les cinq lignes qui décident du total

Sur quarante-cinq cases, **cinq portent l'essentiel de l'écart** entre les trois colonnes.

**1. La sécurisation du canal SAS ↔ IAM — 6 à 17, propre à C. C'est le poste le plus lourd de toute
la matrice.** Il ne s'agit pas d'« ajouter du mTLS ». Trois constats de code en fixent la
difficulté :

- **Le contournement ne porte pas seulement sur l'authentification, mais sur l'isolation
  multi-locataire.** Le commentaire de `WebSecurityConfig` est explicite : les sept chemins
  `/iam/v1/cas/*` contournent *« tenant + token auth »*, au même titre que les endpoints techniques.
  Rétablir la règle suppose de rentrer dans le modèle d'habilitations VitamUI, pas seulement dans la
  couche transport.
- **`runAsSystem(customerId, …)` existe parce que la création d'utilisateur exige un principal
  authentifié** ; le commentaire du code indique que `level=""` sert à lever la restriction de
  niveau. Le corriger, c'est reconstruire un contexte d'appel légitime — un travail de modèle
  d'autorisation, pas de configuration.
- **`GET /cas/idp/{id}` traverse ce contournement en renvoyant `clientSecret`, `keystoreBase64` et
  `keystorePassword`.** Le durcissement doit donc traiter aussi le **découpage de la ressource** et
  le **chiffrement des secrets**, pas uniquement l'accès.

S'y ajoute une contrainte issue de la décision du §10.4 : **pendant la coexistence A + C, CAS et le
serveur d'autorisation appellent les mêmes chemins IAM**, avec deux modes d'accès différents. Le
durcissement doit **servir les deux appelants simultanément sans interrompre CAS en production**.

Il est enfin **bloquant avant toute mise en service** : cette ligne n'est pas arbitrable à la
baisse, seulement dans le temps. C'est aussi la plus incertaine de la matrice, d'où l'amplitude.

**2. La mire de connexion — 5 à 12 en C, 1 à 2 en A.** Deuxième poste de C, et **le seul où C est
structurellement plus cher que A**, puisque A conserve la mire existante.

| | Constat |
|---|---|
| **Ce que le prototype apporte** | La SPA provisoire fait **296 lignes** (`app.js` 246, `index.html` 50, plus la feuille de style). Elle **prouve les enchaînements** — c'est son apport réel — mais ne couvre ni la totalité des écrans, ni l'internationalisation, ni le thème |
| **Ce qu'il faut couvrir** | La mire actuelle compte **20 gabarits pour 1 067 lignes** : e-mail, sélection d'organisation, mot de passe, MFA, téléphone manquant, code expiré, quatre écrans de réinitialisation, trois écrans d'erreur de compte, déconnexion, propagation de déconnexion, arrêt de webflow délégué |
| **L'internationalisation** | **fr / en / de**, environ 60 clés par langue — à porter, pas à réinventer |
| **Ce qui allège** | L'espace de travail Angular comporte déjà un projet **`design-system`** et une bibliothèque **`vitamui-library`** partagés par les huit applications. Une application `auth-ui` s'y ajoute en réutilisant l'existant |
| **Ce qui allège aussi** | **La mire n'applique aucune identité graphique par organisation.** `hasCustomGraphicIdentity` et `themeColors` existent sur `CustomerDto` mais **ne sont référencés nulle part dans `cas-server`**. Il n'y a qu'un thème à porter, pas N |

**Ce qui reste néanmoins du travail réel.** Le prototype ne dispense pas des écrans manquants, et
c'est de l'interface : **le levier de l'assistance IA y est plus faible** que sur le reste de C.
Mais l'ampleur est celle d'un **portage vers une cible connue dans un atelier outillé**, pas d'une
conception.

**3. Le parcours d'entrée multi-organisation — 6 à 11 en B, 0 en C.** L'écart tient à un fait
**établi par test le 27/07** : le modèle d'organisations de Keycloak ne permet pas à N organisations
de partager un domaine. Il faut donc écrire un authentificateur sur mesure **et neutraliser le
comportement natif**. En C, la fonction est **acquise**. Cette seule ligne représente près de la
moitié de l'écart de plancher entre B et C.

**4. L'authentification sur la base VitamUI — 6 à 10 en B, 0 en C.** Keycloak doit atteindre les
comptes qui vivent dans Mongo IAM : soit par une SPI de stockage, soit par migration. **La question
n'est pas tranchée** (§5), et la fourchette suppose la voie SPI. La voie migration coûterait
davantage et ajouterait une reprise de données absente de ce tableau.

**5. Le socle et la plateforme — 6 à 11 en A, 0 en C.** A doit repayer la montée de version des
dépendances ; C est **déjà sur la plateforme cible** (Spring Boot 4, Spring Security 7, Java 21).
**Cette ligne reviendra au cycle suivant en A, et jamais en C.**

### 7.5 Ce que la ventilation révèle et que les totaux masquaient

**C est la colonne la plus concentrée.** Quatre postes — **sécurisation du canal, mire, jeton/SSO,
mot de passe** — portent **environ 70 %** de sa charge de développement. Onze fonctions sur quinze y
sont à zéro. **Le risque de C est donc concentré, donc pilotable** : si la fourchette dérape, on
sait d'avance sur quelles lignes.

**Et son poste le plus lourd n'est pas une fonctionnalité.** La ligne la plus chère de C est
**invisible dans la matrice du §6**, parce qu'elle ne correspond à rien que l'utilisateur voie. Un
COPIL qui ne lirait que le §6 conclurait que C ne coûte presque rien.

**A est la colonne la plus étalée.** Quinze lignes sur seize sont non nulles, aucune ne domine.
C'est la signature d'une montée de version : **pas de poste redoutable, mais rien de gratuit non
plus**, et un total qui monte par accumulation. C'est aussi ce qui rend son plafond difficile à
garantir — **l'incertitude est répartie partout, donc invisible ligne à ligne.**

**B est la seule colonne sans aucun acquis.** Elle ne comporte aucun zéro en dehors de
l'administration. Chaque fonction y coûte quelque chose, **y compris celles que le produit couvre
nativement** — parce qu'il faut les raccorder au modèle VitamUI.

**Le retrait de X.509 et du MFA ne change presque rien.** Ensemble, ils pèsent 2 à 6 en A et 2 à 5
en B. Leur abandon en C **ne fait pas la décision** : il économise l'équivalent d'une semaine, alors
que la mire à elle seule en pèse deux à cinq. C'est un point à porter au COPIL — **l'arbitrage de
périmètre du §9 se justifie par la cohérence de la cible, pas par l'économie.**

### 7.6 Deux réserves sur ce tableau

**La colonne A chiffre une montée de version majeure**, telle que posée par l'équipe pour CAS 8.0.
La cible retenue au §10.2 est la ligne 7.3.x. Voir §7.2.

**Aucune colonne ne comporte le chantier de découplage** (§12), commun aux trois et **à compter une
fois en plus**.

---

## 8. Les risques — ce qui prime sur le chiffrage

**Pourquoi cette section prime sur la précédente.** Une charge est une **estimation** : personne n'a
réalisé aucune des trois trajectoires. Les réserves qui suivent sont d'une autre nature : ce sont
des **constats**, établis par test, par lecture de code, ou par la documentation de l'éditeur.

**Une réserve ne se re-cote pas en atelier. Elle se lève, ou elle reste.**

### 8.1 Réserves transverses

**1 — Le niveau de preuve est inégal, et cela avantage mécaniquement C.**

| Trajectoire | Niveau de preuve |
|---|---|
| **A — CAS** | documentation éditeur et analyse du code existant ; **aucun essai de montée de version mené** |
| **B — Keycloak** | documentation, **+ un point testé** (multi-domaine à suffixe partagé), résultat **négatif** |
| **C — SAS** | travaux de faisabilité réels sur 7 cas d'usage, validés end-to-end |

*Pourquoi elle prime.* **Un scénario mesuré paraît toujours plus sûr qu'un scénario estimé, y
compris quand il ne l'est pas.** Les inconnues de C sont levées parce qu'elle a été explorée ;
celles de A et B restent devant. C'est un biais de méthode, et il joue en faveur de la trajectoire
que l'équipe recommande — raison de plus pour l'énoncer.

**2 — Le taux d'erreur d'estimation observé est de × 2 à × 2,4**, et il a été produit par le même
processus que celui qui a produit les fourchettes du §7. Voir §1 et §7.1. **Vaut pour les trois
colonnes.**

**3 — Ce qui ne se compresse dans aucune trajectoire.** Recette avec de vrais fournisseurs
d'identité, packaging Ansible, mire, revue de sécurité, et débogage des défauts qui n'apparaissent
qu'au premier login réel — le retour d'expérience du 24/07 en recense **13** sur les seuls chantiers
OIDC et SAML. *Pourquoi elle prime.* C'est ce bloc qui domine le calendrier, pas le développement.
Il explique pourquoi un écart de charge ne se traduit pas proportionnellement en délai de livraison.

**4 — La matrice `matrice_CAS_vs_Keycloak.xlsx` n'est pas utilisable en comparatif.** Elle cote de
1 à 5 (linéaire) là où le dossier cote en Fibonacci : les totaux ne sont pas commensurables.
Surtout, **son onglet CAS chiffre une montée de version mineure qui n'existe plus** — il suppose
`cas.version` déjà en 7.0.10.1 et « pas d'API breaking connue ». Or 7.0.x, 7.1.x et 7.2.x sont EOL.
*Pourquoi elle prime.* Son total de 31 pour CAS circule et paraît rassurant. **Il chiffre une
opération qui n'est pas disponible.**

### 8.2 Réserves propres à A

**5 — Le mode de défaillance est silencieux.**
*Constat.* Si CAS renomme `defaultAccessTokenFactory`, notre `@Bean` n'écrase plus rien : il
enregistre un bean supplémentaire inutilisé. **Pas d'erreur de compilation. Pas d'erreur au
démarrage.** CAS utilise silencieusement sa factory par défaut, `TOK-<UUID>` n'est plus émis, et les
resource servers rejettent les jetons. Le défaut se découvre **en recette**, au mieux.
*Comment il a été établi.* Lecture de `AppConfig.java` et de `application.properties:62` — 28 points
d'accroche recensés. Le phénomène n'est pas hypothétique : le retour d'expérience du 24/07 documente
le même mécanisme sur les travaux SAS (*« Bean shadowing token generator : `OpaqueVitamTokenGenerator`
retiré du component-scan pour éviter le fallback par défaut »*).
*Pourquoi il prime.* Aucune valeur de charge ne peut représenter un risque dont on ne sait pas s'il
s'est matérialisé.

**6 — Le coût n'est pas borné.**
*Constat.* Les notes de version documentent le passage de JDK, Jackson, la validation stricte des
propriétés. Elles **ne documentent pas** les changements sur les API webflow, délégation pac4j,
surrogate et password management — soit précisément les quatre surfaces dont dépend l'intégralité de
notre spécifique.
*Pourquoi il prime.* Les postes les plus lourds du §7.3 ne disent pas « on sait que c'est
difficile ». Ils disent **« on ne sait pas ce qu'on ne sait pas »**.

**7 — Le cycle est à repayer.**
*Constat.* 6 mois de support complet, puis 6 mois de correctifs de sécurité seuls. Le projet écrit
ne pas pouvoir offrir de LTS. 8.1.0-RC1 est due le 21/08/2026.
*Pourquoi il prime.* **Aucun résultat de mesure ne peut contredire ce fait.** Un essai de montée de
version préciserait un coût ; il ne changerait pas la cadence imposée. La question posée par A n'est
pas « combien coûte la montée de version ? », mais **« acceptons-nous de la refaire à chaque
cycle ? »** — et cette question se tranche sans essai.

**8 — Le précédent ne se rejouera pas.**
*Constat.* Les 50 à 60 jours de CAS 6 → 7 ont été consommés **avec une application déjà migrée en
exemple**. Pour la montée suivante, cet exemple n'existera pas.
*Pourquoi il prime.* Le seul repère chiffré dont l'équipe dispose a été obtenu dans des conditions
**plus favorables** que celles à venir. **Il constitue un plancher, pas une prévision.**

**9 — La connaissance acquise ne se transfère pas.**
*Constat.* Les internes non documentés s'apprennent en lisant le source et en exécutant. Ils ne se
transmettent ni par la documentation, ni par la revue de code.
*Pourquoi il prime.* Le savoir de la dernière montée vit chez la personne qui l'a faite. C'est un
risque de *bus factor* qui s'ajoute au coût récurrent, et **il ne figure dans aucune grille**.

**10 — Le composant de production est modifié en place.** L'overlay est recompilé contre de nouveaux
internes ; les quinze fonctions du §6 sont à revalider, et le retour arrière est une reprise de
version. *C'est ce que couvre la ligne de recette, et ce qui la rend incompressible.*

### 8.3 Réserves propres à B

**11 — Le total est un plancher, pas une estimation.** Même pénalité de faible levier de l'assistance
IA que A — adaptation aux internes d'un produit — **sans aucun code existant à réutiliser**.
*Pourquoi elle prime.* C'est le seul total des trois dont on sait qu'il est sous-évalué, sans savoir
de combien.

**12 — Une décision structurante n'est pas tranchée.** Keycloak devient-il la source de vérité des
identités, ou branche-t-on un `User Storage SPI` sur le Mongo IAM ?
*Pourquoi elle prime.* Le choix emporte **la migration des données**, le problème du double
référentiel et la réversibilité de l'ensemble. Il devrait être instruit **avant** que le total ne
soit considéré comme signifiant. **Il n'est inclus dans aucune fourchette.**

**13 — Le sur-mesure est déplacé, pas supprimé.** *Organizations* a été **écarté par test le
27/07** : N organisations ne peuvent pas partager un même domaine. Les fonctions d'entrée relèvent
donc toutes d'un développement d'`Authenticator SPI`.
*Pourquoi elle prime.* Elle invalide l'hypothèse d'un « Keycloak en configuration », qui était la
justification première du scénario. **Transposer les 7 737 lignes en SPI Keycloak, c'est changer de
produit sans changer de problème.**

**14 — L'ergonomie de connexion est proche mais non identique.** Impact utilisateur et conduite du
changement, hors périmètre technique et hors fourchette.

### 8.4 Réserves propres à C

**15 — La dette de sécurité est active, et documentée dans le code.**
*Constat.* `CasService.runAsSystem(level="")` constitue un bypass complet, et **7 endpoints
`/iam/v1/cas/*` sont whitelistés** dans `WebSecurityConfig`. Le commentaire du code le dit lui-même :
*« Production hardening (mTLS / signed header) is deferred to Phase 2. »* **Cinq de ces sept
endpoints ont été créés par les travaux de faisabilité eux-mêmes** (§2.3) : la dette n'est pas
héritée du socle actuel, **elle a été introduite avec le prototype**. `GET /cas/idp/{id}` est le plus
exposé — il renvoie `clientSecret`, `keystoreBase64` et `keystorePassword` sans authentification.
*Pourquoi elle prime.* C'est **bloquant avant toute mise en service**. Le §7.3 chiffre le travail ;
il ne dit pas qu'il conditionne la mise en production.

**16 — L'avantage de preuve n'est pas un avantage de fond.**
*Constat.* Les 7 cas d'usage validés en 2 jours ont réutilisé les 7 endpoints IAM existants, **mais
il a fallu en ajouter 5 et écrire +405 lignes** dans `CasController` et `CasService`.
*Pourquoi elle prime.* Deux effets, en sens opposés, qu'il faut tenir ensemble.
**Elle amoindrit la mesure** : les 2 jours ne portent pas que sur le serveur d'autorisation, ils
incluent du développement côté IAM, et cette part n'est pas transposable au reste du périmètre —
d'autant que ce qui reste (mire, X.509, mot de passe, recette) est précisément là où le levier est
le plus faible.
**Elle la conforte aussi** : ces 5 endpoints ne sont pas un contournement du modèle IAM, ce sont des
points d'accès explicites à une logique qui existait déjà. Le fait qu'il ait suffi de 405 lignes
pour exposer HRD, jeton, subrogation validée, IdP et JIT confirme le §2.4 — **le métier était bien
dans IAM ; ce qui manquait, c'était le contrat d'accès.**
En tout état de cause, cette mesure établit la **faisabilité** des sept fonctions. **Elle n'établit
pas une vitesse d'exécution généralisable.**

**17 — Nous devenons responsables du composant.**
*Constat.* Plus d'éditeur pour porter les évolutions du socle : la compétence doit être maintenue en
interne, dans la durée.
*Pourquoi elle prime.* C'est une condition non négociable : sans elle, **la recommandation bascule
sur la trajectoire A**. Cette réserve ne se chiffre pas ; elle se traite en organisation.

**18 — Le durcissement du canal contient une décision d'architecture non prise.** Les postes de C ne
sont pas de même nature, et le §7.3 les présente à tort sur un pied d'égalité :

| Nature | Postes concernés | Ce qu'on peut en dire |
|---|---|---|
| **Énumérable** | mire, mot de passe, persistance, packaging, rechargement à chaud, limitation de débit | On sait quoi faire, la variance est faible. **Estimable aujourd'hui** |
| **Suspendu à une décision** | **durcissement du canal SAS ↔ IAM** | Contient une décision **non prise** : quel modèle de confiance inter-services remplace la liste blanche. Tant qu'elle n'est pas tranchée, ce chiffre est un **espace réservé, pas une estimation** |
| **De débogage** | blocage SSO entre deux `/oauth2/authorize` | **Calendaire et non compressible.** Personne ne sait combien de temps prend un débogage avant de l'avoir fait |

**Additionner ces trois natures produit un nombre qui paraît homogène et ne l'est pas.** C'est
précisément le mécanisme qui a produit les 25 jours du ticket CAS 6 → 7.

> **La mesure la moins chère qui réduirait le plus l'incertitude.** Trancher le modèle de confiance
> inter-services : **2 à 3 points de charge en conception, aucune ligne de code.** Cette seule décision
> convertit le plus gros poste de C d'espace réservé en estimation. Aucune re-cotation en atelier
> n'aura cet effet, parce que **l'atelier ne peut pas coter ce qui n'est pas décidé.** Ce n'est pas
> une étude à financer : c'est la première tâche du travail engagé au §10.3.

### 8.5 Le risque sur l'existant n'est pas réparti également

C'est une asymétrie que la fourchette de charge ne montre pas.

| | Ce qui arrive au socle en production |
|---|---|
| **A** | Il est **modifié en place**. Les 28 points d'extension peuvent cesser d'accrocher **sans erreur de compilation ni de démarrage**. Les quinze fonctions sont à revalider, et le retour arrière est une reprise de version |
| **B** | Il est **remplacé**, et le référentiel d'identités est déplacé |
| **C** | Il **n'est pas touché**. Le serveur d'autorisation est un module distinct qui coexiste avec CAS ; IAM n'est modifié que par ajout de points d'accès. La bascule se fait par redirection, donc **elle est réversible** |

**La trajectoire C est la seule à ne porter aucun risque fonctionnel sur l'existant.** Ses risques —
dette de sécurité, périmètre, compétence interne — portent tous sur **le nouveau chemin**, pas sur le
service rendu aujourd'hui. C'est ce qui rend la coexistence du §10.4 praticable : **investir dans C
ne met pas en jeu le fonctionnement actuel.**

**Lecture d'ensemble.** Les risques de A et B sont des **incertitudes** : on ne sait pas ce qu'on
trouvera. Ceux de C sont des **charges identifiées** : la dette est chiffrée, la perte fonctionnelle
est nommée, la responsabilité interne est une décision d'organisation. **Un risque nommé se traite ;
une incertitude ne se traite qu'en la levant, et aucun essai n'a été mené sur A.**

---

## 9. Ce que la trajectoire C fait perdre

Deux fonctions du §6 passent en ❌. C'est une **réduction de périmètre assumée**, et elle doit être
portée à la décision explicitement — elle ne relève pas de l'équipe technique.

| Fonction perdue | Situation actuelle | Substitution proposée |
|---|---|---|
| **Authentification par certificat client X.509** | extraction du certificat depuis un en-tête nginx, mapping d'attributs, résolution de l'utilisateur dans IAM | **délégation à un IdP externe** portant l'authentification par certificat |
| **MFA sur la base d'utilisateurs interne** | OTP par SMS, **activation conditionnelle au déploiement**, non maintenu par l'équipe | **délégation à un IdP externe** portant le second facteur |

**Ce qui atténue la perte.**

- **X.509 est déjà une fonction bridée.** Elle ne supporte **pas le multi-domaine** : s'il existe
  zéro ou plusieurs fournisseurs de type certificat pour le domaine, l'authentification échoue. Et
  **la subrogation y est désactivée**. Ce n'est pas un parcours de plein exercice, c'est un chemin
  d'accès restreint.
- **Le MFA ne sollicite aucun point d'accès IAM.** Le jeton transite par le registre de tickets de
  CAS, et le déclenchement dépend de deux attributs de l'utilisateur. La fonction est **entièrement
  dans le produit CAS** : rien n'en est réutilisable ailleurs, **ce qui confirme qu'il n'y a pas de
  demi-mesure — on la porte intégralement, ou on l'abandonne.**

**Le point commun des deux substitutions.** Elles reposent sur la même capacité — la **fédération
entrante OIDC / SAML**, ligne 3 du §6, **validée end-to-end**. Techniquement, le report est acquis.
Ce qui ne l'est pas, c'est la condition organisationnelle : **elle suppose qu'un IdP externe soit
disponible pour les populations concernées.**

**Trois questions à instruire avec le métier :**

1. **Quelles populations utilisent effectivement X.509 aujourd'hui ?** La fonction est présente au
   socle ; son usage réel n'a pas été qualifié dans ce dossier.
2. **Le MFA interne est-il en service ?** Sa condition d'activation est fausse par défaut et les
   identifiants du fournisseur SMS sont livrés non renseignés. La fonction est probablement inactive,
   mais cela reste à confirmer environnement par environnement.
3. **Chaque population concernée dispose-t-elle d'un IdP externe ?** À défaut, la substitution n'en
   est pas une, et les deux fonctions redeviennent du périmètre à développer.

**Il n'y a pas de charge de substitution à ajouter** — la fédération qui la porte est déjà acquise.

---

## 10. Décision proposée

### 10.1 Écarter B — Keycloak

**Motif : trop long et trop risqué.** C'est la trajectoire la plus coûteuse dans les deux mesures
indépendantes, la seule à porter un **risque de faisabilité établi par test** plutôt qu'estimé, et
la seule à dégrader l'ergonomie de connexion.

**Ce que cet abandon ne dit pas.** Keycloak reste le meilleur des deux produits sur le plan de
l'extensibilité — SPI déclarées et documentées contre écrasement de beans internes, cadence de
version soutenable. Il est écarté non pas parce qu'il est le moins bon produit, mais parce que **le
modèle d'entrée VitamUI ne se plie pas au sien**. C'est un constat sur notre modèle, pas sur le
sien — et **c'est le même constat qui explique pourquoi CAS est aujourd'hui aussi personnalisé.**

### 10.2 Réaliser une montée de version CAS dans la ligne 7.3.x — à titre conservatoire

**Ce n'est pas le choix d'une cible d'architecture.** C'est une **mesure conservatoire**, motivée par
trois exigences : **politique**, **engagement pris** et **audit** — ne pas rester sur une version
que l'éditeur déclare en fin de vie pendant que la trajectoire cible se construit.

> **La cible est conditionnée par la version de Java supportée.** CAS 7.1, 7.2 et 7.3 requièrent
> **JDK 21** ; seule CAS 8.0 impose **JDK 25**. Le monorepo VitamUI est en Java 21. Rester dans la
> ligne 7.x **supprime le saut de JDK**, ainsi que le saut Spring Boot 4 / Spring Security 7 /
> Jackson 3 qu'impose CAS 8.0. C'est ce qui rend cette mesure réalisable à coût contenu.

> ⚠️ **La cible est 7.3.x, pas 7.1.x.** **CAS 7.1.x est EOL.** La politique de maintenance d'Apereo
> ne référence plus que **7.3.x** — en correctifs de sécurité seuls depuis le 30/06/2026, **EOL au
> 31/12/2026** — et précise que *toute version absente du tableau est considérée EOL*.
>
> **Si le motif est politique et d'audit, une montée vers 7.1 ne l'atteint pas** : elle
> substituerait une version EOL à une autre. **La cible à retenir est 7.3.x**, au même JDK 21 et
> pour un effort du même ordre.
>
> Elle n'achète toutefois que **jusqu'au 31/12/2026**, et uniquement des correctifs de sécurité.
> C'est cohérent avec un statut de mesure conservatoire ; **ce ne serait pas cohérent avec un statut
> de cible.**

### 10.3 Poursuivre C — dans une version industrialisée

**Motif : c'est la seule trajectoire dont l'investissement n'est pas à repayer**, et la seule dont la
faisabilité soit établie par des travaux réels plutôt que par estimation.

Le mot **industrialisée** porte la différence entre le prototype et un composant de production. Il
recouvre, au minimum :

- la **sécurisation du canal SAS ↔ IAM** — condition **bloquante** avant toute mise en service ;
- la **mire Angular** en remplacement de la SPA provisoire, avec thème et i18n fr / en / de ;
- la **persistance** du registre de clients et des autorisations ;
- le **parcours de réinitialisation du mot de passe** — la politique et l'historique sont déjà dans
  IAM (§6, note ¹) — et la **limitation de débit par requête**, le verrouillage de compte étant lui
  aussi déjà acquis ;
- le **rechargement à chaud des fournisseurs d'identité** (§6, note ⁴) ;
- le **packaging, le déploiement et la supervision** (Ansible, Consul) ;
- la **recette end-to-end**, non compressible.

### 10.4 Les deux ensemble

A et C **ne sont pas exclusives**.

| | Rôle | Horizon | Ce que ça coûte de ne pas le faire |
|---|---|---|---|
| **A — 7.3.x** | mesure conservatoire | immédiat, borné au 31/12/2026 | rester en production sur une version EOL pendant la construction de la cible |
| **C — SAS** | trajectoire cible | au-delà | continuer à repayer le cycle CAS indéfiniment |

**Techniquement, rien ne s'y oppose — et ce n'est pas un hasard.** Les deux chantiers ne touchent pas
le même code : A recompile l'overlay `cas/cas-server`, C développe un module distinct
`api/auth-server`. Leur seule zone commune est `api-iam`, où C **ajoute** des points d'accès sans
modifier ceux qu'utilise CAS. Aucune migration de données n'est en jeu, et la bascule vers C se fait
par redirection, donc **sans point de non-retour**. Le socle en production continue de fonctionner
sous CAS pendant toute la construction de la cible.

**Une contrainte technique en découle, et elle n'est pas gratuite.** Pendant la coexistence, **CAS et
le serveur d'autorisation appellent les mêmes chemins `/iam/v1/cas/*`** — CAS par ses rôles
applicatifs, SAS par la liste blanche du prototype. Le durcissement du canal (§7.4, poste n° 1) doit
donc **servir les deux appelants à la fois, sans interrompre CAS en production**. C'est plus difficile
que de sécuriser un appelant unique, et c'est chiffré comme tel.

**La contrepartie doit être énoncée** : les deux chantiers consomment la même équipe. **Le coût de A
est un coût de transition, entièrement perdu à l'arrivée de C** — il n'achète pas un pas vers la
cible, il achète du temps. C'est un arbitrage légitime si l'exigence de conformité est ferme ; il ne
l'est pas si elle ne l'est pas. **C'est la question à poser au COPIL.**

---

## 11. Ce qui reste à trancher

| # | Point | Qui tranche | Sans réponse |
|---|---|---|---|
| 1 | **Abandon de X.509 et du MFA interne** — et disponibilité d'un IdP externe pour les populations concernées (§9) | métier / commanditaire | le périmètre de C n'est pas figé, et sa fourchette non plus |
| 2 | **Fermeté de l'exigence de conformité** qui motive la mesure conservatoire A (§10.4) | commanditaire | on engage un coût de transition perdu sans avoir vérifié qu'il est exigé |
| 3 | **Modèle de confiance inter-services SAS ↔ IAM** — ce qui remplace la liste blanche du prototype. 2 à 3 points de charge en conception, aucune ligne de code (§8.4) | équipe technique | **le premier poste de charge de C reste non estimable**, et la mise en service reste bloquée |
| 4 | **Chantier de découplage** — commun aux trois trajectoires, **exclu de toutes les grilles**, à compter une fois en plus. Implique un **changement visible par l'utilisateur** (§12) | commanditaire | contrainte d'iso-fonctionnel maintenue, donc modèle spécifique maintenu |
| 5 | **Maintien d'une compétence interne dans la durée** sur le socle d'authentification | direction technique | **la trajectoire C n'est pas tenable** — c'est une condition, pas un souhait |

*Décisions déjà actées au COPIL du 28/07 : mandater l'instruction du périmètre iso-fonctionnel, et
prendre acte du risque de sécurité lié à l'absence de correctifs. Le choix de trajectoire avait été
reporté. **Aucun financement de travaux complémentaires d'évaluation n'est demandé.***

---

## 12. Ce qui reste hors des trois colonnes

Le chantier de **découplage** — sortir la sélection d'organisation et la subrogation du flux
d'authentification pour en faire des processus métier IAM — est volontairement **exclu des trois
colonnes** : il est commun aux trois, donc neutre dans la comparaison. **À compter une seule fois,
en plus.**

Le §2.4 montre qu'il est techniquement plus accessible qu'il n'y paraît : la logique existe déjà
côté IAM, c'est l'orchestration qui est à déplacer. Les travaux de faisabilité en fournissent une
mesure indirecte — **405 lignes** ont suffi à exposer en API la résolution HRD, l'émission du jeton,
la validation de subrogation, la configuration d'IdP et le provisioning JIT. Ce chiffre ne couvre pas
le chantier complet, mais il indique **l'ordre de grandeur du travail d'exposition, à distinguer du
travail de refonte du parcours utilisateur.**

**Mais il n'est pas neutre fonctionnellement.** Il suppose de reconnaître qu'une partie du parcours de
connexion actuel n'a pas à vivre dans l'authentification, ce qui **implique un changement visible par
l'utilisateur**. Il relève donc d'un arbitrage de périmètre, et non d'une décision technique que
l'équipe pourrait prendre seule.

---

# Annexe A — Cotations de complexité (historique, non normatif)

> ⚠️ **Pourquoi ces grilles ne sont plus la référence.**
>
> **1. Elles ne sont pas convertibles en charge.** L'échelle est Fibonacci et mesure l'incertitude
> autant que l'effort. Toute conversion en jours ou en euros produirait un chiffre faux, présenté
> avec une précision qu'il n'a pas.
>
> **2. La grille A a été établie pour CAS 8.0**, qui n'est plus la cible retenue (§10.2). Elle ne
> s'applique pas telle quelle à une montée dans la ligne 7.3.x, qui supprime le saut de JDK et le
> saut Spring Boot.
>
> **3. Le total de C n'est plus maintenu.** Depuis l'abandon de X.509 (−5), la correction de
> l'anti-force brute (−2) et la révision partielle du mot de passe, le sous-total « reprise des cas
> d'usage » est **surévalué d'au moins 7 points de complexité sur les lignes existantes, et incomplet d'au moins
> une ligne** (le rechargement à chaud des IdP). **L'ordre de grandeur reste inférieur à 64, sans
> qu'on puisse dire de combien.**
>
> Elles sont conservées parce qu'elles sont **le seul chiffrage poste par poste produit avant le
> 30/07**, et qu'elles documentent le raisonnement qui a conduit à écarter B.

**Échelle** — 1 trivial · 2 simple · 3 modéré · 5 substantiel · 8 lourd · 13 très lourd ou incertain

### A.1 — Trajectoire A (CAS 8.0)

| Poste | Notre situation | Cplx |
|---|---|---:|
| **Bascule effective sur le BOM CAS** — retrait des pins locaux | le POM importe le BOM mais re-déclare `spring.boot.version`, `jackson.version`, `groovy.version`, et pin les 8 artefacts pac4j | 3 |
| 5 sauts successifs (7.0 → 7.1 → 7.2 → 7.3 → 8.0) | **sans exemple migré cette fois** | 5 |
| **25 exclusions Maven à revalider** | une exclusion devenue obsolète est **silencieuse** | 5 |
| **JDK 21 → 25 (obligatoire)** | **hors périmètre du BOM** — build, CI, packaging, JVM de l'hôte | 8 |
| Tomcat 10 → 11 | vient avec Spring Boot 4 *via* le BOM | 1 |
| ~40 propriétés `cas.*` | validation stricte : toute propriété renommée empêche le démarrage | 3 |
| **Sous-total plateforme** | | **25** |
| Webflow de connexion | `VitamLoginWebflowConfigurer`, `DispatcherAction`, `ListCustomersAction` | 13 |
| Délégation OIDC / SAML2 | le plus exposé aux ruptures pac4j | 13 |
| Subrogation (les deux modes) | API `surrogate-*` sujette à changement | 8 |
| Gestion du mot de passe | + suppression du hack `UserLoginModel` | 5 |
| X509 | `CustomRequestHeaderX509CertificateExtractor` et le mapping d'attributs | 3 |
| Tickets & jetons (`TOK-<UUID>`, TGT factory) | | 5 |
| Logout / Single Logout | `TerminateApiSessionAction` | 3 |
| 19 vues Thymeleaf | alignement sur les templates cibles | 5 |
| **Sous-total spécifique** | | **55** |
| Packaging, intégration initiale | | 8 |
| Déploiement / configuration / supervision | | 3 |
| Recette complète | | 13 |
| **Sous-total transverse** | | **24** |
| **GRAND TOTAL** | | **104** |

> **Note de méthode — pourquoi le sous-total plateforme est passé de 60 à 25 le 28/07.** La version
> initiale cotait « Spring Boot 3.2 → 4 » (13), « Jackson 2.16 → 3.x » (8) et « pac4j 6.3.3 → 7 »
> (13) comme des postes de plateforme. Erreur à double titre : **le BOM fait l'alignement des
> versions**, et **ce qui coûte n'est pas la version mais l'impact sur *notre* code**, déjà compté
> dans le sous-total spécifique. Les 34 points de complexité retirés étaient comptés deux fois.

### A.2 — Trajectoire B (Keycloak 26)

| Cas d'usage | Standard | Point de départ | Cplx |
|---|:--:|---|---:|
| Authentification login / mot de passe | ✅ | base users à trancher — **décision structurante** | 13 |
| Résolution HRD e-mail → organisation | ❌ | *Organizations* **écarté par test** ⇒ Authenticator SPI | 13 |
| Sélection d'organisation (N comptes / e-mail) | ❌ | Authenticator SPI + thème custom | 13 |
| Multi-domaine à suffixe partagé | ❌ | **testé : non couvert** | 13 |
| Délégation OIDC | ✅ | provisionner Keycloak depuis IAM (Admin REST) | 5 |
| Délégation SAML2 | ✅ | idem, mécanisme mutualisé | 3 |
| X509 | ✅ | authenticator à configurer + extraction header nginx | 5 |
| Subrogation compte générique | ⚠️ | impersonation admin + token exchange | 8 |
| Subrogation avec validation | ❌ | Authenticator SPI + workflow IAM conservé | 13 |
| Gestion du mot de passe | ✅ | à recâbler sur la base retenue | 5 |
| Émission `TOK-<UUID>` | ❌ | couche d'émission maison **ou** migration JWT | 13 |
| SSO + Single Logout | ✅ | | 2 |
| Thème & i18n de la mire | ⚙️ | 19 vues à retranscrire | 8 |
| Audit / logbook VitamUI | ⚙️ | Event Listener SPI → IAM | 3 |
| Initialisation du super-admin | 🔧 | realm + compte + script de promotion | 3 |
| **Sous-total** | | | **120** |
| Packaging · déploiement · tests d'intégration | | | 24 |
| **Migration des données** (users, IdP, secrets) | | | 13 |
| **GRAND TOTAL** | | | **157** |

### A.3 — Trajectoire C (Spring Authorization Server)

| Cas d'usage | État | Cplx |
|---|---|---:|
| Authentification login / mot de passe | ✅ validé | 0 |
| Résolution HRD + sélection d'organisation | ✅ validé | 0 |
| Délégation OIDC externe | ✅ validé end-to-end | 0 |
| Délégation SAML2 externe | ✅ validé end-to-end | 0 |
| Provisioning JIT | ✅ validé | 0 |
| Émission `TOK-<UUID>` | ✅ contrat préservé | 0 |
| Multi-clients OIDC (8 SPA) | ✅ | 0 |
| Subrogation | ✅ implémenté, test manuel en attente | 1 |
| SSO inter-applications | ⚠️ blocage identifié — **débogage, peu compressible** | 2 |
| **Sécurisation du canal SAS ↔ IAM** | ❌ **bloquant** | 5 |
| Registre de clients persistant | ❌ | 2 |
| Persistance `OAuth2AuthorizationService` | ❌ | 2 |
| Logout / end-session consolidé | ❌ | 3 |
| Gestion du mot de passe | ❌ *(à revoir à la baisse — cf. §6 note ¹)* | 5 |
| Mire Angular `auth-ui` | ❌ **UI réelle, faible levier IA** | 8 |
| X509 | ❌ *(abandonné depuis — §9)* | 5 |
| Throttling / anti-brute force | ❌ *(erroné — c'est dans IAM, cf. §6 note ²)* | 2 |
| Thème & i18n fr/en/de | ❌ messages existants à porter | 3 |
| Dette de sécurité du prototype | ❌ 7 items recensés | 5 |
| **Sous-total** | | **43** |
| Packaging · déploiement · tests d'intégration | | 21 |
| **GRAND TOTAL** | | **64** |

### A.4 — Synthèse comparée en points de complexité

| | A — CAS 8 | B — Keycloak | C — SAS |
|---|---:|---:|---:|
| Reprise des cas d'usage | 80 | 120 | **43** |
| Intégration, déploiement, recette | 24 | 37 | 21 |
| **Total** | **104** | **≥ 157** | **64** |
| Effort relatif normalisé | ≈ 1,6 × | ≈ 2,5 × | **1 ×** |

**Une divergence à ne pas harmoniser en silence.** Les points de complexité placent C nettement
devant A (rapport 1,6) ; **la charge du §7 les met à égalité au plancher.** Les deux mesures ne
mesurent pas la même chose : les points intègrent **l'incertitude** et le levier de l'assistance IA,
la charge intègre **le reste à livrer en conditions réelles**. **Cette divergence ne doit pas être
arbitrée par moyenne** — elle dit que A et C ne se départagent pas sur le coût.

*Note : le MFA par SMS a été retiré des trois grilles le 29/07 (A 107 → 104, B 165 → 157,
C 67 → 64), pour deux motifs : la fonctionnalité est probablement caduque, et sa cotation à 8 points de complexité
en Keycloak était fausse. **Les rapports normalisés sont inchangés.***

---

# Annexe B — Journal des corrections et des divergences

Cette annexe existe pour une raison : les documents sources sont conservés, et **certains portent
des valeurs qui ne sont plus exactes**. Chaque entrée dit ce qui était écrit, ce qui est retenu, et
pourquoi.

| # | Ce qui était écrit | Où | Ce qui est retenu |
|---|---|---|---|
| **D1** | « 13 endpoints » en prose, 12 en annexe | 27 et 28/07 | **7 endpoints** consommés par `cas-server`, **+5 ajoutés par le prototype**. Le décompte brut est de 12 ; le chiffre 13 était faux. Corroboré par une analyse indépendante (annexe C) |
| **D2** | Le scénario A chiffre une montée vers **CAS 8.0** | 27, 28 et 29/07 | **Ligne 7.3.x**, à titre conservatoire (§10.2). Ce n'est pas le même travail : 7.3 reste en JDK 21. **La grille A de l'annexe A ne s'applique pas telle quelle à la cible retenue** |
| **D3** | « 12 mois de support » | 27 et 28/07 | **6 mois de support complet, puis 6 mois de correctifs de sécurité.** Exact comme durée totale, trompeur comme durée de support |
| **D3b** | « 6 mois » (29 et 30/07) puis « 5 mois » (synthèse 30/07) | 29 et 30/07 | **Une date, pas une durée : jusqu'au 31/12/2026.** Les deux chiffres étaient exacts sur des référentiels différents — durée de la phase « correctifs seuls » contre temps restant à la date du document. La date supprime l'ambiguïté |
| **D4** | X.509 en trajectoire C coté **5 points de complexité** comme un portage à réaliser | 29/07 | **Abandonné** (§9), et porté au point 1 du §11. Effet sur la grille C : 64 → 59 |
| **D5** | Recette forfaitaire de **15 jours**, identique dans les trois colonnes | 30/07 | **Recalée sur le précédent mesuré** — 25 à 35 jours, soit **17 à 24 points de charge** après rebasage. Le document du 30/07 qualifiait lui-même l'uniformité de « commodité de présentation ». **L'uniformité subsiste** faute d'élément permettant de la lever, et elle est signalée comme réserve (§7.1) |
| **D6** | Base de B : **70 – 120 j** (document détaillé) contre **85 – 145 j** (synthèse COPIL) | 30/07 | **La base de l'équipe, 70 – 120.** L'écart de +21 % de la synthèse n'était justifié dans aucun document. Le contournement du modèle *Organizations* — que le document détaillé signale explicitement comme **non chiffré** — reste hors fourchette, et il est signalé comme tel (§8.3, réserve 12) plutôt qu'incorporé sans trace |
| **D7** | Base de C : **50 – 100 j** (équipe) contre **60 – 120 j** (estimation indépendante) | 30/07 | **La méthode uniforme du §7.1**, qui reconstruit les trois colonnes de la même façon. Appliquée à C, elle redonne exactement **42 – 84** — soit le chiffre de l'estimation indépendante. Ce n'est pas une coïncidence : cette estimation ne différait de celle de l'équipe **que par la ligne de recette**, qui est précisément ce que la méthode corrige |
| **D8** | La colonne A n'avait **pas reçu** la correction de recette appliquée à B et C | synthèse 30/07 | **Corrigé.** A passe de 35 – 63 à **42 – 77**. Voir l'encadré ci-dessous |
| **D9** | « Points » employé pour deux échelles différentes | 30/07 | **« Points de charge »** dans le corps, **« points de complexité (Fibonacci) »** en annexe A. Jamais « points » seul |
| **D10** | **Demande n° 2 — « Financer un essai de montée de version CAS »** | note du 27/07, l. 102 | **Retirée le 28/07** : *« Nous ne demandons pas de financer de travaux complémentaires d'évaluation. »* Motif : un essai préciserait un coût, **il ne changerait pas la cadence imposée** (§8.2, réserve 7) |
| **D11** | Le découplage qualifié de **« décision sans regret »**, à engager sans condition | note du 27/07, l. 101 | **Soumis au comité** : il implique un **changement visible par l'utilisateur**, donc un arbitrage de périmètre et non une décision technique (§12) |
| **D12** | « 5 sauts successifs » contre « 4 versions à mettre à jour » | 27-28/07 | **4 sauts** de 7.0 vers 8.0 (7.1, 7.2, 7.3, 8.0). La grille de l'annexe A conserve la formulation d'origine, non recalculée |
| **D13** | Le précédent CAS 6 → 7 cité comme **« ~25 jours »**, sans plus | 27, 28 et 29/07 | **25 jours déclarés au ticket, 50 à 60 réellement consommés.** Précision apportée le 30/07. **Ce chiffre ne doit pas circuler amputé** : c'est le seul étalon du dossier, et c'est l'écart qui porte l'information |
| **D14** | Anti-force brute en C coté **2 points de complexité**, « non traité (natif CAS) » | 29/07 | **Déjà dans IAM** et hérité par C (§6, note ²). Seule subsiste la limitation de débit par requête |
| **D15** | Gestion du mot de passe en C cotée **5 points de complexité**, « non traité » | 29/07 | **Politique, historique et validateur sont dans IAM** (§6, note ¹). Ce qui reste est le **parcours** de réinitialisation |
| **D16** | Journalisation des connexions et administration à chaud des IdP | absentes des trois grilles du 29/07 | **Ajoutées** (§6, lignes 13 et 14). L'administration à chaud est **la seule correction qui va dans le sens de la hausse** |
| **D17** | `matrice_CAS_vs_Keycloak.xlsx` — Keycloak 43 / CAS 31, échelle 1-5 | document externe | **Non commensurable**, et son onglet CAS chiffre une montée mineure qui n'existe plus (§8.1, réserve 4) |
| **D18** | La synthèse du 27/07 porte une mention « Révision du 28/07 » | 27/07 | Signalé : **le contenu d'un dossier daté n'est pas figé à sa date**. Le présent document fait foi |

> ### D8 en détail — la correction de la colonne A
>
> C'est la seule divergence qui change un chiffre présenté, et elle mérite d'être exposée.
>
> La synthèse COPIL du 30/07 affichait **A 35 – 63**, obtenu en appliquant ×0,7 au total brut de
> l'équipe (50 – 90). Mais elle affichait dans le même tableau une ligne de recette de 18 – 25, qui
> correspond au précédent mesuré et non au forfait de 15. **Les deux ne se réconcilient pas** : si
> la recette vaut 18 – 25, alors le développement de A tombe à 17 – 38, soit 30 % en dessous de ce
> que donne sa propre ventilation.
>
> Autrement dit : **la correction de recette avait été appliquée à B et C, mais pas à A**, dont le
> total était resté celui du forfait.
>
> La méthode uniforme du §7.1 — même construction pour les trois colonnes — donne **A 42 – 77**.
> Elle redonne au passage **C 42 – 84**, exactement la valeur publiée, ce qui confirme qu'elle
> reproduit bien ce qui avait été fait pour C.
>
> **Effet sur la lecture.** A cesse d'apparaître moins chère que C, et redevient son égale au
> plancher — ce qui est **exactement la conclusion que le document détaillé du 30/07 tirait déjà**
> de son propre tableau de comparaison. La correction rapproche les documents au lieu de les
> éloigner. **Elle ne change aucune des décisions proposées** : celles-ci ne reposaient pas sur un
> écart de coût entre A et C, mais sur la durée de vie de l'investissement et sur les risques.

> ### Une mise en garde de méthode, à conserver
>
> **Six des sept corrections apportées le 30/07 allaient dans le sens de la baisse pour C.** Ce
> n'est pas un résultat, c'est **un effet de la question posée** : l'instruction cherchait ce qui
> était **déjà acquis**. Une instruction symétrique — chercher ce qui manque — a produit d'emblée un
> poste supplémentaire dès qu'un document extérieur a été confronté à la matrice (D16).
>
> **Les grilles restent à re-coter en atelier**, et les fourchettes du §7 ne s'en trouvent pas
> mécaniquement abaissées : elles ont été posées indépendamment.

---

# Annexe C — Sources

### Sources externes

| Source | Relevée le |
|---|---|
| Apereo CAS — politique de maintenance : `https://apereo.github.io/cas/developer/Maintenance-Policy.html` | 29/07, confirmée le 30/07 |
| Apereo CAS — prérequis d'installation (JDK par ligne de version) : `https://apereo.github.io/cas/development/planning/Installation-Requirements.html` | 30/07 |
| Apereo CAS — publications : `https://api.github.com/repos/apereo/cas/releases/latest` | 29/07 |
| Apereo CAS — jalons : `https://github.com/apereo/cas/milestones` | 29/07 |
| Apereo CAS — documentation (branche `Development`) : `https://apereo.github.io/cas/development/` | 29/07 |
| Keycloak — publications : `https://api.github.com/repos/keycloak/keycloak/releases/latest` | 29/07 |
| Keycloak — guide d'administration 26.7 : `https://www.keycloak.org/docs/latest/server_admin/` | 29/07 |

### Analyse indépendante

**« Analyse de la cinématique de communication IAM ↔ CAS »** — analyse de la branche `develop`,
12 chapitres, diagrammes de séquence par fonction. Confrontée à la matrice le 30/07 : elle en
confirme les fonctions et le périmètre — notamment les **7 points d'échange `/iam/v1/cas/*`**, ce
qui corrobore **de source indépendante** la correction D1 — et elle a fait apparaître **deux
fonctions manquantes** (D16) ainsi que le comportement de **non-divulgation d'existence de compte**
(§3). *Document externe au dépôt.*

---

# Annexe D — Index des documents sources

Les documents ci-dessous sont conservés à titre historique et portent un bandeau renvoyant ici.
**Cette annexe dit ce que chacun contient d'unique**, de sorte qu'il ne soit pas nécessaire de les
rouvrir par précaution.

| Document | Nature | Ce qu'il porte d'unique | Divergences |
|---|---|---|---|
| `2026-07-27/observations.md` | notes de travail | Les **gabarits vides** des grilles de complexité, ancêtres des tableaux de l'annexe A. Analyse des trois modes d'usage de Keycloak (générique / découplé / intégré). Coût récurrent « 20-40 J/H » | « 20-30 jours » pour le précédent (D13) |
| `2026-07-27/synthese-fonctionnalites-cas-keycloak-vitamui.md` | analyse technique détaillée | La **matrice à 30 fonctionnalités** (reprise ici en §2.1), la volumétrie, la **réconciliation avec `matrice_CAS_vs_Keycloak.xlsx`** (13 cas d'usage manquants), l'annexe des classes par domaine | D1, D2, D3, D13, D18 |
| `2026-07-27/note-arbitrage-socle-authentification.md` | note de décision, 2 pages | Version courte de l'arbitrage à destination du COPIL | **D10 et D11 — les deux contredisent la position arrêtée le 28/07** |
| `2026-07-28/observations.md` | notes de travail | Le plan de rédaction du compte rendu ; formulation condensée du coût comparé des trois pistes | D12 |
| `2026-07-28/compte-rendu.md` | **compte rendu COPIL** | Le vocabulaire **« iso-fonctionnel »**, le tableau du **coût de l'iso-fonctionnel poste par poste**, le tableau **« dans quel cas chaque trajectoire est le bon choix »**, et les **décisions actées en séance** | D1, D2, D3, D13 |
| `2026-07-28/recapitulatif-complexites.md` | annexe technique | **Reproduction des grilles** de la synthèse du 27/07, plus la ligne « effort relatif normalisé ». Porte la mention « ce document ne circule pas au COPIL » | Grilles reprises en annexe A |
| `2026-07-29/rapport-technique-socle-authentification.md` | rapport technique | Le **comparatif produit à produit** (repris en §4), la **correction des 7 endpoints** (D1), la **veille produit datée**, les **15 réserves détaillées** (reprises en §8), le **mécanisme d'extension par collision de beans** | D2, D4, D13, D14, D15, D16 |
| `2026-07-30/matrice-decision-socle-authentification.md` | matrice de décision | La **matrice fonctionnelle macro** (reprise en §6), la **ventilation par fonction**, l'**estimation indépendante de C**, la **matrice des risques**, les **notes de partage IAM / CAS** | D6, D7 |

**Document maintenu à part, non absorbé** — `2026-07-30/synthese-decision-socle-authentification.md`,
support de séance d'une page. Il est l'**extrait officiel** du présent document et en reprend les
chiffres à l'identique.

**Hors périmètre de ce dossier** — les documents du 20 au 24/07 portent les travaux de faisabilité
eux-mêmes (analyse de découplage, HRD, fédération OIDC et SAML, points durs techniques et dette
accumulée). Ils relèvent d'un autre genre et d'un autre lectorat, et **ne sont pas remplacés par ce
document.** Le retour d'expérience du 24/07 reste la référence sur les 13 anomalies OIDC / SAML et
sur la dette du prototype.
