# Socle d'authentification VitamUI — synthèse de décision

*30 juillet 2026 — COPIL*

---

## Le contexte en trois phrases

Le socle d'authentification repose sur Apereo CAS 7.0.10.1, **version que l'éditeur déclare en fin
de vie**. Trois trajectoires ont été instruites : monter de version, transposer vers Keycloak, ou
internaliser le composant sur Spring Authorization Server. La décision porte sur la trajectoire
cible **et** sur ce qu'on fait en attendant qu'elle existe.

---

## La matrice

**Charges en points de charge.** La ligne recette n'est pas un forfait : elle est recalée sur le
seul précédent mesuré du dossier — la dernière montée de version CAS, **qui a coûté environ le
double de ce qui avait été déclaré**, l'écart étant précisément la recette et les extras. Les trois
colonnes sont construites de la même façon.

| | **A — Montée CAS 7.3** | **B — Keycloak 26** | **C — Spring Authorization Server** |
|---|---|---|---|
| **Nature** | rester sur le produit, changer de version | changer de produit sur étagère | internaliser le composant |
| **Développement** | 25 – 53 pts | 39 – 74 pts | 25 – 60 pts |
| **Recette et extras** | 17 – 24 pts | 17 – 24 pts | 17 – 24 pts |
| **TOTAL** | **42 – 77 pts** | **56 – 98 pts** | **42 – 84 pts** |
| **Effort relatif au plancher** | **1 ×** | 1,3 × | **1 ×** |
| **Ce que ça achète** | du support **jusqu'au 31/12/2026** — 7.3.x est en correctifs de sécurité seuls | une cible durable, sur un produit tiers | une cible durable, sur du code maîtrisé |
| **Migration de données** | aucune | **oui — question non tranchée** | aucune |
| **Impact sur la production** | le composant est **modifié en place** | il est **remplacé** | **aucun** — module distinct, bascule réversible |
| **Points forts** | iso-fonctionnel par construction · aucune décision d'architecture à prendre · périmètre borné et définition du fini mécanique · **seule fourchette calibrée sur un précédent mesuré** · échec détectable en quelques jours | produit maintenu sur la durée · extension par interfaces documentées et versionnées · console et API d'administration natives · **le meilleur des deux produits sur étagère** | **7 fonctions déjà validées**, dont la fédération OIDC et SAML · plateforme cible déjà en place · **aucun cycle à repayer** · ne met pas en jeu le service actuel · compétence dans l'écosystème déjà maîtrisé |
| **Points faibles** | **n'apporte aucune fonction et n'en retire aucune** — 13 chantiers sur 15 à coût non nul et bénéfice nul · le couplage métier reste entier · extension par **écrasement de composants internes non documentés** · **à repayer au cycle suivant** | **aucun acquis** — aucune fonction gratuite, y compris celles couvertes nativement · **le modèle d'organisation ne couvre pas notre besoin** · subrogation de sémantique différente · **ergonomie de connexion proche mais non identique** · le plus cher des trois | **perte de l'authentification par certificat X.509 et du MFA sur base interne** · mire de connexion à réécrire · **nous devenons responsables du composant** · colonne la moins bien estimée du dossier |
| **Risque principal** | **Défaillance silencieuse.** Les extensions s'accrochent à des noms de composants internes du produit ; un renommage ne casse ni la compilation ni le démarrage — **le défaut se découvre en recette, voire en production** | **Faisabilité, établie par test.** N organisations ne peuvent pas partager un domaine dans le modèle Keycloak. Un contournement sur mesure est nécessaire, **non chiffré dans le total** | **Dette de sécurité du prototype.** Le canal entre le serveur d'autorisation et la gestion des identités contourne l'authentification **et l'isolation multi-locataire**. **Bloquant avant toute mise en service** |
| **Risque secondaire** | **Coût non borné**, et la dernière montée de version a été **sous-estimée d'un facteur 2**. Elle ne se rejouera pas à l'identique : elle disposait d'une application déjà migrée en exemple | **Double référentiel** — Keycloak source de vérité, ou base actuelle conservée derrière une interface ? La question emporte la migration des comptes | **Décision d'architecture non prise** — le modèle de confiance entre services. Tant qu'elle n'est pas tranchée, le premier poste de charge n'est pas estimable |
| **Statut proposé** | **mesure conservatoire** | **écarté** | **trajectoire cible** |

**A et C ne se départagent pas sur le coût** : elles sont à égalité au plancher, et l'écart de
plafond est du même ordre que l'incertitude des deux. **Le critère de décision se déplace donc sur
la durée de vie de l'investissement et sur les risques**, pas sur la charge. C'est ce que fait la
section suivante.

*Le chiffre de A porte en outre une réserve dans le sens de la baisse : il chiffre une montée de
version majeure, alors que la cible 7.3.x retenue supprime le saut de Java et le saut Spring Boot.*

---

## La décision proposée

### Écarter B

La plus coûteuse des trois dans toutes les lectures, la seule à porter un risque de faisabilité
**établi par test** plutôt qu'estimé, et la seule à dégrader l'ergonomie de connexion.

Ce n'est pas un jugement défavorable sur le produit — c'est le meilleur des deux candidats sur
étagère. **C'est notre modèle d'entrée qui ne se plie pas au sien**, et c'est le même constat qui
explique pourquoi le socle actuel est aussi personnalisé.

### Réaliser une montée de version CAS à titre conservatoire

Motif **politique, engagement et audit** : ne pas rester sur une version déclarée en fin de vie
pendant que la cible se construit. Ce n'est pas un choix d'architecture.

> **La cible est 7.3.x, pas 7.1.x.** CAS 7.1, 7.2 et 7.3 fonctionnent toutes sur Java 21 — seule
> la version 8.0 impose Java 25, ce qui la met hors de portée à coût contenu. Mais **7.1 est
> elle-même en fin de vie** : le tableau de maintenance de l'éditeur ne référence plus que 7.3.x.
> Monter vers 7.1 remplacerait une version non maintenue par une autre et **n'atteindrait pas
> l'objectif d'audit**.

### Poursuivre C, dans une version industrialisée

Seule trajectoire dont l'investissement n'est pas à repayer, et seule dont la faisabilité est
établie par des travaux réels. « Industrialisée » recouvre au minimum : sécurisation du canal entre
services — **bloquante** —, mire de connexion, persistance, gestion du mot de passe, packaging et
supervision.

### Les deux ensemble

**Rien ne s'y oppose techniquement** : les chantiers ne touchent pas le même code, aucune migration
de données n'est en jeu, et la bascule vers la cible se fait par redirection, donc sans point de
non-retour. Le socle actuel continue de servir pendant toute la construction.

**Mais les deux consomment la même équipe.** Le coût de la mesure conservatoire est **entièrement
perdu** à l'arrivée de la cible : il n'achète pas un pas vers elle, il achète du temps. C'est
légitime si l'exigence de conformité est ferme. **C'est la question à trancher en séance.**

---

## Ce qui reste à trancher

| | Point | Qui tranche |
|---|---|---|
| 1 | **Fermeté de l'exigence de conformité** qui motive la mesure conservatoire — on engage sinon un coût de transition perdu | commanditaire |
| 2 | **Abandon de X.509 et du MFA sur base interne** en trajectoire cible, avec report sur un fournisseur d'identité externe — sous réserve qu'il en existe un pour les populations concernées | métier |
| 3 | **Modèle de confiance entre services** — 2 à 3 points de conception, aucune ligne de code. Sans lui, le premier poste de charge de la cible reste non estimable et la mise en service reste bloquée | équipe technique |
| 4 | **Sortie de la sélection d'organisation et de la subrogation du parcours de connexion** — commun aux trois trajectoires, **compté dans aucune**, et **visible par l'utilisateur** | commanditaire |
| 5 | **Maintien d'une compétence interne dans la durée** — condition de la trajectoire cible, pas un souhait | direction technique |

---

## Trois avertissements de méthode

**1. Les charges sont des estimations, les risques sont des constats.** Les fourchettes n'ont été
réalisées par personne — aucune des trois trajectoires n'a été menée. Les risques, eux, sont
établis par test, par lecture du code ou par la documentation de l'éditeur. **En cas de
contradiction, ce sont les risques qui doivent emporter la décision.**

**2. Le taux d'erreur d'estimation observé sur ce composant est de 2 à 2,4.** Il a été produit par
le même processus que celui qui a produit ces fourchettes. **Cet avertissement vaut pour les trois
colonnes.**

**3. Le poste le plus lourd de la trajectoire cible n'est pas une fonctionnalité.** C'est la
sécurisation du canal entre services : rien n'est ajouté, rien n'est retiré, le service rendu est
identique avant et après — et c'est la ligne la plus chère du dossier.
