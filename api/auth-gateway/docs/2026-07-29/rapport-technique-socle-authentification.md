# Rapport technique — socle d'authentification VitamUI

> **Document historique — 29 juillet 2026.**
> Repris et actualisé par `api/auth-gateway/docs/2026-07-31/dossier-arbitrage-socle-authentification.md`, **qui fait foi**.
> Conservé pour la traçabilité.
>
> **Ce qui n'est plus exact dans ce document :**
> - **le scénario A chiffre une montée vers CAS 8.0** — la cible retenue depuis le 30/07 est la **ligne 7.3.x** (JDK 21) ; **la grille A ne s'applique pas telle quelle** ;
> - **X.509 en trajectoire C, coté 5 points** — la fonction est **abandonnée** depuis le 30/07 ;
> - **l'anti-force brute en C, cotée 2 points « non traité »** — elle est **dans IAM** et héritée par C ;
> - **la gestion du mot de passe en C, cotée 5 points « non traité »** — politique, historique et validateur sont **dans IAM** ; seul le parcours de réinitialisation reste à écrire ;
> - **deux fonctions manquent aux trois grilles** : la journalisation des connexions et l'administration à chaud des fournisseurs d'identité ;
> - **« ~25 jours »** pour le précédent CAS 6 → 7 — **50 à 60 jours** réels.
>
> - **La correction des 7 endpoints, le comparatif produit et les quinze réserves restent valides** et sont repris aux §2, §4 et §8 du document de référence.

---


*29 juillet 2026 — document technique — équipe, architecture, direction technique*

---

> ## Objet et ordre de lecture
>
> Ce rapport pose, dans l'ordre, **l'existant** (§1), **les produits** (§2), **les scénarios** (§3 à §5)
> et **les réserves** (§7). Il est autoporteur : il reprend intégralement les grilles de complexité
> présentées le 28/07 et devient le document de référence à leur sujet.
>
> ### La hiérarchie à respecter en le lisant
>
> **Les grilles des §3 à §5 chiffrent trois scénarios dont aucun n'a été réalisé : ce sont des
> hypothèses.** Les réserves du §7 sont des **constats** — établis par test, par lecture de code,
> ou par la documentation de l'éditeur.
>
> **En cas de contradiction entre les deux, ce sont les réserves qui doivent emporter la décision.**
> Un total de points se discute et se re-cote ; un mode de défaillance silencieux, une politique de
> maintenance de six mois ou un résultat de test négatif ne se re-cotent pas.
>
> ### Périmètre analysé
>
> Branche `discovery_16332` — module `cas/cas-server` (Apereo CAS **7.0.10.1**, overlay WAR),
> module `api/api-iam`, module `api/auth-server` (travaux de faisabilité), configuration de
> déploiement `deployment/roles/vitamui/templates/cas-server/application.yml.j2`.
>
> Les faits produit (§2) ont été relevés en ligne le **29/07/2026** et portent leur source.
>
> ### Convention
>
> Les points de complexité **ne sont pas convertibles en jours-homme**. Ils mesurent l'incertitude
> et l'effort relatif, pas une durée. Les valeurs sont une proposition de départ, à re-poser par
> l'équipe en atelier.

---

## 1. Ce que porte le socle aujourd'hui

### 1.1 Répartition `cas/cas-server` / `api-iam`

La question que ce tableau instruit n'est pas « que sait faire CAS ? », mais **« qui fait quoi
aujourd'hui ? »**. Trois colonnes : ce que fournit le produit CAS lui-même, ce que nous avons écrit
dans l'overlay, et ce que porte le module de gestion des identités.

> ⚠️ **Périmètre : le système en production uniquement.** Les travaux de faisabilité menés sur
> `api/auth-server` ont ajouté à IAM **5 endpoints `/iam/v1/cas/*`** qui ne sont appelés par aucun
> code de `cas-server`. Ils sont **exclus de ce tableau** — les y faire figurer laisserait croire
> que le socle actuel s'appuie dessus. Ils sont identifiés séparément en §1.3.

**Légende** — ✅ natif · ⚙️ natif mais configuration non triviale · ❌ absent du produit · — sans objet

| # | Fonctionnalité | Produit CAS | Spécifique `cas/cas-server` | Part `api-iam` |
|---|---|:--:|---|---|
| 1 | Authentification login / mot de passe | ✅ | `LoginPwdAuthenticationHandler`, `UserPrincipalResolver` (515 l.) | **Vérification du secret** — `POST /cas/login`, `findUserByEmailAndCustomerId`. CAS ne détient aucun mot de passe |
| 2 | Base d'identités | ⚙️ | — (*stateless* sur l'identité) | **Source de vérité** — Mongo `users`, `customers`, `groups`, `profiles`, `tenants` |
| 3 | Résolution HRD (e-mail → organisation + IdP) | ❌ | Orchestration : `DispatcherAction` (220 l.) | **Règles et données** — `patterns` d'e-mail sur `IdentityProvider`, appariement par `IdentityProviderHelper` (bibliothèque partagée `iam-commons`). **Le filtrage s'exécute dans `cas-server`** |
| 4 | Sélection d'organisation (même e-mail sur N organisations) | ❌ | `ListCustomersAction`, `CustomerSelectedAction`, `customerForm.html` | `GET /cas/customers` → `getCustomersByIds` |
| 5 | Multi-domaine à suffixe partagé | ❌ | Consommation du résultat | **Modèle** — `patterns: List<String>` sur `IdentityProvider`, résolution applicative |
| 6 | Webflow de login multi-étapes | ⚙️ | `VitamLoginWebflowConfigurer` (261 l.), `DispatcherAction`, `WebflowConfig`, 4 vues | — |
| 7 | Délégation **OIDC** entrante | ✅ (pac4j) | `ProvidersService`, `CustomDelegatedIdentityProviders`, `Pac4jClientIdentityProviderDto`, `CustomDelegatedClientAuthenticationAction`, `CustomOidcCasClientRedirectActionBuilder` — rechargement dynamique (1 min) | **Registre des IdP** — API standard `identity-providers` (`IdentityProvidersApi.getAll`, `embedded=KEYSTORE,IDPMETADATA`), stockage Mongo |
| 8 | Délégation **SAML 2** entrante | ✅ (pac4j + OpenSAML) | Idem, mécanisme mutualisé | `keystoreBase64`, `keystorePassword`, `idpMetadata` en base |
| 9 | Authentification par certificat **X509** | ✅ | `CustomRequestHeaderX509CertificateExtractor` (extraction depuis header nginx), `CertificateParser`, `X509AttributeMapping`, `X509CertificateAttributes`, `X509CasDelegatingWebflowEventResolver` | Résolution de l'utilisateur via `GET /cas/users` |
| 10 | MFA — OTP par SMS *(activation conditionnelle, non maintenue)* | ✅ (`simple-mfa` + `sms-smsmode`) | `VitamMfaWebflowConfigurer`, `CustomSendTokenAction`, `CheckMfaTokenAction`, vue « téléphone manquant » | Numéro de mobile porté par l'utilisateur |
| 11 | MFA — TOTP / WebAuthn / passkeys | ⚙️ | ❌ non implémenté | — |
| 12 | **Subrogation** compte générique | ⚙️ (`surrogate-authentication`) | `IamSurrogateAuthenticationService`, `CustomSurrogateInitialAuthenticationAction`, `InitializeSubrogationAction`, `SurrogateUsernamePasswordCredential` | `GET /cas/subrogations` → `getSubrogationsBySuperUser` |
| 13 | **Subrogation** nominative **avec validation** | ❌ | Orchestration webflow, propagation `superUserId` dans le principal | **Processus métier complet** — entité `Subrogation` (`CREATED` → `ACCEPTED`). La demande et l'acceptation passent par l'API subrogations d'IAM et l'IHM `identity` ; **CAS ne lit que les subrogations déjà acceptées** |
| 14 | Réinitialisation de mot de passe par e-mail | ✅ (`pm-webflow`) | `IamPasswordManagementService`, `ResetPasswordController`, `I18NSendPasswordResetInstructionsAction`, `PmTransientSessionTicketExpirationPolicyBuilder`, `PmMessageToSend` — **hack** `UserLoginModel` encodé en JSON dans `username` | `POST /cas/password/change` → `updatePassword` |
| 15 | Changement de mot de passe forcé | ✅ | `TriggerChangePasswordAction` + réécriture du `ticketGrantingTicketCheck` | Statut et date d'expiration du compte |
| 16 | Politique de mot de passe (ANSSI / custom) | ⚙️ | `InitPasswordConstraintsConfiguration`, messages fr/en/de | Contraintes servies par IAM ; validateur mutualisé `commons/commons-security` → `PasswordValidator` |
| 17 | Historique des mots de passe | ⚙️ | — | `max-old-password`, `CasService.updatePassword` |
| 18 | Provisioning **JIT** après fédération | ⚙️ | Déclenchement après authentification déléguée | Déclenché à la volée par `GET /cas/users/provisioning` — `autoProvisioningEnabled`, `defaultGroupId`. `cas-server` ne référence aucun code de JIT : tout se joue côté IAM |
| 19 | Provisioning depuis un annuaire externe | ❌ | — | `GET /cas/users/provisioning` → `provisionUser` + `ProvisioningClient` par IdP |
| 20 | SSO inter-applications (8 SPA) | ✅ (TGT + cookie TGC) | `DynamicTicketGrantingTicketFactory` — TTL variable selon le type de compte | Type de compte (`NOMINATIVE` / `GENERIC`) |
| 21 | Émission du **token applicatif `TOK-<UUID>`** | ❌ | `CustomOAuth20DefaultAccessTokenFactory` | **Émission et persistance** — produit **dans la réponse** de `GET /cas/users/provisioning` (`embedded=AUTHTOKEN`) → `generateAndAddAuthToken` / `persistToken`, collection `tokens`, TTL selon subrogation / API / compte générique |
| 22 | Serveur OIDC pour les 8 SPA | ✅ (`support-oidc`) | `CustomOidcCasClientRedirectActionBuilder`, `CustomOidcRevocationEndpointController`, `CustomCorsProcessor` | — |
| 23 | Registre des clients / services persistant | ✅ (`mongo-service-registry`) | Configuration ; collection `services` | — |
| 24 | Logout global / Single Logout | ✅ | `TerminateApiSessionAction`, `CustomDelegatedAuthenticationClientLogoutAction`, flag `propagateLogout` par IdP | `GET /cas/logout` → `removeTokenAndGetPrincipal`, `invalidateTokensOfUser`, purge de la subrogation en cours |
| 25 | Anti-brute force / throttling | ✅ | `cas.authn.throttle.*` | Compteur `nbFailedAttempts`, `updateNbFailedAttempsPlusLastConnectionAndStatus` |
| 26 | Blocage / désactivation de compte | ⚙️ | Contrôle dans `DispatcherAction`, vue `casAccountDisabledView` | `UserStatusEnum`, `checkStatus` |
| 27 | Thème & i18n de la mire (fr/en/de) | ⚙️ (Thymeleaf) | 20 fichiers surchargés (1 067 l.) + `overriden_messages_{fr,en,de}.properties` + CSS / polices / icônes | — |
| 28 | Audit métier (logbook `EXT_VITAMUI_*`) | ⚙️ (Inspektr) | — | **Écriture des événements** — `createEventsSubrogation`, `EXT_VITAMUI_START_SURROGATE_GENERIC`… |
| 29 | Modèle d'habilitations (profils, groupes, tenants, contrats) | ❌ | Portage dans le principal | **Entièrement IAM** — hors périmètre de l'IdP dans tous les scénarios |
| 30 | Console d'administration des utilisateurs | ❌ | — | IAM + SPA `identity` / `identityadmin` |

### 1.2 Volumétrie du spécifique

| Élément | Volume | Vérifié le 29/07 |
|---|---:|---|
| Classes Java `cas/cas-server/src/main/java` | **49 classes / 7 737 lignes** | ✔ |
| Fichiers Thymeleaf surchargés | **20 fichiers / 1 067 lignes** (19 vues + 1 gabarit `layout.html`) | ✔ |
| Exclusions Maven dans `cas-server/pom.xml` | **25** | ✔ |
| `CasController` + `CasService` (côté IAM) | **1 049 lignes** | ✔ *(voir note)* |
| Endpoints IAM dédiés consommés par `cas-server` | **7** | ✔ *(voir note)* |
| Points d'extension accrochés à des internes CAS / pac4j | **28** | — |

> **Note — 7 endpoints, et non 12 ou 13.** Les documents du 27 et du 28/07 annoncent
> « 13 endpoints » en prose et en énumèrent 12 en annexe. Les deux chiffres sont faux pour ce
> tableau, et pour deux raisons distinctes.
>
> **1. Le décompte brut est de 12, pas 13.** `CasController` porte 12 annotations de *mapping* et
> `RestApi` 12 constantes `CAS_*_PATH`.
>
> **2. Surtout, 5 de ces 12 endpoints n'existent que depuis les travaux de faisabilité.** Le
> `git diff develop...HEAD` sur `RestApi.java` montre l'ajout de `CAS_TOKENS_PATH`, `CAS_HRD_PATH`,
> `CAS_SUBROGATION_VALIDATE_PATH`, `CAS_IDP_PATH` et `CAS_USERS_JIT_PATH` — commits `poc:`,
> `mise en place HRD multi-provider`, `mise en place subrogation`, `mise en place d'IdP OIDC + JIT`,
> `mise en place du support IdP SAML`. **`cas-server` n'en appelle aucun.**
>
> Le socle en production s'appuie sur **7 endpoints**, portés par **1 049 lignes**
> (`CasController` 333 + `CasService` 716 sur `develop`). Les travaux de faisabilité y ont ajouté
> **+405 lignes** et **+5 endpoints** — ce qui donne les 1 454 lignes citées dans les documents
> antérieurs. **Ce total mélange l'existant et le POC**, il ne mesure pas le couplage CAS ↔ IAM.

### 1.3 Les 7 points d'échange `/iam/v1/cas/*` du socle actuel

Ces sept endpoints sont ceux que `cas-server` appelle réellement, via le client généré `casApi`.
Ils constituent la totalité de la surface `/cas/*` présente sur `develop`.

| Endpoint | Appel depuis `cas-server` | Rôle |
|---|---|---|
| `POST /cas/login` | `casApi.login` | Vérification du mot de passe, compteur d'échecs, statut de compte |
| `POST /cas/password/change` | `casApi.changePassword` | Changement de mot de passe + historique |
| `GET /cas/users?email=` | `casApi.getUsersByEmail` | Recherche multi-organisation (écran de sélection) |
| `GET /cas/users/provisioning` | `casApi.getUser` | Lookup, provisioning à la volée, **et émission du `TOK-<UUID>`** via `embedded=AUTHTOKEN` |
| `GET /cas/subrogations` | `casApi.getSubrogationsBySuperUserIdOrEmailAndCustomerId` | Subrogations **déjà acceptées** pour un super-utilisateur |
| `GET /cas/logout` | `casApi.logout` | Invalidation du `TOK-<UUID>` et purge de la subrogation en cours |
| `GET /cas/customers` | `casApi.getCustomersByIds` | Résolution des organisations |

`cas-server` consomme par ailleurs l'**API standard** `identity-providers` (`IdentityProvidersApi.getAll`)
pour charger les IdP toutes les minutes. Ce n'est pas une surface dédiée à CAS.

> **Les 5 endpoints ajoutés par les travaux de faisabilité — hors périmètre du tableau §1.1.**
>
> | Endpoint | Ajouté pour | Appelé par `cas-server` ? |
> |---|---|:--:|
> | `GET /cas/hrd?email=` | résolution HRD côté serveur | **non** |
> | `POST /cas/tokens` | émission explicite du token | **non** |
> | `POST /cas/subrogations/validate` | validation de subrogation | **non** |
> | `GET /cas/idp/{id}` | configuration d'un IdP *(renvoie les secrets — à corriger)* | **non** |
> | `POST /cas/users/jit` | provisioning JIT après fédération | **non** |
>
> Ils sont appelés **uniquement** par `api/auth-server` (`IamClient`). Les faire figurer dans la
> répartition des responsabilités du socle actuel donnerait à la trajectoire C un existant qu'elle
> n'a pas hérité : **elle l'a construit.** Conséquence traitée en §1.4 et en §7, réserve 14.

### 1.4 Ce que ce tableau fait apparaître

**La logique métier est déjà, en très large majorité, hors de CAS.** Modèle multi-domaine,
organisations, workflow de subrogation, émission et durée de vie du token, statut de compte,
historique de mot de passe, provisioning, habilitations : tout cela est écrit, testé et versionné
dans `api-iam`. La colonne `cas-server` de ces lignes ne contient pas de règle métier — elle
contient l'**appel** à IAM et le **placement de cet appel dans un état de webflow**.

**Une exception, qui mérite d'être nommée : la résolution HRD.** Les données et les règles
d'appariement vivent bien hors de CAS — `patterns` sur `IdentityProvider`, `IdentityProviderHelper`
— mais dans la bibliothèque **partagée** `iam-commons`, et **le filtrage s'exécute dans
`cas-server`**. C'est la seule des cinq fonctions irréductibles dont une part de traitement soit
réellement logée dans le composant d'authentification. Les travaux de faisabilité ont d'ailleurs
répondu à ce point en créant `GET /cas/hrd` — c'est-à-dire en déplaçant ce traitement vers IAM.

**Ce que `cas-server` détient en propre, c'est l'orchestration du parcours**, pas le métier :
`VitamLoginWebflowConfigurer` et `DispatcherAction` décident de l'enchaînement e-mail →
organisation → mot de passe, et de la branche à prendre selon le statut du compte, le type de
compte ou l'IdP résolu. C'est un séquencement d'écrans, exprimé dans le langage de webflow d'un
produit tiers.

Deux conséquences directes, qui pèsent sur tout le reste du rapport :

1. **C'est ce qui rend le découplage envisageable.** Sortir la sélection d'organisation et la
   subrogation du flux d'authentification ne demande pas de réécrire la logique — elle existe
   déjà côté IAM. Cela demande de déplacer l'orchestration.
2. **Les travaux de faisabilité n'ont pas seulement consommé cet existant, ils l'ont étendu.**
   `api/auth-server` réutilise les 7 endpoints ci-dessus, mais il a fallu en **ajouter 5** et
   écrire **+405 lignes** dans `CasController` et `CasService`. Une partie des « 2 jours » est
   donc du développement **côté IAM**, pas seulement côté serveur d'autorisation — et ce sont
   précisément ces endpoints qui portent aujourd'hui la dette de sécurité (§7, réserve 13). La
   mesure des 2 jours doit être lue en conséquence : voir §7, réserve 14.

---

## 2. Comparatif produit — CAS 8.0 vs Keycloak 26

### 2.1 État des deux produits au 29/07/2026

Ces faits ont été relevés en ligne le jour de la rédaction. Ils ne figuraient dans aucun document
antérieur du dossier, et ils en corrigent deux.

| Fait | Source |
|---|---|
| **CAS 8.0.0 est disponible depuis le 18/07/2026** — soit **11 jours** | API GitHub `apereo/cas`, `releases/latest` : `tag_name: v8.0.0`, `published_at: 2026-07-18`, `prerelease: false` |
| **Aucune ligne documentaire `8.0.x` n'est publiée** sur `apereo.github.io/cas` — le sélecteur de version ne propose que `7.3.x` et `Development` | Site de documentation Apereo |
| **8.0.1 n'est pas publiée** — jalon dû le 14/08/2026 | `github.com/apereo/cas/milestones` |
| **8.1.0-RC1 est à 94 %**, échéance 21/08/2026 | idem |
| **Politique de maintenance : 6 mois de support complet, puis 6 mois de correctifs de sécurité seuls** | `apereo.github.io/cas/developer/Maintenance-Policy.html` |
| **Absence de LTS assumée par le projet** : *« the CAS project can not offer LTS releases in a practical and sustainable sense »* | idem |
| **7.3.x est en correctifs de sécurité seuls depuis le 30/06/2026**, EOL total au 31/12/2026. Toute version absente du tableau de maintenance est déclarée EOL — ce qui inclut **notre 7.0.10.1**. *(Le tableau ne mentionne pas encore 8.0.x, publiée depuis 11 jours : il n'a pas été mis à jour, il ne s'agit pas d'un statut EOL.)* | idem |
| **Keycloak 26.7.0 publiée le 09/07/2026** — la ligne **26.x** est ouverte depuis le **04/10/2024** | API GitHub `keycloak/keycloak`, `releases/latest` et `tags/26.0.0` |

**Deux corrections au dossier.** Le dossier annonçait « 12 mois de support ». C'est exact comme
durée totale, mais **6 mois seulement** sont en support complet ; les 6 suivants ne reçoivent que
des correctifs de sécurité. Et l'absence de LTS n'est plus une inférence de notre part : c'est une
position affichée par l'éditeur, citable telle quelle.

**Un contraste de rythme, mesurable.** Keycloak tient sa ligne majeure **26.x depuis 21 mois** —
de 26.0.0 (04/10/2024) à 26.7.0 (09/07/2026), sans changement de majeure. Sur la même période, CAS
a imposé l'enchaînement **7.0 → 7.1 → 7.2 → 7.3 → 8.0**, et prépare déjà 8.1. Ce n'est pas un
jugement sur la qualité des deux produits : c'est une différence de contrat de maintenance, et
elle est structurelle.

### 2.2 Fonctionnalités des deux produits

Comparatif **produit à produit**, indépendamment de VitamUI. Aucune colonne « notre
implémentation » ici : elle fait l'objet du §1.

**Légende** — ✅ natif · ⚙️ natif mais configuration non triviale · ⚠️ partiel ou sémantique
différente · ❌ absent

| Fonction | CAS 8.0 | Keycloak 26.7 | Niveau de preuve |
|---|:--:|:--:|---|
| Protocole CAS (`/serviceValidate`, proxy tickets) | ✅ | ❌ | doc éditeur |
| Fournisseur OIDC / OAuth 2 (authorization code + PKCE) | ✅ | ✅ | doc éditeur |
| Fournisseur SAML 2 (IdP) | ✅ | ✅ | doc éditeur |
| Délégation OIDC entrante (*identity brokering*) | ✅ | ✅ | doc éditeur |
| Délégation SAML 2 entrante | ✅ | ✅ | doc éditeur |
| Authentification X.509 (certificat client) | ✅ | ✅ | doc éditeur |
| MFA — TOTP | ⚙️ | ✅ | doc éditeur |
| MFA — WebAuthn / passkeys | ⚙️ | ✅ | doc éditeur |
| MFA — **OTP par SMS** | ✅ (`SmsMode` parmi 10 fournisseurs) | ⚠️ **non natif** — `Authenticator SPI`, implémentations tierces courantes et peu coûteuses | doc éditeur, vérifié 29/07 |
| MFA — Duo, FIDO2, Google Authenticator | ✅ | ⚠️ (WebAuthn / TOTP, pas de Duo natif) | doc éditeur |
| Gestion du mot de passe (reset, changement forcé) | ✅ | ✅ | doc éditeur |
| Politique de mot de passe déclarative | ⚙️ (regex) | ✅ (politiques nommées) | doc éditeur |
| Historique des mots de passe | ⚙️ | ✅ | doc éditeur |
| **Subrogation / impersonation** | ✅ (`surrogate-*`, vérifié présent) | ⚠️ **impersonation d'administration** — acte unilatéral, sans consentement de la cible | doc éditeur |
| Provisioning JIT après fédération | ⚙️ | ✅ (*first broker login* configurable) | doc éditeur |
| Stockage d'identités externe | ⚙️ (LDAP, JDBC, REST…) | ✅ (LDAP, AD, SSSD, **User Storage SPI**) | doc éditeur |
| Registre de services / clients persistant | ✅ (Mongo, JPA…) | ✅ (base interne) | doc éditeur |
| Console d'administration | ❌ | ✅ | doc éditeur |
| API d'administration | ⚙️ | ✅ (Admin REST complète) | doc éditeur |
| SCIM | ⚙️ (intégration) | ✅ (26.7, *preview*) | doc éditeur |
| Anti-brute force | ✅ | ✅ | doc éditeur |
| Audit / événements extensibles | ⚙️ (Inspektr) | ⚙️ (Event Listener SPI) | doc éditeur |
| Thème & i18n de la mire | ⚙️ (Thymeleaf) | ⚙️ (thèmes FreeMarker, *login theme v2*) | doc éditeur |
| **Notion d'organisation** | ❌ | ⚠️ *Organizations* — **N organisations ne peuvent pas partager un domaine** | **testé le 27/07/2026**, résultat négatif |
| Modèle de déploiement | ⚠️ **overlay WAR à recompiler** à chaque montée de version | ✅ distribution binaire versionnée | doc éditeur |
| **Modèle d'extension** | ⚠️ **écrasement de beans internes** | ✅ **SPI déclarées et documentées** | analyse de code, voir 2.3 |
| **Contrat documentaire sur l'extension** | ❌ | ✅ | analyse, voir 2.3 |
| Cadence de version majeure | 12 mois imposés, pas de LTS | ligne 26.x ouverte depuis 21 mois | vérifié 29/07 |

### 2.3 La ligne décisive : comment on étend ces deux produits

Les deux dernières lignes fonctionnelles du tableau ne sont pas des fonctionnalités. Ce sont les
deux qui pèsent le plus sur le coût réel, et elles opposent les produits **par nature**, pas par
degré.

**CAS s'étend par collision de noms de beans.** Les points d'extension ne passent pas par une SPI
déclarée : ils passent par le **nom des méthodes `@Bean`**, qui doit correspondre exactement à un
nom de bean interne du produit.

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
interfaces déclarées, nommées, versionnées et documentées, avec un contrat de compatibilité
explicite. Une SPI qui change le fait visiblement.

**Pourquoi cet écart compte plus que les lignes fonctionnelles au-dessus.** Il ne change pas *ce
qu'on peut faire* avec chaque produit — il change **ce qui se passe quand le produit monte de
version**. Une SPI modifiée casse la compilation, bruyamment et tout de suite. Un bean renommé ne
casse rien du tout — c'est le mode de défaillance silencieux détaillé en §7, réserve 5.

**Ce que cet écart ne dit pas.** Il ne désigne pas Keycloak comme la bonne cible pour VitamUI.
Il dit que **si le choix se réduisait à « quel COTS plier ? », Keycloak serait le meilleur
candidat** — mieux outillé, mieux documenté, à cadence de version soutenable. C'est précisément ce
qui rend le résultat du test du 27/07 sur *Organizations* aussi lourd de conséquences : le
meilleur candidat à la personnalisation ne couvre pas davantage le modèle VitamUI que celui que
nous exploitons.

---

> ## Note de périmètre commune aux trois grilles — le MFA par SMS en est retiré
>
> Les grilles présentées le 28/07 cotaient le second facteur par SMS dans les trois colonnes
> (3 points en A, **8** en B, 3 en C). Cette ligne est **retirée des trois grilles**, pour deux
> raisons.
>
> **1. La fonctionnalité est probablement caduque.** Elle est d'origine externe (contribution
> Xelians), **n'est pas maintenue par l'équipe**, et son activation est conditionnelle au
> déploiement — condition `if sms.enabled` dans
> `application.yml.j2`, avec des identifiants smsMode livrés à `changeme`. Elle n'est
> fonctionnelle qu'avec un abonnement tiers actif.
>
> **2. Sa cotation en Keycloak était fausse.** Les 8 points reposaient sur l'absence de support
> natif. C'est exact, mais l'implémentation par `Authenticator SPI` est un cas d'école largement
> outillé : le coût réel est faible, sans commune mesure avec les 8 points portés. L'erreur
> pénalisait la trajectoire B à hauteur de 8 points sur 165.
>
> **Ce que ce retrait change** : A passe de 107 à **104**, B de 165 à **157**, C de 67 à **64**.
> **Les rapports normalisés sont inchangés** — 104/64 ≈ 1,6 et 157/64 ≈ 2,5. La conclusion
> comparative ne dépendait pas de cette ligne.
>
> **Ce que ce retrait n'est pas** : une décision d'abandon de la fonctionnalité. C'est une
> hypothèse de périmètre, à confirmer avec le métier au titre de l'arbitrage du §4 du compte rendu
> COPIL du 28/07. Si la fonction est conservée, elle se recote à faible coût dans les trois
> colonnes.

---

## 3. Scénario A — Mise à niveau vers CAS 8.0

> **La cible.** CAS 7.0.x, 7.1.x et 7.2.x sont EOL, 7.3.x est en correctifs de sécurité seuls
> jusqu'au 31/12/2026 : **8.0 est la seule cible réelle**, le saut ne peut pas être amorti par un
> palier intermédiaire.
>
> **Elle a 11 jours.** 8.0.0 est publiée depuis le 18/07/2026, 8.0.1 n'est pas encore sortie, et
> **aucune ligne documentaire `8.0.x` n'est publiée** — la seule documentation disponible pour la
> ligne 8 est celle de la branche `Development`, qui décrit désormais **8.1**. Nous vérifierions
> donc notre code contre une documentation qui ne décrit pas exactement la version cible.

### 3.1 Saut de plateforme

Le module `cas-server` importe déjà `cas-server-support-bom` : **l'alignement des versions de
plateforme est porté par le BOM**, ce n'est pas une série de migrations à conduire une par une.

| Poste | Notre situation | Cplx |
|---|---|---:|
| **Bascule effective sur le BOM CAS** — retrait des pins locaux qui l'empêchent de gouverner | le POM importe le BOM CAS en premier, mais re-déclare ensuite `spring.boot.version` (3.2.1), `jackson.version` (2.16.1), `groovy.version`, et pin les **8 artefacts pac4j** sur `${pac4j.version}` (6.3.3). Ces pins sont ce qui transforme aujourd'hui une montée de version en migrations parallèles. | 3 |
| 5 sauts successifs (7.0 → 7.1 → 7.2 → 7.3 → 8.0), pilotés par `cas.version` | **sans exemple migré cette fois** ; chaque palier peut casser le démarrage | 5 |
| **25 exclusions Maven à revalider** | des coordonnées d'artefacts ont pu bouger d'une version à l'autre ; une exclusion devenue obsolète est **silencieuse** | 5 |
| **JDK 21 → 25 (obligatoire)** | **hors périmètre du BOM** — monorepo en Java 21 : build, CI, packaging, JVM de l'hôte, validation exploitation | 8 |
| Tomcat 10 → 11 | vient avec Spring Boot 4 *via* le BOM | 1 |
| ~40 propriétés `cas.*` | **validation stricte** : toute propriété renommée empêche le démarrage | 3 |
| **Sous-total plateforme** | | **25** |

> **Note de méthode — pourquoi ce sous-total est passé de 60 à 25 le 28/07.** La version initiale
> cotait « Spring Boot 3.2 → 4 / Spring Security 7 » (13), « Jackson 2.16 → 3.x » (8) et
> « pac4j 6.3.3 → 7 » (13) comme des postes de plateforme. C'était une erreur à double titre.
>
> **1. Le BOM fait l'alignement des versions.** Monter `cas.version` amène la plateforme cohérente
> avec elle, à condition de retirer les pins locaux. Ce n'est pas un chantier de migration, c'est
> une opération de configuration.
>
> **2. Ce qui coûte n'est pas la version, c'est l'impact sur *notre* code** — et cet impact était
> **déjà compté en 3.2**, où chaque poste porte explicitement les ruptures d'API correspondantes.
> Les 34 points retirés étaient comptés deux fois.
>
> **Ce que cette correction ne change pas** : le risque de rupture silencieuse sur les 28 points
> d'extension accrochés à des noms de beans internes. Le BOM aligne des versions ; il ne dit pas si
> `defaultAccessTokenFactory` existe encore. C'est la raison pour laquelle le sous-total suivant
> reste inchangé.

### 3.2 Reprise du spécifique

*Le code existe et est testé, mais les ruptures se découvrent à l'exécution.*

| Cas d'usage à adapter | Point de départ | Cplx |
|---|---|---:|
| Webflow de connexion | `VitamLoginWebflowConfigurer`, `DispatcherAction`, `ListCustomersAction`, `CustomerSelectedAction` | 13 |
| Délégation OIDC / SAML2 | `ProvidersService`, `CustomDelegatedIdentityProviders` — le plus exposé aux ruptures pac4j 7 | 13 |
| Subrogation (les deux modes) | API `surrogate-*` sujette à changement | 8 |
| Gestion du mot de passe | + suppression du hack `UserLoginModel` dans `username` | 5 |
| X509 | `CustomRequestHeaderX509CertificateExtractor` et le mapping d'attributs | 3 |
| Tickets & tokens (`TOK-<UUID>`, TGT factory) | `CustomOAuth20DefaultAccessTokenFactory`, `DynamicTicketGrantingTicketFactory` | 5 |
| Logout / Single Logout | `TerminateApiSessionAction` | 3 |
| 19 vues Thymeleaf | alignement sur les templates CAS 8 | 5 |
| **Sous-total spécifique** | | **55** |

> ⚠️ **Le précédent disponible ne se rejouera pas.** Les ~25 jours de la migration CAS 6 → 7 ont
> été réalisés **avec une application déjà migrée en exemple** — un corpus de référence sur lequel
> s'appuyer à chaque rupture. Pour 7.0 → 8.0, cet exemple **n'existera pas**. À périmètre de code
> égal, le temps de réalisation sur ce sous-total est donc à considérer comme un plancher.

### 3.3 Transverse

| Poste | Cplx |
|---|---:|
| Packaging, intégration initiale | 8 |
| Déploiement / configuration / supervision | 3 |
| Recette complète (X509, MFA, subrogation, mot de passe, SSO, SLO, 8 clients) | 13 |
| **Sous-total transverse** | **24** |
| **Grand total (3.1 + 3.2 + 3.3)** | **104** |

> ⚠️ **Ce que ce total achète : 6 mois de support complet, puis 6 mois de correctifs de sécurité.**
> Apereo écrit ne pas pouvoir offrir de version à support long. 8.1.0-RC1 est due le 21/08/2026 —
> soit avant même qu'une migration de ce volume puisse être livrée et recettée. **Le cycle est à
> repayer, sur du code qui étend 28 classes internes du produit.**

> ⚠️ **Ce total est le moins fiable des trois.** Les 28 points d'extension s'accrochent à des noms
> de beans internes non documentés, avec un mode de défaillance **silencieux**. Les postes à
> 13 points ci-dessus ne signifient pas « on sait que c'est difficile », mais **« on ne sait pas ce
> qu'on ne sait pas »**. Voir §7, réserves 5 et 6.

---

## 4. Scénario B — Transposition vers Keycloak 26

Transposer dans Keycloak ce que `cas-server` fait aujourd'hui, à périmètre fonctionnel constant.

| Cas d'usage | Standard Keycloak | Point de départ | Cplx |
|---|:--:|---|---:|
| Authentification login / mot de passe | ✅ | base users à trancher : Keycloak ou Mongo IAM (User Storage SPI) — **décision structurante** | 13 |
| Résolution HRD e-mail → organisation | ❌ | *Organizations* **écarté par test** ⇒ Authenticator SPI obligatoire | 13 |
| Sélection d'organisation (N comptes / e-mail) | ❌ | Authenticator SPI + thème custom | 13 |
| Multi-domaine à suffixe partagé | ❌ | **testé : non couvert** — contrainte à contourner intégralement en SPI | 13 |
| Délégation OIDC | ✅ | IdP dynamiques : provisionner Keycloak depuis IAM (Admin REST) | 5 |
| Délégation SAML2 | ✅ | idem, mécanisme mutualisé avec l'OIDC | 3 |
| X509 | ✅ | authenticator x509 à configurer + extraction header nginx à revalider | 5 |
| Subrogation compte générique | ⚠️ | impersonation admin + token exchange à évaluer | 8 |
| Subrogation avec validation | ❌ | Authenticator SPI + workflow IAM conservé | 13 |
| Gestion du mot de passe (reset, forcé, politique, historique) | ✅ | à recâbler sur la base d'identités retenue | 5 |
| Émission `TOK-<UUID>` | ❌ | couche d'émission maison **ou** migration JWT des resource servers | 13 |
| SSO + Single Logout | ✅ | | 2 |
| Thème & i18n de la mire | ⚙️ | 19 vues à retranscrire en thème FreeMarker (ou SPA) | 8 |
| Audit / logbook VitamUI | ⚙️ | Event Listener SPI → IAM | 3 |
| Initialisation du super-admin | 🔧 | realm + compte + script de promotion en base VitamUI | 3 |
| **Sous-total reprise des cas d'usage** | | | **120** |
| Packaging, intégration initiale | | | 8 |
| Déploiement / configuration / supervision | | | 8 |
| Tests d'intégration généraux | | | 8 |
| Migration des données (users, IdP, secrets) | | | 13 |
| **Grand total** | | | **157** |

> ⚠️ **157 est un plancher, pas une estimation.** Cette trajectoire subit la même pénalité de faible
> levier de l'assistance IA que le scénario A, **sans bénéficier d'aucun code existant à adapter**.
> Voir §7, réserve 10.

> **Ce que ce total achète en revanche** : une ligne majeure ouverte depuis 21 mois, une
> distribution binaire sans overlay à recompiler, un modèle d'extension par SPI documentées, et
> des fonctions que nous n'avons pas aujourd'hui (MFA moderne, console d'administration, SCIM).
> Ces apports sont réels ; ils ne portent simplement pas sur le périmètre métier VitamUI.

---

## 5. Scénario C — Développement sur Spring Authorization Server

*État d'avancement à jour du 24/07/2026.* Le module `api/auth-server` est **déjà sur la plateforme
cible** (Spring Boot 4, Spring Security 7, Java 21) : aucun saut de plateforme à financer. Les
7 cas d'usage acquis l'ont été **en 2 jours** — cette mesure établit la **faisabilité** des sept
fonctions sur cette API, **pas une vitesse d'exécution généralisable** (voir §7, réserve 14).

| Cas d'usage | État d'avancement | Cplx |
|---|---|---:|
| Authentification login / mot de passe | ✅ validé | 0 |
| Résolution HRD + sélection d'organisation (N>1) | ✅ validé | 0 |
| Délégation OIDC externe | ✅ validé end-to-end (Keycloak) | 0 |
| Délégation SAML2 externe | ✅ validé end-to-end (Keycloak) | 0 |
| Provisioning JIT | ✅ validé | 0 |
| Émission `TOK-<UUID>` | ✅ contrat préservé | 0 |
| Multi-clients OIDC (8 SPA) | ✅ | 0 |
| Subrogation | ✅ implémenté, test manuel en attente | 1 |
| SSO inter-applications | ⚠️ blocage identifié (session perdue entre deux `/oauth2/authorize`) — **débogage, peu compressible** | 2 |
| **Sécurisation du canal SAS ↔ IAM** | ❌ **bloquant** — `runAsSystem(level="")` = bypass total, endpoints IAM whitelistés | 5 |
| Registre de clients persistant | ❌ `InMemoryRegisteredClientRepository` | 2 |
| Persistance `OAuth2AuthorizationService` | ❌ non traité | 2 |
| Logout / end-session consolidé | ❌ non traité | 3 |
| Gestion du mot de passe | ❌ non traité | 5 |
| Mire Angular `auth-ui` | ❌ SPA vanilla provisoire — **UI réelle, faible levier IA** | 8 |
| X509 | ❌ non traité — portage du code CAS existant, pas une invention | 5 |
| Throttling / anti-brute force | ❌ non traité (natif CAS) | 2 |
| Thème & i18n fr/en/de de la mire | ❌ messages existants à porter | 3 |
| Dette de sécurité des travaux de faisabilité | ❌ 7 points (secrets chiffrés, découpage `/cas/idp/{id}`, refresh `idpMetadata`, audit CSRF `SameSite=none`, logs) | 5 |
| **Sous-total reprise des cas d'usage** | | **43** |
| Packaging, intégration (Ansible, Consul) | | 5 |
| Déploiement / configuration / supervision | | 3 |
| Tests d'intégration généraux — **calendaire, non compressible** | | 13 |
| **Grand total** | | **64** |

> **Contrepartie assumée** : tout ce que CAS offrait nativement — X509, gestion du mot de passe,
> throttling — est à réécrire. Le sous-total « cas d'usage » se compte en jours ;
> l'intégration et la recette se comptent en semaines.

---

## 6. Synthèse comparée des trois scénarios

| | A — CAS 8 | B — Keycloak | C — SAS |
|---|---:|---:|---:|
| Reprise des cas d'usage | 80 | 120 | **43** |
| Intégration, déploiement, recette | 24 | 37 | 21 |
| **Total** | **104** | **≥ 157** | **64** |
| Nature dominante du travail | **internes COTS** | internes COTS (SPI) | **code neuf, API publique** |
| Levier de l'assistance IA | **faible** — 25 j pour la dernière montée, *avec* exemple | faible | **fort** — API publique et corpus massif |
| Exemple / précédent disponible | **non** (contrairement à CAS 6 → 7) | non | les travaux de faisabilité déjà menés |
| Acquis à date | code existant à adapter | 0 | **7 cas d'usage validés** |
| Ce que le total achète | **6 mois de support complet + 6 mois de sécurité** | une cible durable, mais base users déplacée | une cible durable, code maîtrisé |
| Couplage métier résiduel | **inchangé** (webflow CAS) | SPI Keycloak | à traiter dans notre code |
| Effort relatif normalisé | ≈ 1,6× | ≈ 2,5× | **1×** |

### 6.1 Comment lire ces totaux

**1. L'écart entre A et C est plus grand que le rapport des points** (104 / 64 ≈ 1,6), sans être
d'un autre ordre de grandeur. Le travail de A relève de l'adaptation aux internes d'un COTS, où le
levier de l'assistance IA est faible ; celui de C relève majoritairement du code neuf contre une
API publique, où il est fort. Mais l'avantage ne joue que sur une partie de C : **moins d'un tiers
de ses 64 points restants relève du code neuf**, le reste étant du portage et de l'intégration non
compressible. **L'avantage décisif de C n'est donc pas sa vitesse d'exécution, c'est qu'il n'y a
rien à repayer au cycle suivant.**

**2. C est le moins complexe parce que 7 cas d'usage sont déjà validés** — dont la fédération OIDC
et SAML, les plus redoutés — et parce que le reste est majoritairement du portage de code existant.

**3. A est le seul dont le total soit à repayer**, et le seul qui ne réduise pas le couplage
métier : à l'arrivée, les cinq irréductibles sont toujours dans le webflow CAS.

**4. B est un plancher.** Même pénalité de levier IA que A, sans code existant à réutiliser.

---

## 7. Les réserves — ce qui prime sur le chiffrage

**Pourquoi cette section prime sur les trois précédentes.** Un point de complexité est une
**hypothèse** : personne n'a réalisé aucun des trois scénarios, et les valeurs ci-dessus sont une
proposition de départ que l'équipe doit re-coter. Les quinze réserves qui suivent sont d'une autre
nature : ce sont des **constats**, établis par test, par lecture de code, ou par la documentation
de l'éditeur.

Une réserve ne se re-cote pas en atelier. Elle se lève, ou elle reste.

### 7.1 Réserves transverses

**1 — Les points ne sont pas convertibles en jours-homme.**
*Constat.* L'échelle est Fibonacci et mesure l'incertitude autant que l'effort.
*Pourquoi elle prime.* Toute conversion en jours ou en euros produirait un chiffre faux, présenté
avec une précision qu'il n'a pas. Les totaux servent à **comparer les trois colonnes entre elles**,
pas à budgéter l'une d'entre elles.

**2 — Le niveau de preuve est inégal, et cela avantage mécaniquement C.**

| Scénario | Niveau de preuve |
|---|---|
| **A — CAS 8** | documentation éditeur et analyse du code existant ; **aucun essai de montée de version mené** |
| **B — Keycloak** | documentation, **+ un point testé** (multi-domaine à suffixe partagé), résultat **négatif** |
| **C — SAS** | travaux de faisabilité réels sur 7 cas d'usage, validés end-to-end |

*Pourquoi elle prime.* **Un scénario mesuré paraît toujours plus sûr qu'un scénario estimé, y
compris quand il ne l'est pas.** Les inconnues de C sont levées parce qu'elle a été explorée ;
celles de A et B restent devant. C'est un biais de méthode, et il joue en faveur de la trajectoire
que l'équipe recommande — raison de plus pour l'énoncer.

**3 — Ce qui ne se compresse dans aucun scénario.**
*Constat.* Recette avec de vrais fournisseurs d'identité, packaging Ansible, mire, revue de
sécurité, et débogage des défauts qui n'apparaissent qu'au premier login réel — le retour
d'expérience du 24/07 en recense **13** sur les seuls chantiers OIDC et SAML.
*Pourquoi elle prime.* C'est ce bloc qui domine le calendrier, pas le développement. Il explique
pourquoi un écart de points ne se traduit pas proportionnellement en délai de livraison.

**4 — La matrice `matrice_CAS_vs_Keycloak.xlsx` n'est pas utilisable en comparatif.**
*Constat.* Elle cote de 1 à 5 (linéaire) là où ce document cote en Fibonacci : les totaux ne sont
pas commensurables. Surtout, **son onglet CAS chiffre une montée de version mineure qui n'existe
plus** — il suppose `cas.version` déjà en 7.0.10.1 et « pas d'API breaking connue », d'où des
complexités à 1 sur OIDC, SAML, X509, MFA et SLO. Or 7.0.x, 7.1.x et 7.2.x sont EOL.
*Pourquoi elle prime.* Son total de 31 pour CAS circule et paraît rassurant. Il chiffre une
opération qui n'est pas disponible.

### 7.2 Réserves propres au scénario A

**5 — Le mode de défaillance est silencieux.**
*Constat.* Si CAS 8 renomme `defaultAccessTokenFactory` : notre `@Bean` n'écrase plus rien, il
enregistre un bean supplémentaire inutilisé. **Pas d'erreur de compilation. Pas d'erreur au
démarrage.** CAS utilise silencieusement sa factory par défaut, `TOK-<UUID>` n'est plus émis, et
les resource servers rejettent les tokens. Le défaut se découvre **en recette**, au mieux.
*Comment il a été établi.* Lecture de `AppConfig.java` et de `application.properties:62`
(`spring.main.allow-bean-definition-overriding=true`) — 28 points d'accroche recensés. Le phénomène
n'est pas hypothétique : le retour d'expérience du 24/07 documente le même mécanisme sur les
travaux SAS (*« Bean shadowing token generator : `OpaqueVitamTokenGenerator` retiré du
component-scan pour éviter le fallback SAS par défaut »*).
*Pourquoi elle prime.* Aucune valeur de complexité ne peut représenter un risque dont on ne sait
pas s'il s'est matérialisé. **Aggravée par le §2.1 : il n'existe pas de documentation `8.0.x`
publiée** — la seule référence disponible décrit la branche de développement, désormais 8.1.

**6 — Le coût n'est pas borné.**
*Constat.* Les notes de version 8.0 documentent le passage à JDK 25, Jackson 3, la validation
stricte des propriétés. Elles **ne documentent pas** les changements sur les API webflow,
délégation pac4j, surrogate et password management — soit précisément les quatre surfaces dont
dépend l'intégralité de notre spécifique.
*Pourquoi elle prime.* Les postes à 13 points du §3.2 ne disent pas « on sait que c'est
difficile ». Ils disent **« on ne sait pas ce qu'on ne sait pas »**. C'est une différence de nature
avec un poste à 5 points du scénario C, où le code de départ est le nôtre et l'API cible publiée.

**7 — Le cycle est à repayer, et plus vite que le dossier ne le disait.**
*Constat.* 6 mois de support complet, puis 6 mois de correctifs de sécurité seuls. Le projet écrit
ne pas pouvoir offrir de LTS. 8.1.0-RC1 est due le 21/08/2026, à 94 % d'avancement.
*Pourquoi elle prime.* **Aucun résultat de mesure ne peut contredire ce fait.** Un essai de montée
de version préciserait un coût ; il ne changerait pas la cadence imposée. La question posée par A
n'est pas « combien coûte la montée de version ? », mais **« acceptons-nous de la refaire chaque
année ? »** — et cette question se tranche sans essai.

**8 — Le précédent ne se rejouera pas.**
*Constat.* Les ~25 jours de CAS 6 → 7 ont été réalisés avec une application déjà migrée en exemple.
Pour 7.0 → 8.0, cet exemple n'existera pas.
*Pourquoi elle prime.* Le seul repère chiffré dont l'équipe dispose sur ce type de travail a été
obtenu dans des conditions **plus favorables** que celles à venir. Il constitue un plancher, pas
une prévision.

**9 — La connaissance acquise ne se transfère pas.**
*Constat.* Les internes non documentés s'apprennent en lisant le source et en exécutant. Ils ne se
transmettent ni par la documentation, ni par la revue de code.
*Pourquoi elle prime.* Le savoir des 25 jours vit chez la personne qui les a faits. C'est un risque
de *bus factor* qui s'ajoute au coût récurrent, et il ne figure dans aucune grille.

### 7.3 Réserves propres au scénario B

**10 — 157 est un plancher, pas une estimation.**
*Constat.* Même pénalité de levier IA que A — adaptation aux internes d'un COTS — **sans aucun code
existant à réutiliser**.
*Pourquoi elle prime.* C'est le seul total des trois dont on sait qu'il est sous-évalué, sans
savoir de combien.

**11 — Une décision structurante n'est pas tranchée.**
*Constat.* Keycloak devient-il la source de vérité des identités, ou branche-t-on un
`User Storage SPI` sur le Mongo IAM ? Le tableau du §4 cote cette ligne à 13 sans choisir.
*Pourquoi elle prime.* Le choix emporte la migration des données, le problème du double référentiel
et la réversibilité de l'ensemble. Il devrait être instruit **avant** que le total ne soit
considéré comme signifiant.

**12 — Le sur-mesure est déplacé, pas supprimé.**
*Constat.* *Organizations* a été **écarté par test le 27/07/2026** : N organisations ne peuvent pas
partager un même domaine. Les fonctions #3, #4 et #5 relèvent donc toutes d'un développement
d'`Authenticator SPI`.
*Pourquoi elle prime.* Elle invalide l'hypothèse d'un « Keycloak en configuration », qui était la
justification première du scénario. **Transposer les 7 737 lignes en SPI Keycloak, c'est changer de
COTS sans changer de problème.**

### 7.4 Réserves propres au scénario C

**13 — La dette de sécurité est active, et documentée dans le code.**
*Constat.* `CasService.runAsSystem(level="")` constitue un bypass complet, et **7 endpoints
`/iam/v1/cas/*` sont whitelistés** dans `WebSecurityConfig` — `hrd`, `tokens`, `login`,
`subrogations/validate`, `idp/*`, `users/provisioning`, `users/jit`. Le commentaire du code le dit
lui-même : *« Production hardening (mTLS / signed header) is deferred to Phase 2. »*
**Cinq de ces sept endpoints ont été créés par les travaux de faisabilité eux-mêmes** (§1.3) : la
dette n'est pas héritée du socle actuel, elle a été introduite avec le prototype. `GET /cas/idp/{id}`
est le plus exposé — il renvoie `clientSecret`, `keystoreBase64` et `keystorePassword` sans
authentification.
*Pourquoi elle prime.* C'est **bloquant avant toute mise en service**, et c'est la première des
trois conditions non négociables posées avec la recommandation du 28/07. Les 5 points portés au
tableau du §5 chiffrent le travail ; ils ne disent pas qu'il conditionne la mise en production.

**14 — L'avantage de preuve n'est pas un avantage de fond.**
*Constat.* Les 7 cas d'usage validés en 2 jours ont réutilisé les 7 endpoints IAM existants,
**mais il a fallu en ajouter 5 et écrire +405 lignes dans `CasController` et `CasService`**
(§1.2, §1.3). La logique métier n'a pas été réécrite — elle a été rebranchée — mais la surface
d'API, elle, a bien été développée.
*Pourquoi elle prime.* Deux effets, en sens opposés, qu'il faut tenir ensemble.
**Elle amoindrit la mesure** : les 2 jours ne portent pas que sur le serveur d'autorisation, ils
incluent du développement côté IAM, et cette part n'est pas transposable telle quelle au reste du
périmètre — d'autant que ce qui reste (mire Angular, X509, gestion du mot de passe, recette) est
précisément là où le levier est le plus faible.
**Elle la conforte aussi** : ces 5 endpoints ne sont pas un contournement du modèle IAM, ce sont
des points d'accès explicites à une logique qui existait déjà. Le fait qu'il ait suffi de 405 lignes
pour exposer HRD, token, subrogation validée, IdP et JIT confirme le diagnostic du §1.4 — **le métier
était bien dans IAM ; ce qui manquait, c'était le contrat d'accès.** Ce constat vaut pour les trois
trajectoires, pas seulement pour C.
En tout état de cause, cette mesure établit la **faisabilité** des sept fonctions sur l'API Spring
Authorization Server. Elle n'établit pas une vitesse d'exécution généralisable.

**15 — Nous devenons responsables du composant.**
*Constat.* Plus d'éditeur pour porter les évolutions du socle d'authentification : la compétence
doit être maintenue en interne, dans la durée.
*Pourquoi elle prime.* C'est une condition non négociable de la recommandation du 28/07 : sans
elle, **la recommandation bascule sur le scénario A**. Cette réserve ne se chiffre pas en points ;
elle se traite en organisation.

---

## 8. Ce qui reste hors des trois colonnes

Le chantier de **découplage** — sortir la sélection d'organisation et la subrogation du flux
d'authentification pour en faire des processus métier IAM — est volontairement **exclu des trois
grilles** : il est commun aux trois scénarios, donc neutre dans la comparaison. **À compter une
seule fois, en plus.**

Le §1.4 montre qu'il est techniquement plus accessible qu'il n'y paraît : la logique existe déjà
côté IAM, c'est l'orchestration qui est à déplacer. Les travaux de faisabilité en fournissent une
mesure indirecte — **405 lignes** ont suffi à exposer en API la résolution HRD, l'émission du
token, la validation de subrogation, la configuration d'IdP et le provisioning JIT (§1.3). Ce
chiffre ne couvre pas le chantier complet, mais il indique l'ordre de grandeur du travail
d'exposition, à distinguer du travail de refonte du parcours utilisateur.

**Mais il n'est pas neutre fonctionnellement.** Il suppose de reconnaître qu'une partie du parcours
de connexion actuel n'a pas à vivre dans l'authentification, ce qui **implique un changement
visible par l'utilisateur**. Il relève donc d'un arbitrage de périmètre — traité au §4 du compte
rendu COPIL du 28/07 — et non d'une décision technique que l'équipe pourrait prendre seule.

---

## Documents liés

- **Compte rendu COPIL — document de décision** (28/07) :
  `api/auth-gateway/docs/2026-07-28/compte-rendu.md`
- **Récapitulatif des complexités** (28/07) — **repris et remplacé par le présent rapport** pour
  ce qui concerne les grilles :
  `api/auth-gateway/docs/2026-07-28/recapitulatif-complexites.md`
- **Synthèse fonctionnelle détaillée** (27/07) — matrice à 30 lignes, réconciliation avec la
  matrice de coûts diffusée en parallèle :
  `api/auth-gateway/docs/2026-07-27/synthese-fonctionnalites-cas-keycloak-vitamui.md`
- **Note d'arbitrage — version courte** (27/07) :
  `api/auth-gateway/docs/2026-07-27/note-arbitrage-socle-authentification.md`
- **Retour d'expérience des travaux de faisabilité** — fédération OIDC + SAML (24/07) :
  `api/auth-gateway/docs/2026-07-24/retex-federation-oidc-saml.md`

### Sources externes relevées le 29/07/2026

- Apereo CAS — publications : `https://api.github.com/repos/apereo/cas/releases/latest`
- Apereo CAS — jalons : `https://github.com/apereo/cas/milestones`
- Apereo CAS — politique de maintenance : `https://apereo.github.io/cas/developer/Maintenance-Policy.html`
- Apereo CAS — documentation (branche `Development`) : `https://apereo.github.io/cas/development/`
- Keycloak — publications : `https://api.github.com/repos/keycloak/keycloak/releases/latest`
- Keycloak — guide d'administration 26.7 : `https://www.keycloak.org/docs/latest/server_admin/`
