# Synthèse fonctionnelle — Apereo CAS / Keycloak / implémentation VitamUI actuelle

> **Document historique — 27 juillet 2026.**
> Repris et actualisé par `api/auth-gateway/docs/2026-07-31/dossier-arbitrage-socle-authentification.md`, **qui fait foi**.
> Conservé pour la traçabilité.
>
> **Ce qui n'est plus exact dans ce document :**
> - **« 13 endpoints »** en prose, **12** en annexe A — le chiffre retenu est **7** endpoints consommés par `cas-server`, plus 5 créés par le prototype (correction du 29/07) ;
> - **le scénario A chiffre une montée vers CAS 8.0** — la cible retenue depuis le 30/07 est la **ligne 7.3.x**, qui supprime le saut de JDK ; la grille A ne s'applique pas telle quelle ;
> - **« 12 mois de support »** — 6 mois complets puis 6 mois de sécurité seuls ;
> - **« ~25 jours »** pour le précédent — **50 à 60 jours** réels ;
> - **les totaux 107 / 165 / 67** — devenus 104 / 157 / 64 après retrait du MFA par SMS le 29/07 ;
> - ce document, bien que daté du 27/07, porte une mention **« Révision du 28/07 »** : son contenu n'est pas figé à sa date.

---


> **Objet** — Cartographier, fonctionnalité par fonctionnalité, ce qui est **porté nativement par CAS**, ce qui serait **porté nativement par Keycloak**, et ce qui relève de **notre code spécifique** (module `cas/cas-server` + surface `/cas` du module `api-iam`). Objectif : disposer d'une base factuelle pour arbitrer entre les trois scénarios (garder CAS, remplacer par Keycloak, développer sur Spring Authorization Server) — **la genèse de ce jeu d'options est expliquée en §2**.
>
> **Périmètre analysé** — branche `discovery_16332`, CAS **7.0.10.1** (overlay WAR, Spring Boot 3.2.1), module `api-iam`, configuration de déploiement `deployment/roles/vitamui/templates/cas-server/application.yml.j2`.
>
> **Colonne Keycloak** — établie à partir de la documentation Keycloak 26.x, **sauf sur le point le plus décisif, qui a été testé**.
>
> ✅ **Résultat de test (27/07/2026)** — la fonctionnalité **Organizations** (organisations + domaines + IdP rattachés + *identity-first login*) **ne permet pas à N organisations de partager un même nom de domaine**. Le besoin VitamUI du multi-domaine à suffixe partagé (#5) n'est donc **pas couvrable en configuration** : il impose un développement d'`Authenticator SPI`. Ce point n'est plus une réserve, c'est un constat.
>
> Le reste de la colonne Keycloak demeure établi sur documentation et dépend de la version cible retenue — en particulier l'impersonation (#12), le MFA SMS (#10) et le *first broker login* (#18), qui n'ont pas été éprouvés.
>
> **Statut des travaux `api/auth-server`** — les développements menés sur ce module sont des **travaux de faisabilité** : ils visent à vérifier ce qui est techniquement réalisable, et à quel coût. Ils sont de même nature que la validation technique restant à mener sur Keycloak, simplement menés en premier. **Ils n'engagent aucune décision et ne préjugent pas du scénario retenu.**
>
> **Niveau de preuve par scénario** — inégal, et il faut le savoir en lisant les totaux :
>
> | Scénario | Niveau de preuve |
> |---|---|
> | **A — CAS 8** | documentation éditeur et analyse du code existant ; **aucun essai de montée de version mené** |
> | **B — Keycloak** | documentation, **+ un point testé** : le multi-domaine à suffixe partagé (#5), résultat **négatif** |
> | **C — SAS** | travaux de faisabilité réels sur 7 cas d'usage, validés end-to-end |
>
> Cet écart tient à ce qui a été exploré à ce jour, non à un jugement de valeur sur les scénarios. **Le seul scénario dont aucune hypothèse n'a encore été éprouvée est A** : un essai de montée de version CAS 8 sur une branche jetable est ce qui manque pour ramener les trois colonnes au même niveau de preuve.

---

## 1. Architecture actuelle en une page

```
                    ┌──────────────────────────────────────────┐
   8 SPA Angular    │  cas/cas-server  (Apereo CAS 7.0.10.1)   │
   (portal, identity│                                          │
    referential…)   │  • webflow de login RÉÉCRIT              │
        │           │      email → [organisation] → mot de passe│
        │  OIDC     │  • délégation OIDC / SAML2 (pac4j)        │
        └──────────►│  • X509 (header nginx)                   │
                    │  • MFA simple (SMS smsmode)              │
                    │  • password management                   │
                    │  • surrogation (subrogation)             │
                    └───────────────┬──────────────────────────┘
                                    │  REST  (CasApi, openapi client)
                                    │  ~13 endpoints /iam/v1/cas/*
                                    ▼
                    ┌──────────────────────────────────────────┐
                    │  api-iam  (source de vérité métier)      │
                    │  users, customers, groups, profiles,     │
                    │  tenants, subrogations, identity-providers│
                    │  + collection `tokens`  (TOK-<UUID>)     │
                    └───────────────┬──────────────────────────┘
                                    │  header X-Auth-Token: TOK-…
                                    ▼
                    ┌──────────────────────────────────────────┐
                    │  Resource servers (portal-api, ingest…)  │
                    └──────────────────────────────────────────┘
```

**Le point structurant** : CAS ne fait pas que de l'authentification. Le webflow CAS a été réécrit pour y **faire entrer des concepts métier VitamUI** (organisation/`customer`, subrogation, statut de compte, type de compte `NOMINATIVE`/`GENERIC`). C'est la source du couplage et le cœur du coût de migration, quelle que soit la cible.

**Volumétrie du spécifique** :

| Élément | Volume |
|---|---:|
| Java custom `cas/cas-server/src/main/java` | **7 737 lignes** (49 classes) |
| Templates Thymeleaf surchargés | **1 067 lignes** (19 vues) |
| Exclusions Maven dans `cas-server/pom.xml` | **25** |
| Côté IAM : `CasController` + `CasService` | **1 454 lignes** |
| Endpoints IAM dédiés à CAS | **13** |

---

## 2. Genèse : pourquoi trois scénarios et non deux

### 2.1 Le point de départ

La question initiale était binaire : **garder CAS, ou le remplacer par Keycloak.** Le raisonnement était classique — CAS coûte cher à maintenir, Keycloak est le standard du marché, on change de COTS.

**Ce qui a réellement déclenché la remise en question est plus concret que le coût annuel agrégé** :

> ⚠️ **Aucune intervention sur CAS n'a jamais tenu dans un sprint de 3 semaines.** Ni une modification fonctionnelle, ni une montée de version. Le coût a systématiquement débordé du cycle de développement.

C'est un problème de nature différente d'un problème de budget. Une brique dont **chaque** évolution déborde de l'itération n'est pas planifiable : elle ne peut être ni engagée en début de sprint, ni découpée en incréments livrables, ni arbitrée face à d'autres sujets à parité. Elle impose son propre rythme au projet.

La montée CAS 6 → 7 en est l'illustration la plus nette : **~25 jours**, soit près du double d'un sprint — et cela **avec** une application déjà migrée en exemple et l'assistance de l'IA.

C'est cette non-planifiabilité, croisée à l'analyse du couplage (§1), qui a fait apparaître un troisième terme.

### 2.2 Le raisonnement en trois temps

**1. Si l'on doit recréer la même adhérence dans Keycloak, on passe à côté de quelque chose.**

Les 7 737 lignes de spécifique ne sont pas un accident d'intégration : elles portent des concepts métier — organisation multi-domaine, subrogation validée, token opaque — qu'aucun produit d'authentification ne modélise. Les transposer en `Authenticator SPI` Keycloak, c'est **changer de COTS sans changer de problème**. Le test du 27/07 sur *Organizations* (#5) confirme que cette transposition serait bien nécessaire, et non évitable par configuration.

**2. Cela dit, à adhérence égale, customiser Keycloak reste plus rationnel que customiser CAS.**

Keycloak est plus largement déployé, mieux documenté, doté d'une communauté plus large et d'un corpus d'exemples sans commune mesure. Si le choix se réduisait à « quel COTS plier ? », **Keycloak serait le meilleur candidat** — l'écart de qualité documentaire joue directement sur le coût, et cet écart est analysé en détail en §5.

**3. Mais si l'on est *obligé* de plier un COTS à ce point, le signal ne porte pas sur le COTS.**

Il porte sur notre conception. Un produit d'authentification qui doit héberger la sélection d'organisation et un workflow de subrogation validée est un produit à qui l'on demande autre chose que de l'authentification. **Le besoin exprimé n'est pas un besoin d'authentification, c'est un besoin métier logé au mauvais endroit.**

### 2.3 D'où l'émergence du scénario C

Plutôt que de plier un COTS à un modèle qu'il ne connaît pas, s'appuyer sur une **brique du framework déjà en usage** — Spring, présent dans l'ensemble de la plateforme — et assumer le spécifique là où il vit déjà : dans notre code.

La logique est inverse des deux autres : au lieu d'importer un produit et de l'adapter au métier, on prend une bibliothèque de protocoles et on l'habille du métier. **Quand le fonctionnel est très spécifique, c'est le rapport de forces le plus favorable.**

### 2.4 Ce que les trois scénarios représentent réellement

Le choix n'est pas un choix d'éditeur. C'est un choix sur **le lieu où vit la logique spécifique** :

| Scénario | Où vit le spécifique | Pari sous-jacent |
|---|---|---|
| **A — CAS 8** | overlay CAS *(inchangé)* | le statu quo reste moins cher que le changement |
| **B — Keycloak** | SPI et thèmes Keycloak | un COTS mieux outillé absorbera le métier à moindre coût |
| **C — SAS** | notre base de code | le métier nous appartient ; l'authentification n'est qu'une brique |

### 2.5 La question que cette genèse laisse ouverte

Le scénario C n'est pas meilleur du seul fait d'être né en dernier — il complète le jeu d'options, il ne le tranche pas.

Surtout, **le constat du temps 3 reste vrai quel que soit le scénario retenu** : si notre modèle métier oblige à customiser lourdement tout produit d'authentification du marché, c'est une question de conception qui mérite d'être posée pour elle-même. C'est précisément l'objet du chantier de **découplage (phase 0)** — sortir organisation et subrogation du flux d'authentification pour en faire des processus métier IAM (§4.4). Ce chantier est commun aux trois scénarios, et c'est le seul qui traite la cause plutôt que le symptôme.

---

## 3. Matrice de synthèse

**Légende** — ✅ natif, prêt à l'emploi · ⚙️ natif mais nécessite configuration non triviale · ⚠️ partiel : le socle existe, l'écart fonctionnel doit être comblé · ❌ absent du produit · 🔧 développement spécifique VitamUI

*Convention de renvoi dans ce document* : `#N` renvoie à la ligne N de cette matrice, `§N` à la section N du document.

| # | Fonctionnalité | CAS 7 standard | Keycloak 26 standard | Notre implémentation actuelle |
|---|---|:--:|:--:|---|
| 1 | Authentification login / mot de passe | ✅ | ✅ | 🔧 `LoginPwdAuthenticationHandler` délègue la vérification à IAM (`POST /cas/login`) — CAS ne détient **aucun** mot de passe |
| 2 | Base d'identités / stockage utilisateurs | ⚙️ (LDAP, JDBC, REST…) | ✅ (interne, LDAP, Kerberos, User Storage SPI) | 🔧 **Mongo IAM** est la source de vérité ; CAS est *stateless* sur l'identité |
| 3 | Résolution de l'identité (HRD email → organisation + IdP) | ❌ | ⚠️ *Organizations* + *identity-first login* — fonctionne **tant qu'un domaine n'appartient qu'à une organisation** (cf. #5) | 🔧 `ListCustomersAction`, `IdentityProviderHelper.findByUserIdentifierAndCustomerId`, `patterns` d'e-mail sur `IdentityProvider` |
| 4 | Écran de sélection d'organisation (même e-mail sur N organisations) | ❌ | ❌ (1 domaine ↔ 1 organisation par realm) | 🔧 `customerForm.html`, `CustomerSelectedAction`, `CasService.resolveHrdEntries` |
| 5 | Multi-domaine par organisation | ❌ | ❌ **testé le 27/07/2026** : plusieurs domaines par organisation **oui**, mais **N organisations ne peuvent pas partager un même domaine** — le besoin VitamUI n'est pas couvert | 🔧 champ `patterns: List<String>` sur `IdentityProvider`, résolution applicative |
| 6 | Webflow de login multi-étapes (e-mail → org → mot de passe) | ⚙️ (webflow Spring extensible) | ⚙️ (Authenticator SPI + thème FreeMarker) | 🔧 `VitamLoginWebflowConfigurer` (261 l.) + `DispatcherAction` (220 l.) + 4 vues |
| 7 | Authentification déléguée **OIDC** (identity brokering) | ✅ (pac4j) | ✅ | 🔧 chargement **dynamique** des IdP depuis Mongo (`ProvidersService`, rechargement 1 min) au lieu de la config statique CAS |
| 8 | Authentification déléguée **SAML 2** | ✅ (pac4j + OpenSAML) | ✅ | 🔧 idem : `keystoreBase64` / `idpMetadata` stockés en base, `CustomDelegatedIdentityProviders` |
| 9 | Authentification par certificat client **X509** | ✅ | ✅ (authenticator x509 navigateur/direct grant) | 🔧 `CustomRequestHeaderX509CertificateExtractor` (extraction depuis header nginx/Apache), `CertificateParser`, `X509AttributeMapping`, `X509CasDelegatingWebflowEventResolver` |
| 10 | MFA — OTP par SMS | ✅ (`simple-mfa` + `sms-smsmode`) | ⚠️ TOTP/WebAuthn/passkeys natifs ; **SMS non natif** (extension ou service tiers) | 🔧 `VitamMfaWebflowConfigurer`, `CustomSendTokenAction`, `CheckMfaTokenAction`, vue « téléphone manquant » |
| 11 | MFA — TOTP / WebAuthn / passkeys | ⚙️ (modules dédiés à activer) | ✅ | ❌ non implémenté |
| 12 | **Subrogation** compte générique (sans validation) | ⚙️ (`surrogate-authentication`) | ⚠️ impersonation admin uniquement (console/REST), sémantique différente | 🔧 `IamSurrogateAuthenticationService`, `CustomSurrogateInitialAuthenticationAction`, `InitializeSubrogationAction`, `SurrogateUsernamePasswordCredential` |
| 13 | **Subrogation** nominative **avec validation** par le subrogé | ❌ | ❌ | 🔧 workflow métier IAM complet : `Subrogation` (`CREATED` → `ACCEPTED`), `POST /cas/subrogations/validate`, propagation `superUserId` dans le principal |
| 14 | Réinitialisation de mot de passe par e-mail | ✅ (`pm-webflow`) | ✅ | 🔧 `IamPasswordManagementService`, `ResetPasswordController`, `I18NSendPasswordResetInstructionsAction` (i18n de l'e-mail) — **hack** : `UserLoginModel` JSON encodé dans le champ `username` pour transporter le `customerId` |
| 15 | Changement de mot de passe forcé (expiration / première connexion) | ✅ | ✅ | 🔧 `TriggerChangePasswordAction` + réécriture du `ticketGrantingTicketCheck` |
| 16 | Politique de mot de passe (longueur, classes, occurrences, profil ANSSI) | ⚙️ (regex `password-policy-pattern`) | ⚙️ (password policies déclaratives) | 🔧 `InitPasswordConstraintsConfiguration`, `PasswordValidator`, messages par langue (fr/en/de), profils `anssi` / `custom` |
| 17 | Historique des mots de passe (N derniers interdits) | ⚙️ | ✅ (`passwordHistory`) | 🔧 `max-old-password` géré côté IAM (`CasService.updatePassword`) |
| 18 | Provisioning **JIT** après fédération | ⚙️ | ✅ (*first broker login* configurable) | 🔧 `autoProvisioningEnabled` + `defaultGroupId` sur l'IdP, `POST /cas/users/jit`, `CasService.jitProvisionUser` |
| 19 | Provisioning depuis un annuaire externe à la connexion | ❌ | ⚠️ via User Storage SPI / LDAP | 🔧 `CasService.provisionUser` + `ProvisioningClient` par IdP |
| 20 | SSO inter-applications (8 SPA) | ✅ (TGT + cookie TGC) | ✅ | ⚙️ configuration ; `DynamicTicketGrantingTicketFactory` pour un TTL variable selon le type de compte |
| 21 | Émission du **token applicatif opaque `TOK-<UUID>`** | ❌ | ❌ (JWT ; introspection possible) | 🔧 `CustomOAuth20DefaultAccessTokenFactory` + `POST /cas/tokens` → collection Mongo `tokens`, consommée par tous les resource servers via `X-Auth-Token` |
| 22 | Serveur OIDC pour les frontends (authorization code + PKCE) | ✅ (`support-oidc`) | ✅ | ⚙️ configuration + `CustomOidcCasClientRedirectActionBuilder`, `CustomOidcRevocationEndpointController` (révocation pour client public) |
| 23 | Registre des clients / services persistant | ✅ (`mongo-service-registry`) | ✅ | ⚙️ collection `services` en Mongo |
| 24 | Logout global / Single Logout | ✅ (front & back channel) | ✅ (OIDC back/front-channel, SAML SLO) | 🔧 `TerminateApiSessionAction` (invalide le `TOK-…` côté IAM via `GET /cas/logout`), `CustomDelegatedAuthenticationClientLogoutAction`, flag `propagateLogout` par IdP |
| 25 | Anti-brute force / throttling | ✅ | ✅ (brute force detection) | ⚙️ `cas.authn.throttle.*` + compteur `nbFailedAttempts` côté IAM (`updateNbFailedAttempsPlusLastConnectionAndStatus`) |
| 26 | Blocage/désactivation de compte | ⚙️ | ✅ | 🔧 `UserStatusEnum` IAM, contrôle dans `DispatcherAction`, vue `casAccountDisabledView` |
| 27 | Thème & i18n de la mire (charte VitamUI, fr/en/de) | ⚙️ (Thymeleaf) | ⚙️ (thèmes FreeMarker) | 🔧 19 templates + `overriden_messages_{fr,en,de}.properties` + CSS/fonts/icônes VitamUI |
| 28 | Audit métier (logbook VitamUI, événements `EXT_VITAMUI_*`) | ⚙️ (Inspektr, format propre) | ⚙️ (Event Listener SPI) | 🔧 événements écrits par IAM (`EXT_VITAMUI_START_SURROGATE_GENERIC`, etc.) |
| 29 | Modèle d'habilitations (profils, groupes, tenants, contrats) | ❌ | ⚠️ rôles/groupes/authz — **pas** le modèle VitamUI | 🔧 **entièrement IAM** ; hors périmètre de l'IdP dans tous les scénarios |
| 30 | Console d'administration des utilisateurs | ❌ | ✅ | 🔧 SPA `identity` / `identityadmin` (VitamUI) |

---

## 4. Lecture des écarts, fonctionnalité par fonctionnalité

### 4.1 Les fonctionnalités qu'aucun COTS ne porte (🔧 dans les trois scénarios)

Ce sont les **irréductibles** : elles resteront du développement quel que soit le choix.

| Fonctionnalité | Pourquoi aucun COTS ne la porte |
|---|---|
| **Sélection d'organisation** (#4) | Un même e-mail correspond à N comptes dans N organisations. Les protocoles OIDC/SAML supposent un identifiant unique par realm. Ni CAS ni Keycloak n'ont ce concept. |
| **Multi-domaine avec suffixe partagé** (#5) | **Vérifié par test le 27/07/2026** : Keycloak *Organizations* n'autorise pas N organisations sur un même domaine ; CAS n'a pas la notion. Aucun des deux COTS ne couvre le besoin. |
| **Subrogation avec validation du subrogé** (#13) | C'est un **processus métier** (demande → acceptation → session subrogée traçée), pas un mécanisme d'authentification. L'impersonation Keycloak est un acte d'administration, sans consentement de la cible. |
| **Token opaque `TOK-<UUID>`** (#21) | Contrat propriétaire, adossé à la collection Mongo `tokens` que lisent **tous** les resource servers. Aucun IdP ne l'émettra nativement ; il faut soit une couche d'émission maison, soit migrer les ~10 resource servers vers du JWT. |
| **Modèle d'habilitations** (#29) | Profils / groupes / tenants / contrats d'accès sont propres à VitamUI et restent dans IAM. |

**Conséquence directe** : le scénario « on remplace CAS par Keycloak et c'est réglé » n'existe pas. Dans tous les cas, il faut une **couche d'adaptation** entre l'IdP et le modèle métier VitamUI.

### 4.2 Les fonctionnalités où Keycloak est plus fort que CAS

| Fonctionnalité | Apport Keycloak |
|---|---|
| MFA moderne (#11) | TOTP, WebAuthn, passkeys, codes de récupération natifs et administrables par l'utilisateur. Aujourd'hui : SMS uniquement. |
| Console d'administration (#30) | Admin console + Admin REST complets — mais qui **ne remplacent pas** les écrans VitamUI, puisqu'ils ignorent le modèle métier. |
| Provisioning JIT / *first broker login* (#18) | Flow configurable (revendication de compte existant, vérification d'e-mail, mapping d'attributs) au lieu de code. |
| Historique de mot de passe, politiques (#16-17) | Déclaratif, sans code. |
| Coût de maintenance | Distribution binaire versionnée, pas d'overlay WAR à recompiler ni 25 exclusions Maven à maintenir à chaque montée de version. |

### 4.3 Les fonctionnalités où CAS est aujourd'hui plus fort (ou déjà payé)

| Fonctionnalité | Situation |
|---|---|
| MFA par SMS (#10) | Natif CAS (`sms-smsmode`) ; **non natif Keycloak** → extension à développer ou service tiers. |
| Subrogation (#12) | CAS a un module `surrogate-authentication` que l'on étend. Keycloak part de plus loin (impersonation admin ≠ subrogation métier). |
| X509 (#9) | Les deux le portent, mais l'intégration nginx (extraction du certificat depuis un header) et le mapping d'attributs sont **déjà écrits et éprouvés** côté CAS. |
| Coût déjà engagé | ~7 700 lignes de spécifique fonctionnel et testé (24 classes de test). Toute cible impose de le retranscrire. |

### 4.4 Les points de couplage à traiter *avant* toute migration

Indépendants de la cible, ils conditionnent le coût de tous les scénarios :

1. **Concepts métier dans le webflow d'authentification** — sélection d'organisation et subrogation sont implémentées comme des *états de webflow CAS*. Elles devraient être des **processus métier** exposés par IAM et consommés par la mire.
2. **13 endpoints IAM dédiés à CAS** (`/iam/v1/cas/*`) — surface d'API taillée pour CAS, à généraliser en contrat « IdP ↔ métier ».
3. **Le token `TOK-<UUID>`** — contrat implicite avec ~10 resource servers. À figer explicitement (adaptateur) ou à migrer (JWT), décision structurante.
4. **Secrets d'IdP en clair dans Mongo** (`clientSecret`, `keystoreBase64`, `keystorePassword`) — à chiffrer, quel que soit le scénario.
5. **`GET /cas/idp/{id}` renvoie les secrets** — à découper en endpoint public / endpoint interne authentifié.
6. **Couplage par écrasement de beans internes CAS** — 28 points d'extension accrochés à des noms de beans non documentés, avec un mode de défaillance **silencieux**. Ce point est traité à part : **voir §5**, car il ne conditionne pas seulement le coût, il conditionne la *prévisibilité* du coût.

---

## 5. Commentaire — la documentation développeur de CAS, facteur de coût structurel

> Cette section est un **commentaire d'analyse**, pas un relevé de configuration. Elle documente la cause mécanique de l'écart de productivité constaté par l'équipe et devrait être lue avant toute décision d'engagement sur le scénario A.

### 5.1 Le constat

**CAS documente exhaustivement ses propriétés de configuration `cas.*`, et pas sa surface d'extension Java.** Des milliers de propriétés référencées, aucun guide d'extension. Or nos 7 737 lignes de spécifique reposent sur **28 classes internes** de CAS et pac4j étendues ou implémentées — c'est-à-dire sur une surface que le projet ne s'engage pas à documenter, ni à stabiliser.

Ce n'est pas un défaut de qualité rattrapable par plus d'effort de lecture : c'est une **propriété structurelle du contrat documentaire** du projet.

### 5.2 Le mécanisme de couplage réel : collision de noms de beans

Les points d'extension ne passent pas par une SPI déclarée. Ils passent par le **nom des méthodes `@Bean`**, qui doit correspondre exactement à un nom de bean interne de CAS :

```java
// cas/cas-server/.../config/AppConfig.java
@Bean
@RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
public PrincipalResolver defaultPrincipalResolver(...)          // ← nom interne CAS
public TicketGrantingTicketFactory defaultTicketGrantingTicketFactory(...)
public OAuth20AccessTokenFactory defaultAccessTokenFactory(...)
public PasswordManagementService passwordChangeService(...)
public DelegatedIdentityProviders delegatedIdentityProviders(...)
```

Aucun `@ConditionalOnMissingBean`, aucun `@Primary`, aucun `@Bean(name = …)` explicite. Et pour que le mécanisme fonctionne, la protection de Spring contre les définitions dupliquées doit être **désactivée globalement** :

```properties
# cas/cas-server/src/main/resources/application.properties:62
spring.main.allow-bean-definition-overriding=true
```

Deux beans vont plus loin encore : `surrogatePrincipalResolver` et `x509SubjectDNPrincipalResolver` se contentent de retourner le resolver par défaut — pure neutralisation d'un comportement interne de CAS, connaissance qui ne s'obtient qu'en lisant le code source du produit.

### 5.3 Le mode de défaillance : silencieux

C'est le point le plus lourd de conséquences. Si CAS 8 renomme `defaultAccessTokenFactory` :

1. Notre `@Bean` **n'écrase plus rien** — il enregistre un bean supplémentaire, inutilisé.
2. **Pas d'erreur de compilation.** Pas d'erreur au démarrage.
3. CAS utilise silencieusement sa factory par défaut.
4. `TOK-<UUID>` n'est plus émis ; les ~10 resource servers rejettent les tokens.
5. Le défaut se découvre **en recette**, au mieux.

Ce mode de défaillance n'est pas hypothétique : le retex du 24/07 documente le même phénomène lors des travaux de faisabilité SAS — *« Bean shadowing token generator : `OpaqueVitamTokenGenerator` retiré du component-scan pour éviter le fallback SAS par défaut »*. Même famille de bug, même retour silencieux au comportement par défaut du framework.

### 5.4 La confirmation par les notes de version CAS 8.0

Les notes de version 8.0 documentent le passage à JDK 25, Jackson 3, la suppression d'Undertow, la validation stricte des propriétés, le retrait de spring-retry.

Elles **ne documentent pas** les changements sur les API **webflow**, **délégation pac4j**, **surrogate** et **password management** — soit précisément les quatre surfaces dont dépend l'intégralité de notre spécifique.

Symptôme convergent dans notre propre code : les commentaires du type

```java
// CUSTO: instead of STATE_ID_GATEWAY_REQUEST_CHECK, send to ACTION_STATE_CHECK_SUBROGATION
```

existent parce que le code doit se documenter lui-même, faute d'un framework qui documente ses points d'accroche.

### 5.5 Trois conséquences sur l'arbitrage

**1. L'estimation du scénario A n'est pas seulement élevée : elle est non bornée.** On ne peut pas cadrer un travail dont les ruptures ne se découvrent qu'à l'exécution. Les postes à 13 points de la grille A ne signifient pas « on sait que c'est difficile », mais « on ne sait pas ce qu'on ne sait pas ». C'est une différence de **nature** avec un poste à 5 points du scénario C, où le code de départ est le nôtre et l'API cible est publiée.

**2. Contre-intuitivement, l'assistance IA creuse l'écart au lieu de le réduire.** Le levier de l'IA suit la densité de documentation et de corpus :

| | Documentation | Corpus | Levier IA | Mesure équipe |
|---|---|---|---|---|
| Spring Security / SAS | référence + javadoc | massif | **fort** | 7 cas d'usage en **2 j** |
| Internes CAS | absente sur l'extension | quasi nul, spécifique à chaque version | **faible** | CAS 6 → 7 en **~25 j** *avec exemple* |

**Plus l'équipe industrialise le développement assisté par IA, plus le scénario CAS devient relativement défavorable.** L'inverse de l'intuition courante, qui voudrait que l'IA rende les migrations indolores.

**3. Cette connaissance ne se transfère pas.** Les internes non documentés s'apprennent en lisant le source et en exécutant ; ils ne se transmettent ni par la documentation ni par la revue de code. Le savoir acquis pendant les 25 jours de CAS 6 → 7 vit chez la personne qui les a faits. C'est un risque de *bus factor* qui s'ajoute au coût récurrent de 20-40 J/H par an.

### 5.6 Ce que ce commentaire ne dit pas

Pour rester équilibré : le source de CAS est ouvert et lisible, la migration CAS 6 → 7 **a été faite**, et l'équipe a constitué un savoir réel sur ces internes. Le scénario A n'est pas impossible — il est **imprévisible**, et son coût est porté par une connaissance non transférable. C'est un profil de risque, pas une impasse technique.

---

## 6. Grille de complexité comparée

Les trois scénarios sont évalués sur la **même liste de cas d'usage** et la **même échelle Fibonacci**, pour être comparables entre eux.

**Échelle de complexité**

| Points | Signification |
|---:|---|
| **1** | trivial — configuration, aucune inconnue |
| **2** | simple — cadré, un seul composant |
| **3** | standard — bien compris, plusieurs fichiers |
| **5** | significatif — plusieurs composants, quelques inconnues |
| **8** | complexe — inconnues techniques réelles, exploration nécessaire |
| **13** | très complexe — risque élevé, à découper avant de s'engager |
| **0** | acquis — déjà réalisé et validé |

> ⚠️ **Ces points ne sont pas convertibles en J/H.** Ils mesurent l'incertitude et l'effort relatif, pas une durée. Les valeurs posées ici sont une **proposition de départ** issue de l'analyse de code de ce document : elles doivent être re-posées par l'équipe en atelier (planning poker), où le débat sur les écarts vaut plus que les chiffres eux-mêmes.

### Scénario A — Monter de version CAS 7.0.10.1 → 8.0

> CAS 7.0.x, 7.1.x et 7.2.x sont **EOL** (aucun patch, y compris de sécurité). 7.3.x est en patches de sécurité seuls jusqu'au 31/12/2026. **8.0 est la seule version CAS ayant un avenir** — le saut ne peut pas être évité par une montée intermédiaire.

**A.1 — Saut de plateforme imposé par CAS 8**

*Révision du 28/07 — voir la note de méthode sous le tableau.* Le module `cas-server` importe déjà `cas-server-support-bom` : **l'alignement des versions de plateforme est porté par le BOM**, ce n'est pas une série de migrations à conduire une par une.

| Poste | Notre situation | Cplx |
|---|---|---:|
| **Bascule effective sur le BOM CAS** — retrait des pins locaux qui l'empêchent de gouverner | le POM importe le BOM CAS en premier, mais re-déclare ensuite `spring.boot.version` (3.2.1), `jackson.version` (2.16.1), `groovy.version`, et pin les **8 artefacts pac4j** sur `${pac4j.version}` (6.3.3). Ces pins sont ce qui transforme aujourd'hui une montée de version en migrations parallèles. | 3 |
| 5 sauts successifs (7.0 → 7.1 → 7.2 → 7.3 → 8.0), pilotés par `cas.version` | **sans exemple migré cette fois** ; chaque palier peut casser le démarrage | 5 |
| **25 exclusions Maven à revalider** | des coordonnées d'artefacts ont pu bouger d'une version à l'autre ; une exclusion devenue obsolète est **silencieuse** | 5 |
| **JDK 21 → 25 (obligatoire)** | **hors périmètre du BOM** — monorepo en Java 21 : build, CI, packaging, JVM de l'hôte, validation exploitation | 8 |
| Tomcat 10 → 11 | vient avec Spring Boot 4 *via* le BOM | 1 |
| ~40 propriétés `cas.*` | **validation stricte** : toute propriété renommée empêche le démarrage | 3 |
| **Sous-total plateforme** | | **25** |

> **Note de méthode — pourquoi ce sous-total passe de 60 à 25.** La version initiale cotait « Spring Boot 3.2 → 4 / Spring Security 7 » (13), « Jackson 2.16 → 3.x » (8) et « pac4j 6.3.3 → 7 » (13) comme des postes de plateforme. C'était une erreur à double titre.
>
> **1. Le BOM fait l'alignement des versions.** Monter `cas.version` amène la plateforme cohérente avec elle, à condition de retirer les pins locaux. Ce n'est pas un chantier de migration, c'est une opération de configuration.
>
> **2. Ce qui coûte n'est pas la version, c'est l'impact sur *notre* code** — et cet impact était **déjà compté en A.2**, où chaque poste porte explicitement les ruptures d'API correspondantes (webflow, délégation pac4j, surrogate, password management). Les 34 points retirés ici étaient donc comptés deux fois.
>
> **Ce que cette correction ne change pas** : le risque de rupture silencieuse sur les 28 points d'extension accrochés à des noms de beans internes (§5). Le BOM aligne des versions ; il ne dit pas si `defaultAccessTokenFactory` existe encore. C'est la raison pour laquelle A.2 reste inchangé à 58.

**A.2 — Reprise du spécifique (adaptation : le code existe et est testé, mais les ruptures se découvrent à l'exécution)**

| Cas d'usage à adapter | Point de départ | Cplx |
|---|---|---:|
| Webflow de connexion | `VitamLoginWebflowConfigurer`, `DispatcherAction`, `ListCustomersAction`, `CustomerSelectedAction` | 13 |
| Délégation OIDC / SAML2 | `ProvidersService`, `CustomDelegatedIdentityProviders` — le plus exposé aux ruptures pac4j 7 | 13 |
| Subrogation (les deux modes) | API `surrogate-*` sujette à changement | 8 |
| Gestion du mot de passe | + suppression du hack `UserLoginModel` dans `username` | 5 |
| X509 | `CustomRequestHeaderX509CertificateExtractor` et le mapping d'attributs | 3 |
| MFA SMS | natif CAS, faible adhérence | 3 |
| Tickets & tokens (`TOK-<UUID>`, TGT factory) | `CustomOAuth20DefaultAccessTokenFactory`, `DynamicTicketGrantingTicketFactory` | 5 |
| Logout / Single Logout | `TerminateApiSessionAction` | 3 |
| 19 templates Thymeleaf | alignement sur les templates CAS 8 | 5 |
| **Sous-total spécifique** | | **58** |

> ⚠️ **Le précédent disponible ne se rejouera pas.** Les ~25 jours de la migration CAS 6 → 7 ont été réalisés **avec une application déjà migrée en exemple** — un corpus de référence sur lequel s'appuyer à chaque rupture. Pour 7.0 → 8.0, cet exemple **n'existera pas**. À périmètre de code égal, le temps de réalisation sur ce sous-total est donc à considérer comme un plancher.

| | Cplx |
|---|---:|
| Packaging, intégration initiale | 8 |
| Déploiement / configuration / supervision | 3 |
| Recette complète (X509, MFA, subrogation, mot de passe, SSO, SLO, 8 clients) | 13 |
| **Grand total (A.1 + A.2 + ci-dessus)** | **107** |

> ⚠️ Ce total achète **~12 mois**. Apereo maintient chaque version 12 mois puis l'EOL, **sans aucune LTS** — le cycle est à repayer, sur du code qui étend 28 classes internes de CAS.

### Scénario B — Remplacer CAS par Keycloak

| Cas d'usage | Standard Keycloak | Point de départ | Cplx |
|---|:--:|---|---:|
| Authentification login / mot de passe | ✅ | base users à trancher : Keycloak ou Mongo IAM (User Storage SPI) — **décision structurante** | 13 |
| Résolution HRD e-mail → organisation | ❌ | *Organizations* **écarté par test** (cf. #5) ⇒ Authenticator SPI obligatoire | 13 |
| Sélection d'organisation (N comptes / e-mail) | ❌ | Authenticator SPI + thème custom | 13 |
| Multi-domaine à suffixe partagé | ❌ | **testé : non couvert** — contrainte à contourner intégralement en SPI | 13 |
| Délégation OIDC | ✅ | IdP dynamiques : provisionner Keycloak depuis IAM (Admin REST) | 5 |
| Délégation SAML2 | ✅ | idem, mécanisme mutualisé avec l'OIDC | 3 |
| X509 | ✅ | authenticator x509 à configurer + extraction header nginx à revalider | 5 |
| MFA SMS | ❌ | extension SPI ou bascule vers TOTP/passkeys | 8 |
| Subrogation compte générique | ⚠️ | impersonation admin + token exchange à évaluer | 8 |
| Subrogation avec validation | ❌ | Authenticator SPI + workflow IAM conservé | 13 |
| Gestion du mot de passe (reset, forcé, politique, historique) | ✅ | à recâbler sur la base d'identités retenue | 5 |
| Émission `TOK-<UUID>` | ❌ | couche d'émission maison **ou** migration JWT des resource servers | 13 |
| SSO + Single Logout | ✅ | | 2 |
| Thème & i18n de la mire | ⚙️ | 19 vues à retranscrire en thème FreeMarker (ou SPA) | 8 |
| Audit / logbook VitamUI | ⚙️ | Event Listener SPI → IAM | 3 |
| Initialisation du super-admin | 🔧 | realm + compte + script de promotion en base VitamUI | 3 |
| **Sous-total reprise des cas d'usage** | | | **128** |
| Packaging, intégration initiale | | | 8 |
| Déploiement / configuration / supervision | | | 8 |
| Tests d'intégration généraux | | | 8 |
| Migration des données (users, IdP, secrets) | | | 13 |
| **Grand total** | | | **165** |

### Scénario C — Spring Authorization Server (travaux de faisabilité sur `api/auth-server`)

État d'avancement à jour du 24/07/2026. Le module `api/auth-server` est **déjà sur la plateforme cible** (Spring Boot 4, Spring Security 7, Java 21) : aucun saut de plateforme à financer. Les 7 cas d'usage acquis l'ont été **en 2 jours** : cette mesure établit la **faisabilité** des sept fonctions sur cette API, pas une vitesse d'exécution généralisable.

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
| MFA SMS | ❌ non traité | 3 |
| Throttling / anti-brute force | ❌ non traité (natif CAS) | 2 |
| Thème & i18n fr/en/de de la mire | ❌ messages existants à porter | 3 |
| Dette de sécurité des travaux de faisabilité | ❌ 7 points (secrets chiffrés, découpage `/cas/idp/{id}`, refresh `idpMetadata`, audit CSRF `SameSite=none`, logs) | 5 |
| **Sous-total reprise des cas d'usage** | | **46** |
| Packaging, intégration (Ansible, Consul) | | 5 |
| Déploiement / configuration / supervision | | 3 |
| Tests d'intégration généraux — **calendaire, non compressible** | | 13 |
| **Grand total** | | **67** |

Avantages structurels : écosystème Spring déjà maîtrisé, spécificités VitamUI dans notre base de code, pas de nouvelle adhérence COTS. Contrepartie : tout ce que CAS offrait nativement (X509, MFA SMS, password management, throttling) est à réécrire.

### Synthèse des trois scénarios

| | A — CAS 8 | B — Keycloak | C — SAS |
|---|---:|---:|---:|
| Reprise des cas d'usage | 83 | 128 | **46** |
| Intégration, déploiement, recette | 24 | 37 | 21 |
| **Total** | **107** | **≥ 165** | **67** |
| Nature dominante du travail | **internes COTS** | internes COTS (SPI) | **code neuf, API publique** |
| Levier de l'assistance IA | **faible** — 25 j pour la dernière montée, *avec* exemple | faible | **fort** — API publique et corpus massif |
| Exemple / précédent disponible | **non** (contrairement à CAS 6 → 7) | non | les travaux de faisabilité déjà menés |
| Acquis à date | code existant à adapter | 0 | **7 cas d'usage validés** |
| Ce que le total achète | **~12 mois** (pas de LTS) | une cible durable, mais base users déplacée | une cible durable, code maîtrisé |
| Couplage métier résiduel | **inchangé** (webflow CAS) | SPI Keycloak | à traiter dans notre code |

Quatre lectures de ce tableau :

1. **L'écart entre A et C est plus grand que le rapport des points** (107 / 67 ≈ 1,6), sans être d'un autre ordre de grandeur. Le travail de A relève de l'adaptation aux internes d'un COTS, où le levier de l'assistance IA est faible ; celui de C relève majoritairement du code neuf contre une API publique, où il est fort. Mais l'avantage ne joue que sur une partie de C : **moins d'un tiers de ses 67 points restants relève du code neuf**, le reste étant du portage et de l'intégration non compressible. **L'avantage décisif de C n'est donc pas sa vitesse d'exécution, c'est qu'il n'y a rien à repayer au cycle suivant.**
2. **C est le moins complexe** parce que 7 cas d'usage sont déjà validés — dont la fédération OIDC et SAML, les plus redoutés — et parce que le reste est majoritairement du portage de code existant. C'est un acquis mesuré, pas une hypothèse.
3. **A est le seul dont le total soit à repayer** au cycle suivant (12 mois, pas de LTS), et le seul qui ne réduise pas le couplage métier : à l'arrivée, les cinq irréductibles sont toujours dans le webflow CAS.
4. **B est un plancher, pas une estimation.** Le scénario Keycloak subit la même pénalité de faible levier IA que A, sans bénéficier d'aucun code existant. Les 165 points sont à considérer comme une borne inférieure.

**Ce qui ne se compresse pas, quel que soit le scénario** — et qui domine donc le calendrier de C : la recette avec de vrais IdP, le packaging Ansible, la mire Angular, la revue de sécurité, et le débogage des bugs qui n'apparaissent qu'au premier login réel (le retex du 24/07 en recense **13** sur les seuls chantiers OIDC et SAML). Le sous-total « cas d'usage » de C se compte en jours ; l'intégration et la recette se comptent en semaines.

> Le chantier de **découplage (phase 0)** est volontairement exclu des trois colonnes : il est commun aux trois scénarios, donc neutre dans la comparaison. À compter une seule fois, en plus.

> ⚠️ **Limite de lecture à garder présente** — la colonne C bénéficie d'un avantage de **niveau de preuve**, pas nécessairement d'un avantage de fond : c'est le seul scénario dont la faisabilité a été explorée par des développements réels. Ses inconnues sont donc levées, tandis que celles de A et B restent devant. **Un scénario mesuré paraît toujours plus sûr qu'un scénario estimé, y compris quand il ne l'est pas.** Pour un arbitrage à armes égales, il faudrait mener sur A et B un effort de vérification comparable — une validation Keycloak sur *Organizations*, un essai de montée de version CAS 8 sur une branche jetable — avant de conclure.

### 6.1 Réconciliation avec `matrice_CAS_vs_Keycloak.xlsx`

Une matrice de coûts circule en parallèle de ce document (`docs/2026-07-27/matrice_CAS_vs_Keycloak.xlsx`, échelle **1 à 5**). Correspondance avec les scénarios du présent document :

| Onglet de la matrice | Scénario | Total | Statut |
|---|:--:|---:|---|
| « Solution : remplacer CAS par Keycloak » | **B** | 43 | périmètre incomplet (voir A ci-dessous) |
| « Solution : montée sur la dernière version de CAS » | **A** | 31 | **chiffre un scénario qui n'existe plus** (voir B.1 ci-dessous) |
| — | **C** | — | **absent de la matrice** |

Cette absence n'est pas un oubli : la matrice est **l'artefact de la question binaire d'origine** — garder CAS ou le remplacer par Keycloak (§2.1). Elle a été construite avant que l'analyse du couplage ne fasse émerger le troisième terme. En l'état, elle ne permet donc pas l'arbitrage à trois branches que ce document instruit.

Sa liste de cas d'usage est par ailleurs plus courte que celle de la matrice §3 — elle porte d'ailleurs deux lignes explicitement ouvertes, « autres oubliés ???? » et « autres refactoration… ».

Voici ce qu'il manque pour la compléter.

**A. Cas d'usage absents de la matrice de coûts**

| Cas d'usage manquant | Réf. | Pourquoi il compte |
|---|:--:|---|
| **Émission du token applicatif `TOK-<UUID>`** | #21 | **L'omission la plus lourde.** Contrat propriétaire lu par ~10 resource servers via `X-Auth-Token`. Aucun IdP ne l'émet nativement : soit couche d'émission maison, soit migration JWT de tous les resource servers. C'est le poste le plus structurant de l'arbitrage. |
| **Serveur OIDC pour les 8 SPA Angular** | #22 | La matrice ne cote que la délégation **entrante** (identity brokering). Or CAS est aussi le **fournisseur OIDC sortant** des 8 frontends (`angular-oauth2-oidc`). Périmètre distinct, non chiffré. |
| **Base d'identités / stockage utilisateurs** | #2 | Décision structurante : Keycloak devient-il la source de vérité, ou branche-t-on un `User Storage SPI` sur Mongo IAM ? Emporte la migration de données et le problème du double référentiel. |
| **Thème & i18n de la mire** | #27 | 19 vues Thymeleaf (1 067 lignes) + messages fr/en/de à retranscrire en thème FreeMarker. Poste de travail réel, invisible dans la matrice. |
| **Provisioning JIT après fédération** | #18 | `autoProvisioningEnabled`, `defaultGroupId`, `POST /cas/users/jit`. Fonctionnalité livrée, à reprendre. |
| **Provisioning depuis un annuaire externe** | #19 | `CasService.provisionUser` + `ProvisioningClient` par IdP. |
| **Registre des clients / services persistant** | #23 | Collection Mongo `services`. Sans équivalent configuré, ajouter un client en production impose un redéploiement. |
| **Résolution HRD e-mail → organisation** | #3 | La matrice la fond dans « gestion multi-domaine ». Ce sont deux mécanismes distincts, et le test du 27/07 montre que le premier n'est pas couvrable en configuration. |
| **Throttling / anti-brute force** | #25 | `cas.authn.throttle.*` + compteur `nbFailedAttempts` côté IAM. |
| **Blocage / désactivation de compte** | #26 | `UserStatusEnum`, contrôle dans `DispatcherAction`, vue dédiée. |
| **Audit métier / logbook** | #28 | Événements `EXT_VITAMUI_*` — exigence de traçabilité, pas un confort. |
| **Initialisation du super-admin** | — | Amorçage : realm + compte + script de promotion en base VitamUI. Sans lui, plateforme non initialisable. |
| **Migration des données** | — | Utilisateurs, IdP, secrets. Absent des lignes transverses de l'onglet Keycloak (qui ne cote que packaging, déploiement, tests). |
| **Sécurisation du canal IdP ↔ IAM** | — | mTLS ou HMAC signé sur les 13 endpoints `/iam/v1/cas/*`. |

**B. Deux réserves de méthode sur la matrice**

1. **L'onglet CAS ne chiffre pas le bon scénario.** Ses constats parlent de « dernière version **mineure** », de `cas.version` déjà en 7.0.10.1 et de « pas d'API breaking connue », d'où des complexités à **1** sur OIDC, SAML, X509, MFA et SLO. Or **CAS 7.0.x, 7.1.x et 7.2.x sont EOL**, et 7.3.x s'arrête au 31/12/2026 : la seule cible réelle est **CAS 8.0**, qui impose JDK 25, Spring Boot 4 / Spring Security 7, Jackson 3 et pac4j 7 (§6, scénario A). **Le total de 31 chiffre une montée de version qui n'existe plus.** À reprendre avant tout usage comparatif.
2. **Les deux échelles ne sont pas commensurables.** La matrice cote de 1 à 5 (linéaire), ce document en Fibonacci (1-13). Un total de 43 et un total de 165 ne se comparent pas. Surtout, aucune des deux ne pondère la différence de productivité entre adaptation aux internes d'un COTS et code neuf sur API publique (§5) — différence qui pénalise mécaniquement les scénarios A et B.

**C. Ce que change l'ajout des lignes manquantes**

Les quatorze postes ci-dessus sont, dans leur grande majorité, **plus coûteux en Keycloak qu'en CAS** — token opaque, serveur OIDC sortant, thème, base d'identités et migration de données n'ont pas d'équivalent natif. Les compléter **creusera l'écart au détriment du scénario Keycloak**, dont le total de 43 est aujourd'hui optimiste pour cause de périmètre incomplet.

---

## 7. Recommandation de lecture

1. **Aucun scénario n'évite le développement spécifique.** Les cinq fonctionnalités du §4.1 (sélection d'organisation, multi-domaine partagé, subrogation validée, token opaque, habilitations) sont hors du champ de tout COTS d'authentification. Le vrai choix porte sur *où* placer ce spécifique : dans un overlay CAS, dans des SPI Keycloak, ou dans notre propre serveur Spring.
2. **Le préalable commun est le découplage**, pas le changement d'outil : sortir organisation et subrogation du webflow d'authentification pour en faire des processus métier IAM. Ce travail réduit le coût des trois scénarios et, une fois fait, rend la cible techniquement interchangeable.
3. **Keycloak apporte surtout de la maintenance en moins et du MFA moderne**, pas de la couverture fonctionnelle métier. La question ouverte sur *Organizations* est désormais **tranchée par le test du 27/07/2026** : le multi-domaine à suffixe partagé n'est pas couvert, donc #3, #4 et #5 relèvent tous d'un développement d'`Authenticator SPI`. L'hypothèse d'un Keycloak « en configuration » est écartée.
4. **Le contrat `TOK-<UUID>` est le point de décision le plus structurant** : le conserver via un adaptateur maison (option explorée lors des travaux de faisabilité) ou migrer les resource servers vers du JWT. Cette décision est indépendante du choix d'IdP et devrait être prise en premier.

---

## Annexe A — Endpoints IAM dédiés à CAS (`/iam/v1/cas/*`)

| Endpoint | Rôle |
|---|---|
| `POST /cas/login` | Vérification du mot de passe (CAS ne détient aucun secret) |
| `POST /cas/changePassword` | Changement de mot de passe + historique |
| `GET /cas/users?email=` | Recherche multi-organisation |
| `GET /cas/users/provisioning` | Lookup + provisioning externe optionnel |
| `POST /cas/users/jit` | Provisioning JIT après fédération |
| `GET /cas/subrogations` | Subrogations autorisées pour un super-utilisateur |
| `POST /cas/subrogations/validate` | Validation de subrogation |
| `GET /cas/logout` | Invalidation du `TOK-<UUID>` |
| `GET /cas/customers` | Résolution des organisations (écran de sélection) |
| `POST /cas/tokens` | Émission du token applicatif opaque |
| `GET /cas/hrd?email=` | Résolution *Home Realm Discovery* |
| `GET /cas/idp/{id}` | Configuration d'un IdP *(renvoie les secrets — à corriger)* |

## Annexe B — Modules CAS activés (extrait du `pom.xml`)

`webapp-tomcat`, `core`, `mongo-service-registry`, `hazelcast-ticket-registry`, `pac4j-{api,core,core-clients,webflow}`, `saml-core{,-api}`, `oauth{,-api,-core,-services,-webflow}`, `oidc{,-core,-core-api}`, `x509-{core,webflow}`, `surrogate-{api,authentication,core,webflow}`, `pm-{core,webflow}`, `simple-mfa{,-core}`, `sms-smsmode`, `throttle`, `metrics`, `bucket4j-core`, `passwordless-api`, `token-core-api`.

## Annexe C — Classes spécifiques par domaine fonctionnel

| Domaine | Classes |
|---|---|
| Webflow de login | `VitamLoginWebflowConfigurer`, `DispatcherAction`, `ListCustomersAction`, `CustomerSelectedAction`, `TriggerChangePasswordAction`, `WebflowConfig` |
| Authentification | `LoginPwdAuthenticationHandler`, `UserPrincipalResolver` (515 l.) |
| Délégation OIDC/SAML | `ProvidersService`, `CustomDelegatedIdentityProviders`, `Pac4jClientIdentityProviderDto`, `CustomDelegatedClientAuthenticationAction`, `CustomOidcCasClientRedirectActionBuilder` |
| X509 | `CustomRequestHeaderX509CertificateExtractor`, `CertificateParser`, `X509AttributeMapping`, `X509CertificateAttributes`, `X509CasDelegatingWebflowEventResolver` |
| MFA | `VitamMfaWebflowConfigurer`, `CustomSendTokenAction`, `CheckMfaTokenAction` |
| Subrogation | `IamSurrogateAuthenticationService`, `CustomSurrogateInitialAuthenticationAction`, `InitializeSubrogationAction`, `SurrogateUsernamePasswordCredential` |
| Mot de passe | `IamPasswordManagementService`, `ResetPasswordController`, `I18NSendPasswordResetInstructionsAction`, `InitPasswordConstraintsConfiguration`, `PmTransientSessionTicketExpirationPolicyBuilder`, `PmMessageToSend` |
| Tickets & tokens | `DynamicTicketGrantingTicketFactory`, `CustomOAuth20DefaultAccessTokenFactory` |
| Logout | `TerminateApiSessionAction`, `CustomDelegatedAuthenticationClientLogoutAction` |
| Web / sécurité | `CustomCorsProcessor`, `CustomOidcRevocationEndpointController`, `CustomCasWebSecurityConfigurerAdapter` |
| Configuration | `AppConfig` (619 l.), `CasBeans`, `WebConfig`, `InitContextConfiguration`, `IamClientConfigurationProperties` |
