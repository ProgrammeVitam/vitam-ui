# Note d'arbitrage — Socle d'authentification VitamUI

> **Document historique — 27 juillet 2026.**
> Repris et actualisé par `api/auth-gateway/docs/2026-07-31/dossier-arbitrage-socle-authentification.md`, **qui fait foi**.
> Conservé pour la traçabilité.
>
> **Ce qui n'est plus exact dans ce document :**
> - **la demande n° 2 — « Financer un essai de montée de version CAS »** (§6), **retirée le 28/07** : *« Nous ne demandons pas de financer de travaux complémentaires d'évaluation »* ;
> - **le découplage qualifié de « décision sans regret »** (§6) — il est depuis le 28/07 **soumis au comité**, car il implique un changement visible par l'utilisateur ;
> - **« 13 endpoints »** — corrigé à **7** le 29/07 (5 des 12 existants ont été créés par le prototype) ;
> - **« environ 25 jours »** pour le précédent CAS 6 → 7 — **50 à 60 jours** réellement consommés (précision du 30/07) ;
> - **« 12 mois de support »** — 6 mois de support complet, puis 6 mois de correctifs de sécurité seuls.

---


*27 juillet 2026 — 2 pages — document technique détaillé en annexe*

---

> ## L'essentiel
>
> **Le composant qui gère la connexion des utilisateurs de VitamUI (Apereo CAS) n'est plus maintenu par son éditeur : il ne reçoit plus aucun correctif de sécurité.** Une action est nécessaire.
>
> Trois trajectoires sont possibles. Elles se distinguent moins par le produit retenu que par **l'endroit où vit notre logique métier**.
>
> **Ce que nous demandons aujourd'hui** : non pas de choisir une trajectoire, mais d'engager le chantier qui les sert toutes les trois, et de financer la vérification qui manque pour trancher en connaissance de cause.

---

## 1. Pourquoi maintenant

Trois faits, indépendants les uns des autres :

**Nous sommes sans correctifs de sécurité.** La version d'Apereo CAS que nous exploitons est en fin de vie. L'éditeur ne publie plus rien pour elle, quelle que soit la gravité d'une éventuelle faille.

**Le problème se reproduira.** L'éditeur maintient chaque version 12 mois, puis l'abandonne. Il n'existe **aucune version à support long**. Rester sur ce produit, c'est accepter une montée de version obligatoire chaque année.

**Ce composant n'est pas planifiable.** Depuis l'origine, **aucune intervention sur CAS — évolution fonctionnelle ou montée de version — n'a jamais tenu dans un sprint de 3 semaines.** La dernière montée de version a demandé environ 25 jours, alors même que nous disposions d'un exemple déjà réalisé. Une brique dont chaque évolution déborde du cycle de développement ne peut être ni engagée, ni découpée, ni arbitrée face à d'autres sujets.

> **Le statu quo n'est pas une option neutre : c'est un risque qui court.**

---

## 2. Ce qui est réellement en jeu

Contrairement à ce que son nom suggère, ce composant ne fait pas que vérifier des mots de passe. **Nous y avons logé du métier VitamUI** : la sélection de l'organisation à la connexion, la subrogation (se connecter à la place d'un autre utilisateur, avec son accord), le jeton d'accès applicatif.

Résultat : **cinq fonctionnalités dont nous avons besoin ne sont couvertes par aucun produit du marché**, ni Apereo CAS, ni Keycloak.

| Fonctionnalité | Pourquoi aucun produit ne la couvre |
|---|---|
| Sélection d'organisation | Une même adresse e-mail correspond à plusieurs comptes dans plusieurs organisations. Les standards du marché supposent un identifiant unique. |
| Multi-domaine à suffixe partagé | Deux organisations ne peuvent pas partager un même nom de domaine. **Vérifié par test sur Keycloak le 27/07.** |
| Subrogation avec accord de l'utilisateur | C'est un processus métier (demande → acceptation → traçabilité), pas un mécanisme d'authentification. |
| Jeton d'accès applicatif | Format propriétaire, lu par une dizaine de composants VitamUI. |
| Modèle d'habilitations | Profils, groupes, tenants, contrats d'accès : spécifiques à VitamUI. |

**Conséquence directe** : quelle que soit la trajectoire retenue, ces cinq fonctionnalités resteront du développement spécifique. **Le choix ne porte pas sur un éditeur, mais sur l'endroit où cette logique métier doit vivre.**

---

## 3. Les trois trajectoires

| | **A — Monter CAS** | **B — Keycloak** | **C — Brique Spring** |
|---|---|---|---|
| **En quoi ça consiste** | Monter CAS vers sa dernière version | Remplacer CAS par le standard du marché | Construire sur un composant du framework que nous utilisons déjà partout |
| **Effort relatif** | ≈ 1,6× | ≈ 2,5× | **référence (1×)** |
| **Ce que ça achète** | **~12 mois**, puis on recommence | Une cible durable | Une cible durable |
| **Ce que nous savons déjà** | **Jamais essayé** | Testé : ne couvre pas notre modèle d'organisations | 7 fonctions clés validées **en 2 jours** |
| **Risque principal** | Coût imprévisible et non planifiable, à repayer chaque année | Recréer ailleurs exactement la dépendance que nous voulons quitter | Nous devenons responsables du composant : compétence à maintenir en interne |

*L'effort relatif intègre le fait qu'adapter un produit du marché est nettement moins productif que développer sur un socle documenté — la dernière montée de version a demandé 25 jours, avec un exemple déjà migré. Cet écart ne joue toutefois que sur la part de développement neuf : l'intégration et la recette ne se compressent dans aucune trajectoire.*

---

## 4. Ce que nous savons, ce que nous supposons

Par honnêteté méthodologique, le niveau de preuve n'est pas le même selon la trajectoire :

| | Niveau de preuve |
|---|---|
| **A — Monter CAS** | Documentation éditeur uniquement. **Aucun essai mené à ce jour.** |
| **B — Keycloak** | Documentation, **plus un point testé** : notre modèle d'organisations n'est pas couvert (résultat négatif) |
| **C — Brique Spring** | 7 fonctions validées de bout en bout par des développements réels |

**La trajectoire A est la seule dont aucune hypothèse n'a été éprouvée.** C'est précisément ce qui manque pour arbitrer à armes égales — et c'est l'objet de notre demande n° 2 ci-dessous.

*Les développements menés sur la trajectoire C sont des travaux de faisabilité. Ils vérifient ce qui est réalisable et à quel coût. Ils n'engagent aucune décision.*

---

## 5. Recommandation de l'équipe technique

**Nous recommandons la trajectoire C**, pour trois raisons :

1. **Elle place le spécifique là où il est déjà** — dans notre code, avec nos outils et nos tests — au lieu de le reloger dans un produit tiers qui ne connaît pas notre modèle.
2. **Elle est la seule à sortir du cycle annuel imposé.** Les trajectoires A et B nous laissent dépendants du rythme de publication d'un éditeur.
3. **C'est la seule dont l'effort a été mesuré plutôt qu'estimé.**

**Trois conditions, non négociables :**

- Une compétence interne durable sur ce composant. Sans elle, la recommandation bascule sur la trajectoire A.
- La sécurisation des échanges entre le nouveau composant et le reste de la plateforme, avant toute mise en service.
- Le maintien de CAS en production jusqu'à couverture fonctionnelle vérifiée.

**Ce qui remettrait cette recommandation en cause :** un essai de montée de version CAS qui se révélerait nettement moins coûteux qu'anticipé, ou l'impossibilité de garantir la compétence interne dans la durée.

---

## 6. Ce que nous demandons

| | Demande | Pourquoi |
|---|---|---|
| **1** | **Engager le chantier de découplage** — sortir la sélection d'organisation et la subrogation du composant d'authentification pour en faire des services métier | **Décision sans regret** : ce chantier réduit le coût des **trois** trajectoires et ne présuppose aucun choix. Il traite la cause, pas le symptôme. |
| **2** | **Financer un essai de montée de version CAS** sur une branche jetable | Mettre les trois trajectoires au même niveau de preuve avant de trancher. Effort limité, valeur d'arbitrage élevée. |
| **3** | **Prendre acte du risque de sécurité** lié à l'absence de correctifs, et valider les mesures compensatoires en attendant | Le risque court dès aujourd'hui, indépendamment de la trajectoire retenue. |

**Le choix de la trajectoire n'est pas demandé aujourd'hui.** Il pourra être arbitré à l'issue de la demande n° 2, sur une base comparable.

---

## Pour aller plus loin

Analyse détaillée — cartographie des fonctionnalités, chiffrage des trois trajectoires, éléments de preuve :
`api/auth-gateway/docs/2026-07-27/synthese-fonctionnalites-cas-keycloak-vitamui.md`
