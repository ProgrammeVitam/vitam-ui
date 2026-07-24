# Matrice de décision — socle d'authentification VitamUI

> **Document historique — 30 juillet 2026.**
> Repris et actualisé par `api/auth-gateway/docs/2026-07-31/dossier-arbitrage-socle-authentification.md`, **qui fait foi**.
> Conservé pour la traçabilité.
>
> **Ce qui n'est plus exact dans ce document :**
> - **les charges sont exprimées en jours-homme sur la base d'origine** (A 50-90, B 70-120, C 50-100) — le document de référence les exprime en **points de charge** après recalage de la ligne de recette et rebasage : **A 42-77, B 56-98, C 42-84** ;
> - **la recette forfaitaire de 15 jours, identique dans les trois colonnes** — recalée sur le précédent mesuré (25 à 35 jours) ;
> - **l'estimation indépendante de C (60-120 j)** — elle est **intégrée** au document de référence, dont la méthode uniforme redonne exactement sa valeur.
>
> - **La matrice fonctionnelle macro, la ventilation par fonction, la matrice des risques et les notes de partage IAM / CAS restent valides** et sont reprises aux §6, §7 et §8 du document de référence.

---


*30 juillet 2026 — document d'aide à la décision — COPIL, direction technique*

---

> ## Objet
>
> Ce document est une **matrice de décision**. Il ne réexpose ni l'analyse technique ni les
> grilles de complexité : celles-ci sont dans le rapport technique du 29/07, auquel il renvoie.
>
> Il tient sur quatre tableaux :
> - **quelles fonctions macro** chaque trajectoire conserve, dégrade ou abandonne (§2) ;
> - **quelle charge** chaque trajectoire représente (§3) ;
> - **où va cette charge**, fonction par fonction et en jours-homme (§3.3) ;
> - **quel risque** chaque trajectoire porte (§4).
>
> S'y ajoute une **seconde estimation de la trajectoire C**, construite indépendamment de celle de
> l'équipe (§3.4). Les deux figurent au dossier : **60 – 120 j** contre **50 – 100 j**, l'écart
> portant entièrement sur la ligne de recette.
>
> Puis il énonce la **décision proposée** (§6) et **ce qui reste à trancher** (§7).
>
> ### Statut des chiffres
>
> Les fourchettes du §3 sont des **estimations de charge en jours-homme**, posées par l'équipe le
> 30/07. Elles ne sont pas la conversion des points de complexité du rapport du 29/07 — les points
> ne sont pas convertibles. Ce sont **deux mesures indépendantes**, et le §3.2 signale explicitement
> là où elles divergent.
>
> ### Ce qui a changé depuis le 29/07
>
> Quatre inflexions, portées par l'équipe :
> 1. la **cible du scénario A** n'est plus CAS 8.0 mais une montée **dans la ligne 7.x**, à titre
>    conservatoire (§6.2) ;
> 2. le **scénario B est écarté** (§6.1) ;
> 3. le **périmètre du scénario C est réduit** : X.509 et MFA sur base interne sont abandonnés (§5) ;
> 4. **A et C ne sont plus exclusives** — elles sont retenues conjointement, avec des rôles
>    distincts (§6.4).
>
> S'y ajoutent des **corrections de fait**, établies par lecture du code le 30/07 et par
> confrontation avec l'analyse indépendante *« Analyse de la cinématique de communication
> IAM ↔ CAS »* :
> - la protection anti-force brute, la journalisation des connexions et l'essentiel de la politique
>   de mot de passe sont **dans IAM et non dans CAS** — donc acquis en trajectoire C (§2.2) ;
> - **deux fonctions manquaient à la matrice** : la journalisation des connexions et
>   l'administration à chaud des fournisseurs d'identité. Elles y sont désormais (lignes 13 et 14).
>
> Voir « Divergences » en fin de document pour l'effet sur les grilles du 29/07.

---

## 1. Les trois trajectoires

| | **A — Montée de version CAS** | **B — Transposition Keycloak** | **C — Spring Authorization Server** |
|---|---|---|---|
| Nature | rester sur le produit, changer de version | changer de produit sur étagère | internaliser le composant |
| Ce qu'on garde | tout — iso-fonctionnel par construction | le produit, pas le parcours | le modèle métier VitamUI |
| Ce qu'on écrit | adaptation des 28 points d'extension | SPI Keycloak pour les spécificités | le serveur d'autorisation |
| Ce que ça achète | **du support, pour une durée bornée** | une cible durable, sur un produit tiers | une cible durable, sur du code maîtrisé |
| **Migration de données d'identités** | **aucune** | **oui — question non tranchée** | **aucune** |
| Risque de régression sur l'existant | **oui** — le composant de production est modifié | oui — il est remplacé | **non** — module distinct, bascule réversible |
| Éditeur | Apereo | Red Hat / CNCF | nous |
| Statut | **mesure conservatoire** (§6.2) | **écarté** (§6.1) | **trajectoire retenue** (§6.3) |

---

## 2. Matrice fonctionnelle macro

Quinze fonctions macro — le niveau du COPIL, pas celui de l'implémentation. Le détail à
30 lignes est au §1.1 et §2.2 du rapport technique du 29/07.

**Légende** — ✅ acquis, sans travail · 🔧 à porter ou reconfigurer, sans risque fonctionnel ·
🔨 à développer · ⚠️ couvert mais **dégradé ou de sémantique différente** · ❌ **non couvert —
fonction abandonnée**

| # | Fonction macro | Aujourd'hui | A — CAS 7.x | B — Keycloak 26 | C — SAS |
|---|---|:--:|:--:|:--:|:--:|
| 1 | Authentification identifiant / mot de passe sur la base VitamUI | ✅ | 🔧 | 🔨 | ✅ |
| 2 | Parcours d'entrée multi-organisation (e-mail → organisation) ⁵ | ✅ | 🔧 | ⚠️ 🔨 | ✅ |
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

**¹ Mot de passe en trajectoire C — une partie est acquise, mais pas la totalité.** Le partage est
plus fin que ne le laissait entendre une première lecture :

| Composant | Où il vit | Acquis en C ? |
|---|---|---|
| Historique des anciens mots de passe | **IAM** — `maxOldPassword` et `saveCurrentPasswordInOldPasswords` dans `UserService`, comparaison dans `CasService` | **oui** |
| Délai de révocation | **IAM** — `passwordRevocationDelay` porté par `Customer` | **oui** |
| Changement effectif | **IAM** — `POST /cas/password/change` | **oui** |
| Validateur de complexité | **bibliothèque partagée** `commons-security` — `PasswordValidator`, déclaré en bean **des deux côtés** et injecté dans `CasService` | **oui** — déjà câblé en IAM |
| **Décision d'expiration** | **CAS** — `mustChangePassword()` dans `LoginPwdAuthenticationHandler` ; la donnée `passwordExpirationDate` est en IAM, la règle est en CAS | **non** — à réimplémenter, mais c'est une règle courte |
| **Parcours de réinitialisation** | **CAS** — jeton transitoire, e-mail localisé, écrans (≈ 840 lignes) | **non** — c'est le vrai reste à faire |

Ce qui reste à écrire en C est donc le **parcours**, pas le moteur de politique. La cotation à
5 points du 29/07 est à revoir à la baisse, sans être annulée.

**² Anti-force brute en trajectoire C — déjà acquis pour l'essentiel.** Le compteur de tentatives,
le seuil, le passage du compte en `BLOCKED`, la fenêtre de réarmement et l'événement d'audit sont
**implémentés dans IAM**, dans `CasController.login()` (`maximumFailuresForLoginAttempts`,
`getTimeIntervalForLoginAttempts()`, `updateNbFailedAttempsPlusLastConnectionAndStatus`). Ils
s'exécutent à **chaque appel de `POST /cas/login`** — c'est-à-dire y compris quand l'appelant est
le serveur d'autorisation. **La trajectoire C en hérite sans écrire une ligne.** Reste la seconde
couche, la limitation de débit par requête, aujourd'hui native CAS (`cas.authn.throttle.failure.*`,
seuil 2 sur 3 s) : elle relève de l'infrastructure et dispose d'équivalents standard dans
l'écosystème Spring déjà en place.

**³ Journalisation des connexions — acquise, avec une lacune préexistante.** L'événement de
connexion est produit par **IAM**, dans `IamLogbookService.loginEvent`, appelé depuis
`CasController.login()` en succès comme en échec. C en hérite. **Mais `loginEvent` n'est appelé
que là** : les connexions par fédération OIDC et SAML, qui passent par `getUser` et non par
`POST /cas/login`, **ne produisent aucun événement de connexion aujourd'hui**. C'est une lacune du
socle actuel, pas une perte de C — elle est signalée ici parce qu'une revue de sécurité la
relèvera, quelle que soit la trajectoire retenue.

**⁴ Administration à chaud des fournisseurs d'identité — à reproduire en C.** `ProvidersService`
recharge la liste des IdP depuis IAM **toutes les 60 secondes**
(`@Scheduled(initialDelay = 60_000, fixedRate = 60_000)`) et reconstruit les clients pac4j
correspondants. Conséquence opérationnelle : **on ajoute ou modifie un fournisseur d'identité
depuis l'application d'administration sans redémarrer le serveur d'authentification.** C'est une
capacité d'exploitation réelle, absente des grilles de complexité du 29/07, et **à porter
explicitement en trajectoire C**. Elle n'est ni difficile ni volumineuse, mais elle ne s'obtient
pas gratuitement — et son oubli se découvrirait en exploitation, pas en recette.

**⁵ Non-divulgation d'existence de compte — un comportement à préserver, pas une commodité.**
Un e-mail inconnu dont le **domaine** correspond à un IdP est tout de même routé, silencieusement,
vers la page de connexion de ce fournisseur : le parcours ne révèle jamais si le compte existe.
Ce comportement est porté par le routage du §2 et doit être **reconduit à l'identique dans toute
trajectoire**. Il est facile à casser par inadvertance en réécrivant le parcours d'entrée, et une
régression sur ce point est un défaut de sécurité, pas un défaut d'ergonomie. **À inscrire au plan
de recette des trois scénarios.**

### 2.1 Ce que ce tableau fait apparaître

**La colonne A ne comporte aucun ❌ et seulement deux ✅** — les habilitations et l'administration
fonctionnelle, qui vivent toutes deux hors de CAS.
Ce n'est pas un hasard, c'est sa définition : **une montée de version n'apporte aucune fonction et
n'en retire aucune.** Treize cases sur quinze sont du travail d'adaptation à coût non nul et à
bénéfice fonctionnel nul. Ce que A achète n'est pas dans ce tableau — c'est du support éditeur, et
il est borné dans le temps (§3.1).

**La colonne B porte quatre ⚠️, dont trois sur le parcours d'entrée.** Ce sont les fonctions où le
produit couvre le besoin *avec une sémantique différente de la nôtre* : les organisations de
Keycloak ne permettent pas à N organisations de partager un domaine (**testé le 27/07, résultat
négatif**), son *impersonation* est un acte administratif unilatéral là où notre subrogation
requiert le consentement de la cible, et sa mire donne une **ergonomie proche mais non identique**
à l'actuelle. Un ⚠️ coûte plus cher qu'un 🔨 : il faut d'abord défaire le comportement natif.

**La colonne C porte deux ❌, et ce sont les seuls du tableau.** X.509 et MFA sur base interne sont
**abandonnés**, non reportés. C'est le seul endroit du dossier où une trajectoire retire une
fonction au lieu d'en changer l'implémentation. Le §5 lui est consacré.

**La ligne 7 est stable partout sauf en B.** Les habilitations VitamUI vivent dans `api-iam` et
ne sont touchées par aucune trajectoire — sauf par B, qui pose la question non tranchée du double
référentiel d'identités (réserve 11 du rapport du 29/07).

### 2.1 bis — Ce que cette matrice ne peut pas montrer

> ⚠️ **Le poste le plus lourd de la trajectoire C n'apparaît dans aucune ligne ci-dessus.**
>
> La **sécurisation du canal entre le serveur d'autorisation et IAM** — le durcissement de la dette
> introduite par le prototype — est chiffrée **9 à 24 jours** au §3.3, soit **le premier poste de
> toute la matrice de charge**. Elle est absente de ce tableau parce qu'elle **ne correspond à
> aucune fonction visible par l'utilisateur** : rien n'est ajouté, rien n'est retiré, le service
> rendu est identique avant et après.
>
> Elle est pourtant **bloquante avant toute mise en service**. Lire le §2 sans le §3.3 conduirait à
> conclure que la trajectoire C ne coûte presque rien.
>
> Deux autres chantiers sont dans le même cas :
> - le **découplage** du parcours de connexion — commun aux trois trajectoires, exclu de toutes les
>   grilles, et le seul à impliquer un changement visible par l'utilisateur (§7, point 4) ;
> - la **recette end-to-end** — 15 jours dans chaque colonne, incompressibles.
>
> **Une matrice fonctionnelle mesure le service rendu, pas le travail à faire.** Les deux ne se
> recouvrent pas, et l'écart est ici la ligne la plus chère du dossier.

### 2.2 Le fait structurant : la majorité des fonctions est déjà portée par IAM

C'est le constat qui explique la forme de tout le tableau, et il mérite d'être énoncé seul.

**CAS ne détient pas le métier. Il l'orchestre.** Ce qui vit dans `cas/cas-server` est une
séquence d'états de webflow ; ce qui décide est dans `api-iam`. Vérifié fonction par fonction :

| Fonction macro | Où vit réellement la décision |
|---|---|
| Authentification mot de passe | **IAM** — `POST /cas/login`, vérification du secret. CAS ne détient aucun mot de passe |
| Base d'identités | **IAM** — Mongo `users`, `customers`, `groups`, `profiles`, `tenants`. CAS est sans état sur l'identité |
| Résolution organisation (HRD) | **IAM** — `patterns` d'e-mail portés par `IdentityProvider`, appariement par une bibliothèque partagée |
| Subrogation | **IAM** — subrogations et leur acceptation, `GET /cas/subrogations` |
| Jeton d'accès VitamUI | **IAM** — émission du `TOK-<UUID>` |
| Statut de compte, verrouillage | **IAM** — compteur, seuil, `BLOCKED`, audit, dans `CasController.login()` |
| Politique et historique de mot de passe | **IAM** — `maxOldPassword`, `passwordRevocationDelay` sur `Customer` ; validateur de complexité en bibliothèque partagée |
| Journalisation des connexions | **IAM** — `IamLogbookService.loginEvent` |
| Habilitations (tenants, profils, groupes) | **IAM** — jamais dans CAS |
| Référentiel des IdP, organisations, subrogations | **IAM** — administré par l'application Angular Identity, pas par CAS |

**Les deux exceptions, à ne pas gommer.** Tout n'est pas dans IAM. La **décision d'expiration du
mot de passe** (`mustChangePassword`) et surtout le **rechargement à chaud des fournisseurs
d'identité** (§2, note ⁴) s'exécutent dans CAS. La seconde est une capacité d'exploitation réelle
et doit être portée explicitement en trajectoire C.

**Ce que cela change pour la décision.** Les quinze fonctions du §2 ne sont pas quinze fonctions
« à reprendre » : pour la plupart, **le moteur est déjà écrit et ne bouge dans aucune trajectoire**.
Ce qui se déplace, c'est le **point d'orchestration**. C'est pourquoi les travaux de faisabilité ont
pu valider sept cas d'usage en réutilisant les points d'accès IAM existants — le métier était déjà
là, il manquait le contrat d'accès.

**La contrepartie, à ne pas taire.** Ce constat coupe dans les deux sens. Il réduit le coût de C,
mais il explique aussi pourquoi **aucun COTS ne se pliera au modèle** : ce n'est pas CAS qui est
mal utilisé, c'est le modèle VitamUI qui n'est pas celui d'un serveur d'authentification sur
étagère. C'est le même constat qui fonde l'abandon de B (§6.1).

### 2.3 Aucune migration de données en trajectoire C

Corollaire direct du point précédent, et il vaut d'être isolé parce qu'il est souvent le poste le
plus lourd et le plus risqué d'un changement de socle.

**En C, le référentiel d'identités ne bouge pas.** Il est aujourd'hui dans IAM, il y reste ; le
serveur d'autorisation l'interroge par les mêmes points d'accès que CAS aujourd'hui. **Zéro
migration, zéro reprise de comptes, zéro double référentiel, zéro fenêtre de bascule sur les
données.** C'est également vrai en A, où le référentiel n'est pas davantage concerné.

**B est la seule trajectoire à porter la question**, et elle n'est pas tranchée : Keycloak devient-il
la source de vérité des identités — ce qui impose une migration des comptes et des secrets — ou
conserve-t-on la base IAM derrière une SPI de stockage, ce qui crée un double référentiel à tenir ?
Cette décision n'est pas incluse dans les 70 – 120 j.

---

## 3. Charge estimée

| | **A — CAS 7.x** | **B — Keycloak 26** | **C — SAS** |
|---|---:|---:|---:|
| **Fourchette totale** | **50 – 90 j** | **70 – 120 j** | **50 – 100 j** |
| dont recette end-to-end / QA | 15 j | 15 j | 15 j |
| Hors recette | 35 – 75 j | 55 – 105 j | 35 – 85 j |
| Amplitude de la fourchette | × 1,8 | × 1,7 | × 2,0 |
| Risque de faisabilité | non — chemin déjà parcouru | **oui, identifié** | **levé** sur 7 cas d'usage |
| Iso-ergonomie de la mire | acquise | **proche, non identique** | maîtrisée (code interne) |
| Durée de vie de l'investissement | **bornée** — voir §3.1 | ligne 26.x, durable | durable |

### 3.1 La durée de vie n'est pas la même dans les trois colonnes

C'est le point que la fourchette seule ne montre pas. **Les 15 jours de recette sont identiques
partout ; ce qu'ils valident ne l'est pas.**

En **A**, l'investissement est adossé à la politique de maintenance d'Apereo : **6 mois de support
complet, puis 6 mois de correctifs de sécurité seuls**, sans LTS — position affichée par l'éditeur.
La ligne 7.3.x est en correctifs de sécurité seuls **depuis le 30/06/2026** et **EOL au
31/12/2026**. Une montée dans la ligne 7.x réalisée à l'automne 2026 achèterait donc **quelques
mois**, pas un cycle.

En **B** et en **C**, l'investissement n'est pas à repayer au cycle suivant.

### 3.2 Là où les fourchettes divergent de la cotation en points

Il faut le dire, plutôt que d'harmoniser en silence.

| | A | B | C |
|---|---:|---:|---:|
| Points de complexité (29/07) | 104 | ≥ 157 | **64** |
| Effort relatif en points | ≈ 1,6 × | ≈ 2,5 × | **1 ×** |
| Fourchette en jours (30/07) | 50 – 90 | 70 – 120 | 50 – 100 |
| Effort relatif au plancher | **1 ×** | ≈ 1,4 × | **1 ×** |

**Les deux mesures s'accordent sur B** — dernier dans les deux, et de loin. **Elles divergent sur
A et C** : la cotation en points place C nettement devant A, la fourchette en jours les met à
égalité au plancher et donne même à C un plafond plus haut.

Les deux mesures ne mesurent pas la même chose. Les points intègrent **l'incertitude** et le levier
de l'assistance IA — ils créditent C de ses 7 cas d'usage déjà validés et pénalisent A pour son
travail sur des internes non documentés. La fourchette en jours intègre **le reste à livrer en
conditions réelles** — mire Angular, gestion du mot de passe, industrialisation — là où C n'a pas
d'acquis et où le levier est le plus faible.

**Ce que cette divergence doit produire.** Elle ne doit pas être arbitrée par moyenne. Elle dit que
**A et C ne se départagent pas sur le coût** : à plancher égal, le critère de décision se déplace
sur la durée de vie de l'investissement (§3.1) et sur les risques (§4). C'est exactement ce que
conclut le §6.

### 3.3 Ventilation par fonctionnalité macro

> ⚠️ **À lire avant le tableau — d'où viennent ces nombres.**
>
> **L'équipe a posé trois totaux**, pas quarante-cinq valeurs de détail. La ventilation ci-dessous
> est une **proposition de décomposition** de ces totaux, construite à partir de la matrice
> fonctionnelle du §2, du volume de code par domaine et des constats de code établis les 29 et
> 30/07.
>
> **Elle est donc contrainte, pas mesurée** : chaque colonne somme exactement au total posé par
> l'équipe. Sa valeur est de montrer **où va la charge**, pas de fixer un prix par fonction.
> Chaque ligne est à re-poser en atelier ; le total, lui, est celui de l'équipe.
>
> Les fourchettes basses ne sont pas atteignables ensemble sur toutes les lignes — le plancher d'une
> colonne suppose que tout se passe bien partout, ce qui n'arrive pas. **Lire les colonnes, pas les
> cellules isolées.**

**Unité : jours-homme.** Les cases à `0` signifient « rien à faire dans cette trajectoire »,
et non « négligeable ».

| # | Fonction macro | **A — CAS** | **B — Keycloak** | **C — SAS** |
|---|---|---:|---:|---:|
| — | **Socle et plateforme** — montée de dépendances (A), déploiement du produit et des realms (B), plateforme déjà en place (C) | 8 – 15 | 8 – 14 | **0** |
| 1 | Authentification identifiant / mot de passe sur la base VitamUI | 2 – 4 | 8 – 14 | **0** |
| 2 | Parcours d'entrée multi-organisation | 3 – 6 | **8 – 16** | **0** |
| 3 | Fédération entrante OIDC / SAML 2 | 5 – 10 | 2 – 4 | 0 – 2 |
| 4 | Provisioning à la volée des utilisateurs fédérés | 1 – 2 | 1 – 2 | **0** |
| 5 | Subrogation validée | 3 – 6 | 6 – 11 | 1 – 2 |
| 6 | Jeton d'accès VitamUI, SSO et déconnexion globale | 3 – 6 | 6 – 11 | **5 – 10** |
| 7 | Habilitations VitamUI | **0** | 3 – 5 | **0** |
| 8 | Certificat client X.509 | 2 – 4 | 2 – 4 | **0** *(abandonné)* |
| 9 | MFA sur la base interne | 2 – 4 | 1 – 3 | **0** *(abandonné)* |
| 10 | Gestion du mot de passe | 3 – 6 | 2 – 3 | 4 – 9 |
| 11 | Anti-force brute / verrouillage de compte | 1 – 2 | 0 – 1 | 1 – 2 |
| 12 | Mire de connexion — thème, i18n, ergonomie | 1 – 3 | 4 – 8 | **7 – 16** |
| 13 | Journalisation des connexions | 0 – 1 | 2 – 3 | 0 – 1 |
| 14 | Administration à chaud des fournisseurs d'identité | 1 – 2 | 0 – 1 | 2 – 4 |
| 15 | Administration des organisations, IdP et subrogations | **0** | 0 – 1 | **0** |
| — | **Sécurisation du canal SAS ↔ IAM et dette du prototype** | — | — | **9 – 24** |
| — | **Registre de clients et persistance des autorisations** | — | — | 2 – 5 |
| — | **Packaging, déploiement, supervision** | 0 – 4 | 2 – 4 | 4 – 10 |
| | **Sous-total hors recette** | **35 – 75** | **55 – 105** | **35 – 85** |
| | **Recette end-to-end / QA** | 15 | 15 | 15 |
| | **TOTAL** | **50 – 90** | **70 – 120** | **50 – 100** |

#### Les cinq lignes qui décident du total

Sur quarante-cinq cases, **cinq portent l'essentiel de l'écart** entre les trois colonnes.

**1. La sécurisation du canal SAS ↔ IAM — 9 à 24 j, propre à C. C'est le poste le plus lourd de
toute la matrice.** Il ne s'agit pas d'« ajouter du mTLS ». Trois constats de code en fixent la
difficulté :

- **Le contournement ne porte pas seulement sur l'authentification, mais sur l'isolation
  multi-tenant.** Le commentaire de `WebSecurityConfig` est explicite : les sept chemins
  `/iam/v1/cas/*` contournent *« tenant + token auth »*, au même titre que les endpoints techniques.
  Rétablir la règle suppose donc de rentrer dans le modèle d'habilitations VitamUI, pas seulement
  dans la couche transport.
- **`runAsSystem(customerId, …)` existe parce que la création d'utilisateur exige un principal
  authentifié** ; le commentaire du code indique que `level=""` sert à lever la restriction de
  niveau. Le corriger, c'est reconstruire un contexte d'appel légitime — un travail de modèle
  d'autorisation, pas de configuration.
- **`GET /cas/idp/{id}` traverse ce contournement en renvoyant `clientSecret`, `keystoreBase64` et
  `keystorePassword`.** Le durcissement doit donc traiter aussi le **découpage de la ressource** et
  le **chiffrement des secrets**, pas uniquement l'accès.

S'y ajoute une contrainte issue de la décision du §6.4 : **pendant la coexistence A + C, CAS et le
serveur d'autorisation appellent les mêmes chemins IAM**, avec deux modes d'accès différents — CAS
par rôles applicatifs, SAS par la liste blanche. Le durcissement doit **servir les deux appelants
simultanément sans interrompre CAS en production**. C'est ce qui fait passer ce poste devant la mire.

Enfin, il est **bloquant avant toute mise en service** : cette ligne n'est pas arbitrable à la
baisse, seulement dans le temps. C'est aussi la plus incertaine de la matrice, d'où l'amplitude.

**2. La mire de connexion — 7 à 16 j en C, 1 à 3 j en A.** Deuxième poste de C, et **le seul où C
est structurellement plus cher que A**, puisque A conserve la mire existante.

*La fourchette a été revue à la baisse* (elle était de 10 à 24 j) après vérification de ce qui
existe réellement de part et d'autre :

| | Constat |
|---|---|
| **Ce que le prototype apporte** | La SPA provisoire fait **296 lignes** (`app.js` 246, `index.html` 50, plus la feuille de style). Elle **prouve les enchaînements** — c'est son apport réel — mais elle ne couvre ni la totalité des écrans, ni l'internationalisation, ni le thème |
| **Ce qu'il faut couvrir** | La mire actuelle compte **20 gabarits pour 1 067 lignes** : e-mail, sélection d'organisation, mot de passe, MFA, téléphone manquant, code expiré, quatre écrans de réinitialisation, trois écrans d'erreur de compte, déconnexion, propagation de déconnexion, arrêt de webflow délégué |
| **L'internationalisation** | **fr / en / de**, environ 60 clés par langue — à porter, pas à réinventer |
| **Ce qui allège** | L'espace de travail Angular comporte déjà un projet **`design-system`** et une bibliothèque **`vitamui-library`** partagés par les huit applications. Une application `auth-ui` s'y ajoute en réutilisant les composants existants |
| **Ce qui allège aussi** | **La mire n'applique aucune identité graphique par organisation.** `hasCustomGraphicIdentity` et `themeColors` existent bien sur `CustomerDto`, mais **ne sont référencés nulle part dans `cas-server`**. Il n'y a qu'un thème à porter, pas N |

**Ce qui reste néanmoins du travail réel.** Le prototype ne dispense pas des écrans manquants, et
c'est de l'interface : **le levier de l'assistance IA y est plus faible** que sur le reste de C.
Mais l'ampleur est celle d'un **portage vers une cible connue dans un atelier outillé**, pas d'une
conception. C'est ce qui justifie la révision.

**3. Le parcours d'entrée multi-organisation — 8 à 16 j en B, 0 en C.** L'écart tient à un fait
**établi par test le 27/07** : le modèle d'organisations de Keycloak ne permet pas à N organisations
de partager un domaine. Il faut donc écrire un authentificateur sur mesure et neutraliser le
comportement natif. En C, la fonction est **acquise** — c'est l'un des sept cas d'usage validés.
Cette seule ligne représente près de la moitié de l'écart de plancher entre B et C.

**4. L'authentification sur la base VitamUI — 8 à 14 j en B, 0 en C.** Keycloak doit atteindre les
comptes qui vivent dans Mongo IAM : soit par une SPI de stockage, soit par migration. **La question
n'est pas tranchée** (§2.3), et la fourchette suppose la voie SPI. La voie migration coûterait
davantage et ajouterait une reprise de données absente de ce tableau.

**5. Le socle et la plateforme — 8 à 15 j en A, 0 en C.** A doit repayer la montée de version des
dépendances ; C est **déjà sur la plateforme cible** (Spring Boot 4, Spring Security 7, Java 21).
Cette ligne est celle qui reviendra au cycle suivant en A, et jamais en C.

#### Ce que la ventilation révèle et que les totaux masquaient

**C est la colonne la plus concentrée.** Quatre postes — **sécurisation du canal, mire, jeton/SSO,
mot de passe** — portent **environ 70 %** de sa charge hors recette. Onze fonctions sur quinze y
sont à zéro. **Le risque de C est donc concentré, donc pilotable** : si la fourchette dérape, on
sait d'avance sur quelles lignes.

**Et son poste le plus lourd n'est pas une fonctionnalité.** C'est la conséquence la plus
inconfortable de cette ventilation : la ligne la plus chère de C — la sécurisation du canal — est
**invisible dans la matrice fonctionnelle du §2**, parce qu'elle ne correspond à rien que
l'utilisateur voie. Un COPIL qui ne lirait que le §2 conclurait que C ne coûte presque rien.

**A est la colonne la plus étalée.** Quinze lignes sur seize sont non nulles, aucune ne dépasse
15 j. C'est la signature d'une montée de version : **pas de poste redoutable, mais rien de gratuit
non plus**, et un total qui monte par accumulation. C'est aussi ce qui rend son plafond difficile
à garantir — l'incertitude est répartie partout, donc invisible ligne à ligne.

**B est la seule colonne sans aucun acquis.** Elle ne comporte **aucun zéro** en dehors de
l'administration. Chaque fonction y coûte quelque chose, y compris celles que le produit couvre
nativement — parce qu'il faut les raccorder au modèle VitamUI.

**Le retrait de X.509 et du MFA ne change presque rien.** Ensemble, ils pèsent **4 à 8 j en A** et
**3 à 7 j en B**. Leur abandon en C **ne fait pas la décision** : il économise l'équivalent d'une
semaine, alors que la mire à elle seule en pèse deux à cinq. C'est un point à porter au COPIL —
l'arbitrage de périmètre du §5 se justifie par la cohérence de la cible, **pas par l'économie**.

#### Le seul étalon réel du dossier — et ce qu'il dit de la ligne « recette »

> **CAS 6 → 7 : 25 jours déclarés au ticket, 50 à 60 jours réellement consommés.**
> L'écart correspond à **la recette et aux extras**. *(Précision apportée par l'équipe le 30/07 ;
> les documents antérieurs du dossier ne retiennent que les 25 jours du ticket.)*

C'est le seul chiffre mesuré du dossier sur une montée de version CAS, et il porte **trois
enseignements**, tous inconfortables.

**1. La fourchette A de 50 – 90 j est bien calibrée, et non surévaluée.** Le précédent était une
montée **majeure**, réalisée **avec une application déjà migrée en exemple**, et il a coûté
50 à 60 j. Une montée sans exemple ne coûtera pas moins.

**2. Le taux d'erreur d'estimation sur ce composant est de × 2 à × 2,4.** Ce n'est pas une opinion
sur la rigueur de l'équipe : c'est ce qu'a produit **le même processus d'estimation que celui qui a
produit les fourchettes de ce tableau**. Il n'y a aucune raison de penser qu'il s'est amélioré
depuis, et **cette mise en garde vaut pour les trois colonnes**, pas seulement pour A.

**3. Les 15 jours de recette sont sous-dimensionnés d'un facteur voisin de 2.** Sur le précédent,
la part « recette et extras » a représenté **25 à 35 jours**. Nous en budgétons 15, et de façon
identique dans les trois colonnes. Deux corrections s'imposent :

- **le volume** : 15 j pour recetter quinze fonctions sur six protocoles avec de vrais fournisseurs
  d'identité n'est tenable que s'il s'agit de recette *pure*, hors correction des défauts trouvés.
  Le retour d'expérience du 24/07 recense **13 anomalies sur les seuls OIDC et SAML** ;
- **l'uniformité** : A revalide un comportement qui fonctionnait la veille — on y cherche des
  **régressions**. C éprouve un comportement qui n'a jamais existé — on y cherche des **défauts**.
  Ce n'est pas le même travail. Une valeur identique dans les trois colonnes est une commodité de
  présentation, pas une estimation.

**Ce que cet étalon ne dit pas.** Il ne dit pas que la trajectoire C est mieux estimée que A —
c'est l'inverse. **La fourchette de A s'appuie sur un précédent mesuré ; celle de C ne s'appuie sur
rien de comparable.** Les deux jours des travaux de faisabilité établissent une faisabilité, pas
un rythme : ils ont porté sur les sept fonctions dont le chemin existait déjà, et n'ont produit
qu'un prototype dépourvu de sécurisation, de recette et de packaging — c'est-à-dire dépourvu
précisément de ce qui a fait déraper le précédent CAS.

#### Deux réserves sur ce tableau

**La colonne A chiffre une montée de version majeure**, telle que posée par l'équipe. La cible
retenue au §6.2 est **la ligne 7.x à titre conservatoire** : elle supprime le saut de JDK et le saut
Spring Boot, donc l'essentiel de la ligne « socle et plateforme » et une partie de l'adaptation des
extensions. **Elle se situe au plancher de la colonne, voire en deçà.** Le tableau ne la chiffre pas
séparément faute de cadrage.

**Aucune colonne ne comporte le chantier de découplage** (§8 du rapport du 29/07), commun aux trois
et à compter une fois en plus.

### 3.4 Une estimation indépendante de la trajectoire C

> **Ce que cette section est, et ce qu'elle n'est pas.**
>
> C'est une **seconde estimation de la trajectoire C**, construite indépendamment de celle de
> l'équipe. Elle ne la remplace pas : les deux figurent au dossier, et l'écart entre elles est une
> information en soi.
>
> **Pourquoi seulement C.** Parce qu'elle est la colonne la moins ancrée. La fourchette de A
> s'appuie désormais sur un précédent mesuré (§3.3) ; celle de B, sur une architecture de produit
> documentée. **C ne s'appuie sur rien de comparable** — les deux jours des travaux de faisabilité
> établissent une faisabilité, pas un rythme. Aucune contre-estimation de A ni de B n'est proposée
> ici, faute d'élément nouveau à leur opposer.

#### Le principe retenu : ne corriger que ce qui est mesuré

Le dossier ne contient **qu'un seul chiffre mesuré** — le précédent CAS 6 → 7, 25 jours déclarés
pour 50 à 60 réels (§3.3). Une seule ligne de la ventilation peut donc être recalée sur autre chose
qu'un jugement : celle de la recette. Les cotations de développement de l'équipe sont **reprises
sans modification**. Remplacer un jugement par un autre jugement n'apporte rien.

| Poste | Jours | Origine |
|---|---:|---|
| Développement — durcissement, mire, jeton / SSO, mot de passe, persistance, reste | 31 – 75 | ventilation §3.3, **inchangée** |
| Packaging, déploiement, supervision | 4 – 10 | ventilation §3.3, **inchangée** |
| **Recette et extras** | **25 – 35** | **recalé sur le précédent mesuré** — 15 j au tableau |
| **Total** | **60 – 120** | contre **50 – 100** posés par l'équipe |

**L'écart tient donc entièrement à la ligne recette.** Plancher et plafond montent d'environ 20 %.
Le centre de gravité se situe vers **85 – 95 jours**.

#### Trois natures de postes, qu'il ne faudrait pas additionner

C'est la correction de méthode la plus importante de cette section, et elle vaut indépendamment du
nombre retenu. **Les postes de C ne sont pas de même nature, et la ventilation du §3.3 les présente
à tort sur un pied d'égalité.**

Ces trois lignes **redécoupent les mêmes 35 – 85 jours hors recette** du §3.3, selon ce qu'on peut
en dire plutôt que selon la fonction servie. Le poste de débogage est extrait de la ligne
« jeton, SSO et déconnexion globale ».

| Nature | Postes concernés | Jours | Ce qu'on peut en dire |
|---|---|---:|---|
| **Énumérable** | mire, mot de passe, persistance, packaging, rechargement à chaud, limitation de débit, reste du poste jeton / SSO | 24 – 56 | On sait quoi faire, la variance est faible. **Estimable aujourd'hui** |
| **Suspendu à une décision** | **durcissement du canal SAS ↔ IAM** | 9 – 24 | Contient une décision d'architecture **non prise** : quel modèle de confiance inter-services remplace la liste blanche. Tant qu'elle n'est pas tranchée, ce chiffre est un **espace réservé, pas une estimation** |
| **De débogage** | blocage SSO entre deux `/oauth2/authorize` | 2 – 5 | **Calendaire et non compressible.** Personne ne sait combien de temps prend un débogage avant de l'avoir fait |

**Additionner ces trois natures produit un nombre qui paraît homogène et ne l'est pas.** C'est
précisément le mécanisme qui a produit les 25 jours du ticket CAS 6 → 7.

#### La mesure la moins chère qui réduirait le plus l'incertitude

**Trancher le modèle de confiance inter-services : deux à trois jours de conception, aucune ligne
de code.**

Cette seule décision convertit **le plus gros poste de C** d'espace réservé en estimation. Aucune
re-cotation en atelier n'aura cet effet, parce que l'atelier ne peut pas coter ce qui n'est pas
décidé.

Ce n'est **pas une étude à financer** : c'est la première tâche du travail engagé par la
recommandation du §6.3.

#### Réserve sur cette estimation elle-même

**Elle est produite par le même type de raisonnement que celui qui a produit les 25 jours du
ticket.** Sa seule partie ancrée dans une mesure est la ligne recette ; le reste hérite exactement
de la même faiblesse. **Si l'on applique à cette estimation le taux d'erreur observé sur ce
composant (× 2 à × 2,4), le haut de la fourchette n'est pas un plafond.**

**Ce qui la ferait baisser n'est pas un gain de productivité**, mais deux arbitrages :

- **réduire l'ambition de la mire** — c'est le deuxième poste de C, et le seul dont le contenu
  relève d'un choix et non d'une contrainte technique ;
- **assumer une coexistence plus longue** — faire porter au serveur d'autorisation les seuls
  parcours nouveaux dans un premier temps, en laissant CAS servir l'existant. Cela ne supprime pas
  de charge, cela la déplace hors du premier jalon — mais cela rend le premier jalon atteignable.

---

## 4. Matrice des risques

| Trajectoire | Risque | Nature | Effet s'il se réalise |
|---|---|---|---|
| **A** | **Défaillance silencieuse des extensions** — les 28 points d'accroche reposent sur des noms de beans internes ; un bean renommé n'écrase plus rien, sans erreur de compilation ni de démarrage | constat, lecture de code | Défaut découvert **en recette**, voire en production. Non détecté par le build |
| **A** | **Coût non borné** — surface d'extension que l'éditeur ne s'engage ni à documenter ni à stabiliser | constat, doc éditeur | Le plafond de 90 j n'est pas garanti |
| **A** | **Le précédent CAS 6 → 7 ne se rejouera pas** — il disposait d'une application déjà migrée en exemple, absente cette fois | constat | Sous-estimation de la fourchette |
| **A** | **Le précédent a lui-même coûté le double de ce qui a été déclaré** — 25 j au ticket, **50 à 60 j réels** avec la recette et les extras | constat d'équipe, 30/07 | C'est le taux d'erreur d'estimation observé sur ce composant : × 2 à × 2,4 |
| **A** | **Cycle à repayer** — pas de LTS, support borné | doc éditeur | Le même arbitrage revient dans 6 à 12 mois |
| **A** | **Régression sur l'existant** — le composant de production est modifié en place ; les quinze fonctions du §2 sont à revalider | constat de nature | C'est ce que couvrent les 15 j de recette, et ce qui les rend incompressibles |
| **B** | **Risque de faisabilité identifié** — le modèle d'organisation ne couvre pas le multi-domaine à suffixe partagé | **testé le 27/07, négatif** | Développement de contournement non chiffré, en plus des 120 j |
| **B** | **Ergonomie de connexion proche mais non identique** | constat | Impact utilisateur et conduite du changement, hors périmètre technique |
| **B** | **Décision structurante non tranchée** — Keycloak source de vérité, ou base IAM conservée via SPI ? | question ouverte | Emporte la migration des données et un double référentiel |
| **C** | **Dette de sécurité active** du prototype — canal SAS ↔ IAM non authentifié, endpoints IAM en liste blanche | constat, lecture de code | **Bloquant avant toute mise en service** — chiffré, mais non optionnel |
| **C** | **Nous devenons responsables du composant** | constat d'organisation | Sans compétence interne maintenue dans la durée, la trajectoire n'est pas tenable |
| **C** | **Perte fonctionnelle assumée** — X.509 et MFA interne | **décision de périmètre**, §5 | Nécessite un IdP externe pour les populations concernées |

**Lecture 1 — nature des risques.** Ceux de A et B sont des **incertitudes** : on ne sait pas ce
qu'on trouvera. Ceux de C sont des **charges identifiées** : la dette de sécurité est chiffrée, la
perte fonctionnelle est nommée, la responsabilité interne est une décision d'organisation. Un
risque nommé se traite ; une incertitude ne se traite qu'en la levant, et **aucun essai n'a été
mené sur A**.

**Lecture 2 — le risque sur l'existant n'est pas réparti également.** C'est une asymétrie que la
fourchette de charge ne montre pas.

| | Ce qui arrive au socle en production |
|---|---|
| **A** | Il est **modifié en place**. L'overlay est recompilé contre de nouveaux internes ; les 28 points d'extension peuvent cesser d'accrocher **sans erreur de compilation ni de démarrage**. Les quinze fonctions sont à revalider, et le retour arrière est une reprise de version |
| **B** | Il est **remplacé**, et le référentiel d'identités est déplacé (§2.3) |
| **C** | Il **n'est pas touché**. Le serveur d'autorisation est un module distinct qui coexiste avec CAS ; IAM n'est modifié que par ajout de points d'accès. La bascule se fait par redirection, donc **elle est réversible** |

**La trajectoire C est la seule à ne porter aucun risque fonctionnel sur l'existant.** Ses risques
— dette de sécurité, périmètre, compétence interne — portent tous sur **le nouveau chemin**, pas sur
le service rendu aujourd'hui. C'est ce qui rend la coexistence du §6.4 praticable : investir dans C
ne met pas en jeu le fonctionnement actuel.

---

## 5. Ce que la trajectoire C fait perdre

Deux fonctions du tableau §2 passent en ❌. C'est une **réduction de périmètre assumée**, et elle
doit être portée à la décision explicitement — elle ne relève pas de l'équipe technique.

| Fonction perdue | Situation actuelle | Substitution proposée |
|---|---|---|
| **Authentification par certificat client X.509** | extraction du certificat depuis un en-tête nginx, mapping d'attributs, résolution de l'utilisateur dans IAM | **délégation à un IdP externe** portant l'authentification par certificat |
| **MFA sur la base d'utilisateurs interne** | OTP par SMS, **activation conditionnelle au déploiement**, non maintenu par l'équipe | **délégation à un IdP externe** portant le second facteur |

**Ce que les deux fonctions couvrent réellement aujourd'hui — et qui atténue la perte.**

- **X.509 est déjà une fonction bridée.** Elle ne supporte **pas le multi-domaine** : s'il existe
  zéro ou plusieurs fournisseurs de type certificat pour le domaine, l'authentification échoue.
  Et **la subrogation y est désactivée**. Ce n'est pas un parcours de plein exercice, c'est un
  chemin d'accès restreint.
- **Le MFA ne sollicite aucun point d'accès IAM.** Le jeton transite par le registre de tickets de
  CAS, et le déclenchement dépend de deux attributs de l'utilisateur (`otp`, `mobile`). La fonction
  est **entièrement dans le produit CAS** : rien n'en est réutilisable ailleurs, ce qui confirme
  qu'il n'y a pas de demi-mesure — on la porte intégralement, ou on l'abandonne.

**Le point commun des deux substitutions.** Elles reposent sur la même capacité — la **fédération
entrante OIDC / SAML**, ligne 3 du tableau §2, qui est **validée end-to-end** dans les travaux de
faisabilité. Techniquement, le report est acquis. Ce qui ne l'est pas, c'est la condition
organisationnelle : **elle suppose qu'un IdP externe soit disponible pour les populations
concernées.**

**Trois questions à instruire avec le métier :**

1. **Quelles populations utilisent effectivement X.509 aujourd'hui ?** La fonction est présente au
   socle ; son usage réel n'a pas été qualifié dans ce dossier.
2. **Le MFA interne est-il en service ?** Sa condition d'activation est fausse par défaut et les
   identifiants du fournisseur SMS sont livrés non renseignés. La fonction est probablement
   inactive, mais cela reste à confirmer environnement par environnement.
3. **Chaque population concernée dispose-t-elle d'un IdP externe ?** À défaut, la substitution
   n'en est pas une, et les deux fonctions redeviennent du périmètre à développer.

**Effet sur le chiffrage.** Le retrait de X.509 allège la grille C de **5 points** (43 → 38, total
64 → 59). Le MFA en avait déjà été retiré le 29/07. Le rapport du 29/07 **cotait encore X.509
en C** comme un portage du code CAS existant : ce document acte le changement de position et le
signale comme divergence. **Il n'y a pas de charge de substitution à ajouter** — la fédération qui
la porte est déjà acquise.

---

## 6. Décision proposée

### 6.1 Écarter B — Keycloak

**Motif : trop long et trop risqué.** C'est la trajectoire la plus coûteuse dans les deux mesures
indépendantes (§3.2 — dernière dans les deux, et de loin), la seule à porter un **risque de
faisabilité établi par test** plutôt qu'estimé, et la seule à dégrader l'ergonomie de connexion.

**Ce que cet abandon ne dit pas.** Keycloak reste le meilleur des deux COTS sur le plan de
l'extensibilité — SPI déclarées et documentées contre écrasement de beans internes, cadence de
version soutenable. Il est écarté non pas parce qu'il est le moins bon produit, mais parce que
**le modèle d'entrée VitamUI ne se plie pas au sien**. C'est un constat sur notre modèle, pas sur
le sien — et c'est le même constat qui explique pourquoi CAS est aujourd'hui aussi personnalisé.

### 6.2 Réaliser une montée de version CAS dans la ligne 7.x — à titre conservatoire

**Ce n'est pas le choix d'une cible d'architecture.** C'est une **mesure conservatoire**, motivée
par trois exigences : **politique**, **engagement pris** et **audit** — ne pas rester sur une
version que l'éditeur déclare en fin de vie pendant que la trajectoire cible se construit.

**La cible est conditionnée par la version de Java supportée**, et cette condition est déterminante :

> **CAS 7.1, 7.2 et 7.3 requièrent JDK 21. Seule CAS 8.0 impose JDK 25.**
>
> Le monorepo VitamUI est en Java 21. Rester dans la ligne 7.x **supprime le saut de JDK**, ainsi
> que le saut Spring Boot 4 / Spring Security 7 / Jackson 3 qu'impose CAS 8.0. C'est ce qui rend
> cette mesure réalisable à coût contenu, là où le scénario A du rapport du 29/07 — qui visait
> CAS 8.0 — portait 8 points sur le seul poste JDK.

> ⚠️ **Point de vigilance sur le numéro de version.** L'intention est juste, la cible demande une
> précision. **CAS 7.1.x est EOL.** La politique de maintenance d'Apereo ne référence plus que
> **7.3.x** — en correctifs de sécurité seuls depuis le 30/06/2026, **EOL au 31/12/2026** — et
> précise que *toute version absente du tableau est considérée EOL*.
>
> **Si le motif est politique et d'audit, une montée vers 7.1 ne l'atteint pas** : elle
> substituerait une version EOL à une autre. **La cible à retenir est 7.3.x**, au même JDK 21 et
> pour un effort du même ordre — c'est la seule version 7.x encore référencée.
>
> Elle n'achète toutefois que **jusqu'au 31/12/2026**, et uniquement des correctifs de sécurité.
> C'est cohérent avec un statut de mesure conservatoire ; ce ne serait pas cohérent avec un statut
> de cible.

**Effet sur la fourchette.** Les 50 – 90 j ont été posés pour une montée de version majeure.
Une cible 7.3.x retire le saut de JDK et le saut Spring Boot — elle se situe **au plancher de la
fourchette, voire en deçà**. À reposer par l'équipe une fois la cible confirmée.

### 6.3 Poursuivre C — dans une version industrialisée

**Motif : c'est la seule trajectoire dont l'investissement n'est pas à repayer**, et la seule dont
la faisabilité soit établie par des travaux réels plutôt que par estimation (7 cas d'usage validés,
dont la fédération OIDC et SAML end-to-end).

Le mot **industrialisée** porte la différence entre le prototype et un composant de production.
Il recouvre, au minimum :

- la **sécurisation du canal SAS ↔ IAM** — condition **bloquante** avant toute mise en service ;
- la **mire Angular** en remplacement de la SPA provisoire, avec thème et i18n fr / en / de ;
- la **persistance** du registre de clients et des autorisations ;
- le **parcours de réinitialisation du mot de passe** — la politique, l'historique et l'expiration
  sont déjà dans IAM (§2.2) — et la **limitation de débit par requête**, le verrouillage de compte
  étant lui aussi déjà acquis ;
- le **packaging, le déploiement et la supervision** (Ansible, Consul) ;
- la **recette end-to-end** — 15 j, non compressibles.

### 6.4 Ce que la combinaison des deux produit

A et C **ne sont pas exclusives dans cette lecture** — c'est l'inflexion principale par rapport au
dossier du 29/07, qui les présentait comme trois branches alternatives.

| | Rôle | Horizon | Ce que ça coûte de ne pas le faire |
|---|---|---|---|
| **A — 7.3.x** | mesure conservatoire | immédiat, borné au 31/12/2026 | rester en production sur une version EOL pendant la construction de la cible |
| **C — SAS** | trajectoire cible | au-delà | continuer à repayer le cycle CAS indéfiniment |

**Techniquement, rien ne s'y oppose — et ce n'est pas un hasard.** Les deux chantiers ne touchent
pas le même code : A recompile l'overlay `cas/cas-server`, C développe un module distinct
`api/auth-server`. Leur seule zone commune est `api-iam`, où C **ajoute** des points d'accès sans
modifier ceux qu'utilise CAS. Aucune migration de données n'est en jeu d'aucun des deux côtés
(§2.3), et la bascule vers C se fait par redirection, donc **sans point de non-retour**. Le socle
en production continue de fonctionner sous CAS pendant toute la construction de la cible.

**Une contrainte technique en découle, et elle n'est pas gratuite.** Pendant la coexistence, **CAS
et le serveur d'autorisation appellent les mêmes chemins `/iam/v1/cas/*`** — CAS par ses rôles
applicatifs, SAS par la liste blanche du prototype. Le durcissement du canal (§3.3, poste n° 1)
doit donc **servir les deux appelants à la fois, sans interrompre CAS en production**. C'est plus
difficile que de sécuriser un appelant unique, et c'est chiffré comme tel.

**La contrepartie doit être énoncée** : les deux chantiers consomment la même équipe. Le coût de A
est un coût de transition, entièrement perdu à l'arrivée de C — il n'achète pas un pas vers la
cible, il achète du temps. C'est un arbitrage légitime si l'exigence de conformité est ferme ;
il ne l'est pas si elle ne l'est pas. **C'est la question à poser au COPIL.**

---

## 7. Ce qui reste à trancher

| # | Point | Qui tranche | Sans réponse |
|---|---|---|---|
| 1 | **Abandon de X.509 et du MFA interne** — et disponibilité d'un IdP externe pour les populations concernées (§5) | métier / commanditaire | le périmètre de C n'est pas figé, et sa fourchette non plus |
| 2 | **Fermeté de l'exigence de conformité** qui motive la mesure conservatoire A (§6.4) | commanditaire | on engage un coût de transition perdu sans avoir vérifié qu'il est exigé |
| 3 | **Numéro de version cible de la mesure A** — 7.3.x et non 7.1.x (§6.2) | équipe technique | la mesure n'atteint pas son objectif d'audit |
| 3 bis | **Modèle de confiance inter-services SAS ↔ IAM** — ce qui remplace la liste blanche du prototype. 2 à 3 jours de conception (§3.4) | équipe technique | **le premier poste de charge de C reste non estimable**, et la mise en service reste bloquée |
| 4 | **Chantier de découplage** — sortir la sélection d'organisation et la subrogation du flux d'authentification. Commun aux trois trajectoires, **exclu de toutes les grilles**, à compter une fois en plus. Implique un **changement visible par l'utilisateur** | commanditaire | contrainte d'iso-fonctionnel maintenue, donc modèle spécifique maintenu |
| 5 | **Maintien d'une compétence interne dans la durée** sur le socle d'authentification | direction technique | **la trajectoire C n'est pas tenable** — c'est une condition, pas un souhait |

---

## Documents liés

- **Rapport technique — socle d'authentification** (29/07) — existant, comparatif produit,
  grilles de complexité, quinze réserves :
  `api/auth-gateway/docs/2026-07-29/rapport-technique-socle-authentification.md`
- **Compte rendu COPIL — document de décision** (28/07) :
  `api/auth-gateway/docs/2026-07-28/compte-rendu.md`
- **Synthèse fonctionnelle détaillée** (27/07) — matrice à 30 lignes :
  `api/auth-gateway/docs/2026-07-27/synthese-fonctionnalites-cas-keycloak-vitamui.md`
- **Retour d'expérience des travaux de faisabilité** — fédération OIDC + SAML (24/07) :
  `api/auth-gateway/docs/2026-07-24/retex-federation-oidc-saml.md`
- **Analyse de la cinématique de communication IAM ↔ CAS** — analyse indépendante de la branche
  `develop`, 12 chapitres, diagrammes de séquence par fonction. **Confrontée à la matrice du §2 le
  30/07** : elle en confirme les fonctions et le périmètre — notamment les **7 points d'échange
  `/iam/v1/cas/*`**, ce qui corrobore de source indépendante la correction « 7 endpoints et non 12 »
  du rapport du 29/07 — et elle a fait apparaître **deux fonctions manquantes** (lignes 13 et 14 de
  la matrice) ainsi que le comportement de **non-divulgation d'existence de compte** (note ⁵).
  *Document externe au dépôt.*

### Divergences assumées avec les documents antérieurs

1. **X.509 en trajectoire C.** Le rapport du 29/07 le cote à 5 points comme un portage à réaliser ;
   ce document l'acte comme **abandonné** (§5). Écart : C passe de 64 à **59 points**.
2. **Cible du scénario A.** Le rapport du 29/07 chiffre une montée vers **CAS 8.0** ; ce document
   retient une montée **dans la ligne 7.x** à titre conservatoire (§6.2). Les deux ne sont pas le
   même travail — la grille A du 29/07 ne s'applique pas telle quelle.
3. **Exclusivité des trajectoires.** Le dossier antérieur présente A, B et C comme trois branches
   alternatives ; ce document retient **A et C conjointement**, avec des rôles distincts (§6.4).
4. **Anti-force brute en trajectoire C.** Le rapport du 29/07 le porte à **2 points**, « non traité
   (natif CAS) ». Vérification faite (§2.2, note ²), le compteur, le seuil, le verrouillage et
   l'audit sont **dans IAM**, pas dans CAS, et s'exécutent à chaque `POST /cas/login` : C en hérite.
   Seule subsiste la limitation de débit par requête.
5. **Gestion du mot de passe en trajectoire C.** Cotée **5 points** au 29/07 comme « non traité ».
   La politique, l'historique et l'expiration sont **dans IAM** (§2.2, note ¹) ; ce qui reste est le
   parcours de réinitialisation. La cotation est à revoir à la baisse, sans être annulée.

6. **Journalisation des connexions.** Absente des trois grilles du 29/07. Elle est **dans IAM** et
   acquise en C (§2, note ³) — mais l'analyse révèle au passage que **la fédération OIDC / SAML ne
   produit aucun événement de connexion aujourd'hui**. Lacune du socle actuel, à traiter quelle que
   soit la trajectoire.
7. **Administration à chaud des fournisseurs d'identité.** **Absente des trois grilles**, et c'est
   la seule correction qui va dans le sens de la hausse : elle est **dans CAS**, pas dans IAM, et
   doit être développée en trajectoire C (§2, note ⁴).

> **Effet cumulé sur la grille C — et pourquoi il ne faut pas le soustraire mécaniquement.**
>
> À la baisse : retrait de X.509 (−5), correction de l'anti-force brute (−2), révision partielle du
> mot de passe. À la hausse : un poste absent, le rechargement à chaud des IdP.
>
> Le sous-total « reprise des cas d'usage » de 43 points est **surévalué d'au moins 7 points sur les
> lignes existantes, et incomplet d'au moins une ligne**. Il n'y a donc pas de nouveau total à
> afficher — l'ordre de grandeur reste **inférieur à 64**, sans qu'on puisse dire de combien.
>
> **Une mise en garde de méthode.** Six des sept corrections vont dans le sens de la baisse. Ce
> n'est pas un résultat, c'est un effet de la question posée : l'instruction cherchait ce qui était
> **déjà acquis**. Une instruction symétrique — chercher ce qui manque — a produit d'emblée un poste
> supplémentaire dès qu'un document extérieur a été confronté à la matrice. **La grille reste à
> re-coter en atelier**, et les fourchettes du §3 ne s'en trouvent pas mécaniquement abaissées :
> elles ont été posées indépendamment.

### Sources externes

- Apereo CAS — politique de maintenance :
  `https://apereo.github.io/cas/developer/Maintenance-Policy.html` *(relevée le 29/07/2026,
  confirmée le 30/07/2026 — seule 7.3.x y figure)*
- Apereo CAS — prérequis d'installation (JDK par ligne de version) :
  `https://apereo.github.io/cas/development/planning/Installation-Requirements.html`
- Apereo CAS — publications : `https://api.github.com/repos/apereo/cas/releases/latest`
- Keycloak — publications : `https://api.github.com/repos/keycloak/keycloak/releases/latest`
