########
VITAM UI
########



Déploiement
===========

Préparation
-----------

Créer un inventaire depuis ``environments/hosts.example``

Adaptation des group_vars
-------------------------

Editer les fichiers

Surcharge
----------

Editer le fichier vitamui_extra_vars.yml pour surcharger les variables
de group_vas/all si nécessaire.

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

Création des hostvars
----------------------

ansible-playbook -i <inventaire> generate_hostvars_for_1_network_interface.yml --vault-password-file vault_pass.txt

Création des repositories
-------------------------

ansible-playbook --become -i <inventaire> bootstrap.yml --vault-password-file vault_pass.txt [ --extra-vars=@./environments/vitamui_extra_vars.yml ]


Déploiement
------------

ansible-playbook -i <inventaire> vitamui.yml --vault-password-file vault_pass.txt [ --extra-vars=@./environments/vitamui_extra_vars.yml ]


Désinstallation
----------------

ansible-playbook -i <inventaire> uninstall.yml --vault-password-file vault_pass.txt [ --extra-vars=@./environments/vitamui_extra_vars.yml ]
