
# Introduction

Le dossier d’architecture technique de la solution VITAMUI vise à donner une vision d’ensemble de l’architecture applicative et technique de la solution ainsi que de présenter les choix structurants de principes et de composants ainsi que les raisons de ces choix.

Il s’adresse aux personnes suivantes :

* Les architectes techniques
* Les devops, les ingénieurs systèmes et exploitants

Pour assurer la cohérence de la solution avec VITAM, l’architecture retenue pour VITAMUI se rapproche fortement de celle mise en oeuvre par le socle VITAM. Bien que plusieurs paragraphes partagent les mêmes informations, ce document ne traite pas directement de l’architecture VITAM dont la documentation est disponible sur le site web du Programme VITAM. 

Il convient néanmoins d'être attentif, car certains choix et briques technologiques utilisés pour développer VITAMUI sont différents de ceux employés par VITAM.

## Structure du document

Ce document est séparé en deux parties distinctes :

* L’architecture applicative, principalement à destination des architectes applicatifs
* L’architecture technique, avec :
    * les principes d’architecture technique à destination des architectes d’infrastructure
    * les choix d’architecture et de composants techniques, à destination des architectes d’infrastructure et des exploitants

Les principes et règles de sécurité appliquées et applicables à la solution sont réparties dans les différents chapîtres.

## Objectifs de la solution

La solution VITAMUI a pour objectif de répondre aux principaux enjeux métiers et techniques liés à l’archivage. Dans cette optique la solution offre des IHM web simples et ergonomiques pour l’administration des organisations, des utilisateurs, des profils de sécurité et d'autres fonctionnalités métiers.

## Contributions et licences

La solution VITAMUI est construite sur une contribution initiale de la société [XELIANS Archivage](https://www.xelians.fr/).  

Plusieurs partenaires et contributeurs participent à l'enrichissement de la solution VITAMUI, notamment le Projet interministériel [VAS](http://www.programmevitam.fr/pages/VaS/), le [CEA](https://www.cea.fr/), le [CINES](https://www.cines.fr/).

La solution VITAMUI s'inscrit aujourd'hui dans le cadre du [Programme VITAM](https://www.programmevitam.fr/) porté par l'Etat Français pour développer une solution complète d'archivage numérique. La solution est soumise à la [licence Ceccil C v2.1](https://cecill.info/licences/Licence_CeCILL_V2.1-fr.html). 

Les différentes couches et services internes de la solution VITAMUI communiquent avec les clients externes java de la solution VITAM, ces derniers sont publiés sous la licence [CeCILL-C](https://cecill.info/licences/Licence_CeCILL-C_V1-fr.html).

Si vous souhaitez contribuer au développement officiel de la solution, merci de [contacter le Programmme VITAM](contact@programmevitam.fr).
