# Observations

> **Document historique — 28 juillet 2026.**
> Repris et actualisé par `api/auth-gateway/docs/2026-07-31/dossier-arbitrage-socle-authentification.md`, **qui fait foi**.
> Conservé pour la traçabilité.
>
> **Ce qui n'est plus exact dans ce document :**
> - **« 4 versions à mettre à jour »** — le dossier a longtemps écrit « 5 sauts successifs » ; le décompte retenu est **4 sauts** de 7.0 vers 8.0.

---


## Actions

* Faire un rapport pour l'aide à la décision à l'aide des informations et des pistes explorées par l'analyse.

## Plan

* Présentation de l'existant
* Raisons d'être de l'analyse
* Pistes étudiées
  * Mise à jour de CAS
    * Expériences passées
    * 4 versions à mettre à jour
    * Migration spring-boot 3 vers 4
    * résolution problème jackson
    * upgrade java 25
  * Mise en place de keycloak
    * utilisation générique (non iso fonctionnel)
    * utilisation iso fonctionnelle (nécessite développement custom)
      * développement de SPI Keycloak pour les features de CAS + Module Mongo DB
      * Utilisation uniquement identité, nécessite des développements pour gérer les tokens (très similaire à la solution 3)
  * Mise en place de spring authorization server
    * écosystème spring
    * application java, coût de déploiement et configuration quasi nul
    * agnostique d'une quelconque solution IAM
    * iso fonctionnel
    * nécessite développement pour les feature fournies par l'IAM (MFA, sms, emails password reset...)
* Cout de mise en oeuvre des différentes solutions
  * CAS (couteux au regard de l'historique, jamais sûr à chaque upgrade)
  * Keycloak (couteux, plus de docs que CAS pour le montées de version, incertitude jamais fait)
  * Spring Authorization Server (couteux, reste java, code maitrisé en interne, beaucoup de docs,déjà dans la stack technique, probablement moins couteux à la mise à jour)
* Préconisation de la 3ème approche car l'iso fonctionnel implide que de conserver le modèle actuel qui n'est pas compatible avec des outils sur étagère (cf custom CAS, nécessite de custom Keyclaok si on garde iso fonctionnel)

