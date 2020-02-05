########
VITAM UI
########



Déploiement
===========

Préparation
-----------

Créer un inventaire depuis ``environments/hosts.example``

Adaptation des grou_vars
-------------------------

Editer les fichiers

Bootstrap
---------

Pour que les VM puissent pointer sur les dépôts nécessaires.

Template de repo
~~~~~~~~~~~~~~~~

Fichier ``environments/group_vars/all/repositories.yml``

Lancement du playbook
~~~~~~~~~~~~~~~~~~~~~

ansible-playbook -i <inventaire> bootstrap.yml --vault-password-file vault_pass.txt

PKI
---

./pki/scripts/generate_ca.sh

./pki/certs/generate_certs.sh <inventaire>

./generate_stores.sh <inventaire>


Déploiement
------------

ansible-playbook -i <inventaire> vitamui.yml --vault-password-file vault_pass.txt

