# Récapitulatif des complexités — les trois trajectoires

> **Document historique — 28 juillet 2026.**
> Repris et actualisé par `api/auth-gateway/docs/2026-07-31/dossier-arbitrage-socle-authentification.md`, **qui fait foi**.
> Conservé pour la traçabilité.
>
> **Ce qui n'est plus exact dans ce document :**
> - **les grilles de ce document sont reprises en annexe A** du document de référence, à titre **historique et non normatif** ;
> - **les totaux 107 / 165 / 67** — devenus 104 / 157 / 64 après retrait du MFA par SMS le 29/07 ;
> - **la grille A chiffre une montée vers CAS 8.0** — la cible retenue est la ligne 7.3.x ;
> - **le total de C n'est plus maintenu** : X.509 abandonné (−5), anti-force brute déjà dans IAM (−2), mot de passe à revoir à la baisse, et un poste manquant (rechargement à chaud des IdP).

---


*28 juillet 2026 — document technique, annexe du compte rendu COPIL*

> **Objet** — Isoler dans un document unique les grilles de complexité des trois trajectoires étudiées pour le remplacement du socle d'authentification, afin qu'elles puissent être discutées et re-cotées par l'équipe indépendamment du document de décision.
>
> **Les trois trajectoires** sont évaluées sur la **même liste de cas d'usage** et la **même échelle**, pour être comparables entre elles.
>
> | | Trajectoire | Cible |
> |---|---|---|
> | **A** | Monter de version CAS | CAS 7.0.10.1 → 8.0 |
> | **B** | Remplacer CAS par Keycloak | Keycloak 26.x |
> | **C** | Développer sur Spring Authorization Server | module `api/auth-server` |
>
> *Ordre de présentation : le maintien du composant actuel d'abord, puis les deux remplacements.*

---

## ⚠️ Avertissement de lecture

**Ces points ne sont pas convertibles en jours-homme.** Ils mesurent l'incertitude et l'effort relatif, pas une durée.

Les valeurs posées ici sont une **proposition de départ** issue de l'analyse de code du 27/07. Elles doivent être re-posées par l'équipe en atelier (planning poker), où **le débat sur les écarts vaut plus que les chiffres eux-mêmes**.

Ce document ne circule pas au COPIL : les totaux bruts y seraient convertis en jours ou en euros malgré cet avertissement. Le document de décision retient un effort relatif normalisé (1× / 1,6× / 2,5×).

---

## 1. Échelle de complexité

| Points | Signification |
|---:|---|
| **1** | trivial — configuration, aucune inconnue |
| **2** | simple — cadré, un seul composant |
| **3** | standard — bien compris, plusieurs fichiers |
| **5** | significatif — plusieurs composants, quelques inconnues |
| **8** | complexe — inconnues techniques réelles, exploration nécessaire |
| **13** | très complexe — risque élevé, à découper avant de s'engager |
| **0** | acquis — déjà réalisé et validé |

---

## 2. Trajectoire A — Monter de version CAS 7.0.10.1 → 8.0

> CAS 7.0.x, 7.1.x et 7.2.x sont **EOL** (aucun patch, y compris de sécurité). 7.3.x est en patches de sécurité seuls jusqu'au 31/12/2026. **8.0 est la seule version CAS ayant un avenir** — le saut ne peut pas être évité par une montée intermédiaire.

### A.1 — Saut de plateforme imposé par CAS 8

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
> **Ce que cette correction ne change pas** : le risque de rupture silencieuse sur les 28 points d'extension accrochés à des noms de beans internes (voir §5 de la synthèse du 27/07). Le BOM aligne des versions ; il ne dit pas si `defaultAccessTokenFactory` existe encore. C'est la raison pour laquelle A.2 reste inchangé à 58.

### A.2 — Reprise du spécifique

*Le code existe et est testé, mais les ruptures se découvrent à l'exécution.*

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

### A.3 — Transverse

| Poste | Cplx |
|---|---:|
| Packaging, intégration initiale | 8 |
| Déploiement / configuration / supervision | 3 |
| Recette complète (X509, MFA, subrogation, mot de passe, SSO, SLO, 8 clients) | 13 |
| **Grand total (A.1 + A.2 + A.3)** | **107** |

> ⚠️ **Ce total achète ~12 mois.** Apereo maintient chaque version 12 mois puis l'EOL, **sans aucune LTS** — le cycle est à repayer, sur du code qui étend 28 classes internes de CAS.

> ⚠️ **Ce total est le moins fiable des trois.** Les 28 points d'extension du spécifique s'accrochent à des **noms de beans internes non documentés**, avec un mode de défaillance **silencieux** : un bean renommé en CAS 8 n'écrase plus rien, ne produit aucune erreur de compilation ni de démarrage, et le défaut se découvre en recette. Les postes à 13 points ci-dessus ne signifient pas « on sait que c'est difficile », mais **« on ne sait pas ce qu'on ne sait pas »**.

---

## 3. Trajectoire B — Remplacer CAS par Keycloak

| Cas d'usage | Standard Keycloak | Point de départ | Cplx |
|---|:--:|---|---:|
| Authentification login / mot de passe | ✅ | base users à trancher : Keycloak ou Mongo IAM (User Storage SPI) — **décision structurante** | 13 |
| Résolution HRD e-mail → organisation | ❌ | *Organizations* **écarté par test** ⇒ Authenticator SPI obligatoire | 13 |
| Sélection d'organisation (N comptes / e-mail) | ❌ | Authenticator SPI + thème custom | 13 |
| Multi-domaine à suffixe partagé | ❌ | **testé : non couvert** — contrainte à contourner intégralement en SPI | 13 |
| Délégation OIDC | ✅ | IdP dynamiques : provisionner Keycloak depuis IAM (Admin REST) | 5 |
| Délégation SAML2 | ✅ | idem, mécanisme mutualisé avec l'OIDC | 3 |
| X509 | ✅ | authenticator x509 à configurer + extraction header nginx à revalider | 5 |
| MFA SMS | ❌ | extension SPI ou bascule vers TOTP / passkeys | 8 |
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

> **B est un plancher, pas une estimation.** Cette trajectoire subit la même pénalité de faible levier IA que A, sans bénéficier d'aucun code existant. Les 165 points sont à considérer comme une borne inférieure.

---

## 4. Trajectoire C — Spring Authorization Server

*État d'avancement à jour du 24/07/2026.* Le module `api/auth-server` est **déjà sur la plateforme cible** (Spring Boot 4, Spring Security 7, Java 21) : aucun saut de plateforme à financer. Les 7 cas d'usage acquis l'ont été **en 2 jours** : cette mesure établit la **faisabilité** des sept fonctions sur cette API, pas une vitesse d'exécution généralisable.

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

> **Contrepartie assumée** : tout ce que CAS offrait nativement (X509, MFA SMS, password management, throttling) est à réécrire. Le sous-total « cas d'usage » se compte en jours ; l'intégration et la recette se comptent en semaines.

---

## 5. Synthèse comparée

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
| Effort relatif normalisé | ≈ 1,6× | ≈ 2,5× | **1×** |

---

## 6. Comment lire ces totaux

**1. L'écart entre A et C est plus grand que le rapport des points** (107 / 67 ≈ 1,6), sans être d'un autre ordre de grandeur. Le travail de A relève de l'adaptation aux internes d'un COTS, où le levier de l'assistance IA est faible ; celui de C relève majoritairement du code neuf contre une API publique, où il est fort. Mais l'avantage ne joue que sur une partie de C : **moins d'un tiers de ses 67 points restants relève du code neuf**, le reste étant du portage et de l'intégration non compressible. **L'avantage décisif de C n'est donc pas sa vitesse d'exécution, c'est qu'il n'y a rien à repayer au cycle suivant.**

**2. C est le moins complexe parce que 7 cas d'usage sont déjà validés** — dont la fédération OIDC et SAML, les plus redoutés — et parce que le reste est majoritairement du portage de code existant. C'est un acquis mesuré, pas une hypothèse.

**3. A est le seul dont le total soit à repayer** au cycle suivant (12 mois, pas de LTS), et le seul qui ne réduise pas le couplage métier : à l'arrivée, les cinq irréductibles sont toujours dans le webflow CAS.

**4. B est un plancher.** Même pénalité de levier IA que A, sans code existant à réutiliser.

**5. Le niveau de preuve est inégal, et il faut le savoir en lisant les totaux.**

| Trajectoire | Niveau de preuve |
|---|---|
| **A — CAS 8** | documentation éditeur et analyse du code existant ; **aucun essai de montée de version mené** |
| **B — Keycloak** | documentation, **+ un point testé** (multi-domaine à suffixe partagé), résultat **négatif** |
| **C — SAS** | travaux de faisabilité réels sur 7 cas d'usage, validés end-to-end |

C'est le seul biais qui joue en faveur de C : ses inconnues sont levées parce qu'elle a été explorée, celles de A et B restent devant. **Un scénario mesuré paraît toujours plus sûr qu'un scénario estimé, y compris quand il ne l'est pas.**

Cet écart ne justifie pas pour autant d'investir dans une mesure complémentaire sur A : quel qu'en soit le résultat, cette trajectoire achète douze mois. Un essai préciserait un coût, il ne lèverait pas ce qui la disqualifie.

**6. Ce qui ne se compresse pas, quelle que soit la trajectoire** — et qui domine donc le calendrier de C : la recette avec de vrais IdP, le packaging Ansible, la mire Angular, la revue de sécurité, et le débogage des bugs qui n'apparaissent qu'au premier login réel (le retex du 24/07 en recense **13** sur les seuls chantiers OIDC et SAML).

---

## 7. Ce qui est exclu des trois colonnes

Le chantier de **découplage** — sortir la sélection d'organisation et la subrogation du flux d'authentification pour en faire des processus métier IAM — est volontairement **exclu des trois grilles** : il est commun aux trois trajectoires, donc neutre dans la comparaison. **À compter une seule fois, en plus.**

Attention toutefois : ce chantier n'est pas neutre *fonctionnellement*. Il suppose de reconnaître qu'une partie du parcours de connexion actuel n'a pas à vivre dans l'authentification, ce qui implique un changement visible par l'utilisateur. Il relève donc d'un arbitrage de périmètre, traité dans le compte rendu COPIL du 28/07.

---

## Documents liés

- **Compte rendu COPIL** — document de décision : `2026-07-28/compte-rendu.md`
- **Analyse détaillée** — cartographie fonctionnalité par fonctionnalité, réconciliation avec `matrice_CAS_vs_Keycloak.xlsx` : `2026-07-27/synthese-fonctionnalites-cas-keycloak-vitamui.md`
- **Retex travaux de faisabilité** — fédération OIDC + SAML : `2026-07-24/retex-federation-oidc-saml.md`
