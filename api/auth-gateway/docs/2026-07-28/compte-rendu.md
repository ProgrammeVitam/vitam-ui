# Socle d'authentification VitamUI — Aide à la décision

> **Document historique — 28 juillet 2026.**
> Repris et actualisé par `api/auth-gateway/docs/2026-07-31/dossier-arbitrage-socle-authentification.md`, **qui fait foi**.
> Conservé pour la traçabilité.
>
> **Ce qui n'est plus exact dans ce document :**
> - **« 13 points d'échange »** — corrigé à **7** le 29/07 ;
> - **« environ 25 jours »** pour le précédent CAS 6 → 7 — **50 à 60 jours** réels (précision du 30/07). *Ce chiffre corrigé renforce l'argument du §1 selon lequel aucune intervention n'a jamais tenu dans un sprint de trois semaines.* ;
> - **« chaque version maintenue douze mois »** — 6 mois de support complet, puis 6 mois de correctifs de sécurité seuls ;
> - **le choix de trajectoire présenté comme exclusif (A, B ou C)** — depuis le 30/07, **A et C sont retenues conjointement**, avec des rôles distincts.
>
> - **Les décisions actées en séance restent valides** et sont reprises au §11 du document de référence.

---


*Comité de pilotage — 28 juillet 2026*

---

> ## L'essentiel
>
> **Le composant qui gère la connexion des utilisateurs de VitamUI n'est plus maintenu par son éditeur.** Il ne reçoit plus aucun correctif, y compris de sécurité. Le statu quo n'est pas une position neutre.
>
> Trois trajectoires de remplacement ou de remise à niveau ont été instruites. Elles se distinguent moins par le produit retenu que par **l'endroit où vit notre logique métier**.
>
> **Le constat central de l'analyse** : aucun produit du marché ne couvre le fonctionnement actuel de VitamUI. L'exigence d'iso-fonctionnel, retenue dans l'expression de besoin, porte donc **l'essentiel du coût des trois trajectoires** — bien davantage que le choix du produit lui-même.
>
> **Ce que nous soumettons au comité** : le détail de ce que coûte chaque exigence fonctionnelle, afin que le périmètre puisse être arbitré poste par poste. C'est aujourd'hui le seul levier d'économie réellement disponible.
>
> Le choix de trajectoire n'est pas demandé en séance.

---

## 1. Pourquoi maintenant

Trois faits indépendants les uns des autres.

**Nous sommes sans correctifs de sécurité.** La version d'Apereo CAS exploitée en production est en fin de vie. L'éditeur ne publie plus rien pour elle, quelle que soit la gravité d'une faille éventuelle.

**Le problème se reproduira chaque année.** L'éditeur maintient chaque version douze mois, puis l'abandonne. Il n'existe **aucune version à support long**. Rester sur ce produit revient à accepter une montée de version obligatoire tous les ans.

**Ce composant n'est pas planifiable.** Depuis l'origine, **aucune intervention sur ce composant — évolution fonctionnelle ou montée de version — n'a jamais tenu dans un sprint de trois semaines.** La dernière montée de version a demandé environ 25 jours, alors même que l'équipe disposait d'un exemple déjà réalisé. Le coût récurrent de maintien est estimé entre 20 et 40 jours-homme par an.

Une brique dont chaque évolution déborde du cycle de développement ne peut être ni engagée en début d'itération, ni découpée en incréments livrables, ni arbitrée à parité face à d'autres sujets. Elle impose son rythme au projet.

---

## 2. L'existant et la nature du problème

Contrairement à ce que son nom suggère, ce composant ne se contente pas de vérifier des mots de passe. **Nous y avons logé du métier VitamUI** : la sélection de l'organisation au moment de la connexion, la subrogation, l'émission du jeton d'accès applicatif, le contrôle du statut des comptes.

Cela représente aujourd'hui environ **7 700 lignes de code spécifique** et **13 points d'échange dédiés** avec le module de gestion des identités.

C'est la source du coût, et c'est aussi ce qui explique pourquoi la question ne se règle pas par un simple changement de produit : **le besoin exprimé n'est pas un besoin d'authentification, c'est un besoin métier logé dans le composant d'authentification.**

---

## 3. Ce que « iso-fonctionnel » recouvre

Cinq fonctionnalités dont VitamUI a besoin ne sont couvertes par **aucun produit du marché**, ni celui utilisé aujourd'hui, ni son principal concurrent.

| Fonctionnalité | Pourquoi aucun produit ne la couvre |
|---|---|
| Se connecter avec la même adresse e-mail dans plusieurs organisations | Les standards du marché supposent qu'une adresse identifie une seule personne dans un seul périmètre. |
| Plusieurs organisations partageant un même nom de domaine de messagerie | **Vérifié par test le 27/07** : le produit concurrent n'autorise pas deux organisations sur un même domaine. |
| Subrogation avec accord de la personne concernée | C'est un processus métier — demande, acceptation, traçabilité — et non un mécanisme d'authentification. |
| Jeton d'accès applicatif propriétaire | Format spécifique à VitamUI, lu par une dizaine de composants de la plateforme. |
| Modèle d'habilitations VitamUI | Profils, groupes, organisations, contrats d'accès : propres au produit. |

**Conséquence directe** : quelle que soit la trajectoire retenue, ces fonctionnalités resteront du développement spécifique. Le scénario « on remplace le produit et c'est réglé » n'existe pas.

*Le modèle d'habilitations fait exception dans le raisonnement qui suit : il reste dans le module de gestion des identités dans les trois trajectoires, et ne constitue donc pas un poste d'arbitrage.*

---

## 4. Ce que coûte l'iso-fonctionnel, poste par poste

C'est la section centrale de ce document. Les quatre exigences ci-dessous concentrent l'essentiel du surcoût de migration, **dans les trois trajectoires**. Les décomposer permet de les arbitrer une par une plutôt qu'en bloc.

| Exigence | A — Monter la version actuelle | B — Produit concurrent | C — Brique du framework interne | Ce qui change si l'on y renonce |
|---|---|---|---|---|
| **Même e-mail dans plusieurs organisations** | Code existant, à réadapter | Développement spécifique lourd | **Déjà vérifié** | L'utilisateur choisit explicitement son organisation sur l'écran de connexion. Fonctionnement standard, coût quasi nul. |
| **Domaine de messagerie partagé entre organisations** | Code existant, à réadapter | **Testé : non couvert.** Contournement intégral à développer | **Déjà vérifié** | Un domaine de messagerie n'appartient qu'à une organisation. Impose de revoir les organisations concernées. |
| **Subrogation avec accord de la personne** | Code existant, à réadapter | Développement spécifique lourd | Implémenté, validation en attente | On retombe sur une prise de contrôle par un administrateur, sans consentement ni traçabilité métier de l'accord. |
| **Jeton d'accès applicatif propriétaire** | Code existant, à réadapter | Couche d'émission à développer | **Contrat préservé** | Migration d'une dizaine de composants de la plateforme vers un format standard. Chantier réel, mais qui supprime une spécificité durable. |

**Trois lectures de ce tableau.**

1. **Les renoncements ne sont pas équivalents.** Le premier est peu coûteux fonctionnellement et très rentable techniquement. Le quatrième est l'inverse : renoncer au jeton propriétaire supprime une dette durable, mais ouvre un chantier sur toute la plateforme. Le deuxième et le troisième sont des décisions métier qui n'appartiennent pas à l'équipe technique.
2. **Ce tableau est le seul levier d'économie disponible au comité.** Le choix du produit déplace le coût ; l'arbitrage du périmètre le réduit.
3. **Sortir ces fonctions du composant d'authentification pour en faire des processus métier** — le chantier de découplage — réduit le coût des trois trajectoires. Mais il suppose de reconnaître qu'une partie de ce parcours n'a pas à vivre dans l'authentification, ce qui **implique un changement fonctionnel visible par l'utilisateur**. Ce chantier n'est donc pas sans conséquence pour le commanditaire, et c'est à ce titre qu'il est soumis au comité et non conduit par défaut.

---

## 5. Les trois trajectoires

| | **A — Monter la version actuelle** | **B — Produit concurrent** | **C — Brique du framework interne** |
|---|---|---|---|
| **En quoi cela consiste** | Monter le composant actuel vers sa dernière version | Remplacer le composant par le standard le plus répandu du marché | Construire sur un composant du framework déjà utilisé partout dans VitamUI |
| **Effort relatif** | ≈ 1,6× | ≈ 2,5× | **référence (1×)** |
| **Ce que cela achète** | **~12 mois**, puis le cycle recommence | Une cible durable | Une cible durable |
| **Niveau de preuve** | Documentation éditeur. **Aucun essai mené à ce jour.** | Documentation, **plus un point testé** — résultat négatif | 7 fonctions clés validées **en 2 jours** par des travaux réels |
| **Où vit le spécifique** | Inchangé, dans le composant actuel | Extensions du produit tiers | Dans notre code |
| **Risque principal** | Coût imprévisible, non planifiable, à repayer chaque année | Recréer ailleurs la dépendance que nous cherchons à quitter | Nous devenons responsables du composant : compétence à maintenir en interne |

**Trois précisions de méthode, à garder en tête en lisant ce tableau.**

**Le niveau de preuve est inégal, et cela joue en faveur de C.** La trajectoire C est la seule dont la faisabilité a été explorée par des développements réels : ses inconnues sont levées, celles de A et B restent devant. *Un scénario mesuré paraît toujours plus sûr qu'un scénario estimé, y compris lorsqu'il ne l'est pas.* Nous le signalons par honnêteté méthodologique.

**Cet écart ne justifie pas pour autant d'investir dans une mesure complémentaire sur la trajectoire A.** Quel qu'en soit le résultat, cette trajectoire achète douze mois : l'éditeur ne publie aucune version à support long, et le cycle est à repayer intégralement l'année suivante. Un essai préciserait un coût ; il ne lèverait pas ce qui disqualifie la trajectoire. **La question posée par A n'est pas « combien coûte la montée de version ? », mais « acceptons-nous de la refaire chaque année ? »** — et cette question se tranche sans essai.

**Les développements menés sur la trajectoire C sont des travaux de faisabilité.** Ils vérifient ce qui est réalisable et à quel coût. Ils sont de même nature que la vérification restant à mener sur A et B, simplement conduits en premier. **Ils n'engagent aucune décision et ne préjugent d'aucun choix.**

**L'écart d'effort ne porte que sur une partie du travail.** Adapter les composants internes d'un produit tiers est nettement moins productif que développer contre une documentation publique : la dernière montée de version a demandé 25 jours, alors même qu'une application déjà migrée servait d'exemple — exemple qui n'existera pas cette fois. Cet écart joue en faveur de la trajectoire C, mais **sur la part de développement neuf uniquement, soit moins d'un tiers de ce qu'il lui reste à faire**. L'intégration et la recette, qui dominent le calendrier, ne se compressent dans aucune trajectoire. **L'avantage de C tient donc moins à sa vitesse d'exécution qu'à l'absence de cycle à repayer.**

*Une matrice de coûts a circulé en parallèle de cette analyse. Elle chiffre pour le composant actuel une montée de version mineure qui n'est plus disponible — les versions intermédiaires étant toutes en fin de vie. Ses totaux ne sont donc pas utilisables en comparatif en l'état. Le détail figure en annexe.*

---

## 6. Dans quel cas chaque trajectoire est le bon choix

| Retenir… | …si |
|---|---|
| **A — Monter la version actuelle** | La priorité absolue est de ne rien changer fonctionnellement à court terme, et le comité accepte de repayer le cycle tous les douze mois. |
| **B — Produit concurrent** | La priorité est de sortir du sur-mesure et le comité accepte d'assouplir significativement le périmètre du §4. C'est la seule trajectoire qui apporte en plus une authentification renforcée moderne et une console d'administration prête à l'emploi. |
| **C — Brique du framework interne** | L'iso-fonctionnel est confirmé comme non négociable. C'est alors la trajectoire où le spécifique coûte le moins cher à porter et à maintenir. |

Ce tableau appelle une observation : **l'exigence d'iso-fonctionnel portée par l'expression de besoin conduit d'elle-même à la trajectoire C.** La recommandation qui suit n'est pas une préférence de l'équipe technique : elle est la conséquence du périmètre demandé. Modifier ce périmètre modifierait la recommandation.

---

## 7. Avis de l'équipe technique

**Nous recommandons la trajectoire C**, à périmètre fonctionnel inchangé, pour trois raisons :

1. Elle place le spécifique là où il est déjà — dans notre code, avec nos outils et nos tests — au lieu de le reloger dans un produit tiers qui ne connaît pas notre modèle.
2. Elle est la seule à sortir du cycle annuel imposé par l'éditeur.
3. C'est la seule dont l'effort a été mesuré plutôt qu'estimé.

**Trois conditions, non négociables :**

- Une compétence interne durable sur ce composant. Sans elle, la recommandation bascule sur la trajectoire A.
- La sécurisation des échanges entre le nouveau composant et le reste de la plateforme, avant toute mise en service.
- Le maintien du composant actuel en production jusqu'à couverture fonctionnelle vérifiée.

**Ce qui remettrait cette recommandation en cause :**

- **La publication par l'éditeur d'une version à support long**, qui supprimerait le cycle annuel et redonnerait sa valeur à la trajectoire B.
- L'impossibilité de garantir la compétence interne dans la durée.
- **Un assouplissement large du périmètre iso-fonctionnel** : si plusieurs exigences du §4 sont levées, la trajectoire B redevient compétitive et apporte en outre des fonctions que nous n'avons pas aujourd'hui.

**Contrepartie assumée de la trajectoire C** : tout ce que le produit actuel fournissait nativement — authentification par certificat, second facteur par SMS, gestion des mots de passe, protection contre les tentatives répétées — est à réécrire. Et le calendrier n'est pas dominé par le développement mais par l'intégration, la recette avec de vrais fournisseurs d'identité et la refonte de l'écran de connexion, qui ne se compressent pas.

---

## 8. Décisions attendues

**En séance :**

| N° | Décision | Pourquoi maintenant |
|---|---|---|
| **1** | **Mandater l'instruction du périmètre iso-fonctionnel** avec le métier, sur la base du §4, pour un arbitrage poste par poste en séance ultérieure. | C'est le seul levier de réduction du coût, et il est indépendant du choix de trajectoire. |
| **2** | **Prendre acte du risque de sécurité** lié à l'absence de correctifs, et valider les mesures compensatoires dans l'intervalle. | Le risque court dès aujourd'hui, indépendamment de la trajectoire retenue. |

**Reporté à une séance ultérieure :**

| N° | Décision | Conditionnée à |
|---|---|---|
| **3** | **Choix de la trajectoire** (A, B ou C). | L'aboutissement de la décision 1. |

*Nous ne demandons pas de financer de travaux complémentaires d'évaluation. L'arbitrage du périmètre (décision 1) est instruit à partir des éléments déjà réunis ; il porte sur des exigences fonctionnelles, non sur des inconnues techniques.*

---

## Annexes

Documents de référence, disponibles sur demande :

- **Récapitulatif des complexités** — grilles détaillées poste par poste des trois trajectoires, échelle de complexité, synthèse comparée :
  `api/auth-gateway/docs/2026-07-28/recapitulatif-complexites.md`
- **Analyse détaillée** — cartographie fonctionnalité par fonctionnalité, éléments de preuve, réconciliation avec la matrice de coûts diffusée en parallèle :
  `api/auth-gateway/docs/2026-07-27/synthese-fonctionnalites-cas-keycloak-vitamui.md`
- **Note d'arbitrage — version courte (2 pages)** :
  `api/auth-gateway/docs/2026-07-27/note-arbitrage-socle-authentification.md`
- **Retour d'expérience des travaux de faisabilité** — fédération d'identité externe, difficultés rencontrées et levées :
  `api/auth-gateway/docs/2026-07-24/retex-federation-oidc-saml.md`
