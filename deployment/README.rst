#####################
Déploiement VITAM-UI
#####################

Préparation
============

Inventaire
-----------

Dans ``environments``, créer un inventaire depuis ``hosts.example``

Adaptation des group_vars
-------------------------

Sous ``environments/group_vars/all``, éditer les fichiers au besoin:

Fichier ``ansible_options.yml``
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Permet de gérer le nombre de tentatives d'installation de packages, ainsi que le délai entre chaque tentative. Les valeurs par défaut devraient être suffisantes.

Fichier ``consul_vars.yml``
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Permet de gérer le paramétrage de Consul. Le paramètre utile est network: "ip_admin", par défaut donc, relié sur le réseau identifié par les host_vars "ip_admin".

Si consul vitam-ui doit être rattaché à consul vitam, décommenter la fin du fichier et paramétrer "à la vitam".

Fichier ``elasticsearch_log_vars.yml``
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Ce fichier permet la configuration du cluster Elasticsearch sur lequel logstash envoie les données.
Fichier ``infra.yml``
~~~~~~~~~~~~~~~~~~~~~

Ce fichier décrit l'acèès à des services d'infrastructure :

* accès à un serveur d'envoi de mail (smtp)
* plate-forme d'enoi de SMS (pour désactive cette section, passer à ``enabled: false``)

Fichier ``kibana_vars.yml``
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Paramérage *vitam-like* pour kibana (semble inutile)

Fichier ``logstash_vars.yml``
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Paramétrage du composant ``vitamui-logstash`` :

* nom du *package*
* nom du service
* ports d'écoute
* gestion des log (rétention, tailles max, ...)

Fichier ``mongo_express_vars.yml``
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Paramétrage du composant ``mongo-express`` :

* nom du *package*
* nom du service 
* port découte
* base URI

Fichier ``mongodb_vars.yml``
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Paramétrage de MongoDB :

* nom du *package*
* nom du service
* port d'écoute
* fréquence du check consul associé
* niveau de verbosité de mongoDB
* nom du réplicatset (fonction du paramètre d'inventaire ``mongo_shard_id``)
* *timeout* de connexion pour les applications clientes

Fichier ``repositories.yml``
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Permet, par un playbook ansible, de configurer les *repositories* à utiliser pour déployer vitam-ui. Renseigner les URL conformément à votre infrastructure.

Fichier ``reverse_proxy_conf.yml``
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Permet de choisir le *reverse proxy* frontal. Par défaut, apache.

Les valeurs acceptées sont :
* apache
* nginx

Fichier ``vault_consul.yml``
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. warning:: Pour le moment, ce fichier n'est pas encrypté par ``ansible-vault``

Permet de gérer l'encryption des messages consul.

Fichier ``vault_mongo_express.yml``
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Permet de gérer une *basic auth* au niveau de l'accès à mongo-express. Laisser vide n'active pas la *basic auth*.

Fichier ``vault_mongodb.yml``
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. warning:: Pour le moment, ce fichier n'est pas encrypté par ``ansible-vault``

gestion de la *passphrase* mongoDB.
Gestion des utilisteurs et de leurs droits associés

Fichier ``vault-keystores.yml``
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. note:: Fichier encrypté par ``ansible-vault``.

Se baser sur le fichier ``vault-keystore.yml.example``

Sont contenus les mots de passe des différents *stores* au format ``JKS`` gérés par la PKI.

Fichier ``vault-vitamui.yml``
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. warning:: Pour le moment, ce fichier n'est pas encrypté par ``ansible-vault``

Contient le ``nginx_cert_key_password``

Fichier ``vitam_vars.yml``
~~~~~~~~~~~~~~~~~~~~~~~~~~~

Fichier à paramétrer avec les informations de VITAM, en particulier les changements de ports par rapport à une installation "par défaut".

La section ``vitam_certs`` contient les informations des certificats pour connecter vitam-ui à vitam.
Les fichiers ``*.p12`` associés doivent être stockés dans ``environments/certs_vitam``.

Fichier ``vitamui_vars.yml``
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

"A la vitam", permet le paramétrage des différents composants de vitam-ui.

La section ``vitamui_platform_informations`` permet de définir une première entité, ainsi que quelques comptes "administrateur".
platform_name est utilisé pour définir / surcharger le nom de l'application qui sera récupéré sur les applications frontend et affiché par exemple dans le titre de la page.
Pour définir les couleurs de base du système visibles par défaut sur l'ensemble des interfaces utilisateurs, il faut surcharger cette structure  : 
``
  theme_colors:
    vitamui_primary: "#ff4a54"
    vitamui_secondary: "#241f63"
``

Surcharge
----------

Editer le fichier ``vitamui_extra_vars.yml`` pour surcharger les variables de ``group_vas/all`` si nécessaire.

Mise en place
==================

Playbook ``bootstrap.yml``
-------------------------------

Une fois le paramétrage des ``group_vars/all`` et éventuels *extra vars* effectué, il est possible de *bootstraper* les VM déclarées dans l'inventaire pour pointer sur les *repositories* de binaire de vitam-ui :

*Playbook* ::

   ansible-playbook -i <inventaire> bootstrap.yml --vault-password-file vault_pass.txt (-e extra_vars, si nécessaire)

PKI
---

Depuis le répertoire ``deployment``, lancer les scripts suivants ::

   ./pki/scripts/generate_ca.sh
   ./pki/certs/generate_certs.sh <inventaire>
   ./generate_stores.sh <inventaire>

Cette PKI, fournie à vocation de tests, permet de créer CA, certificats et *stores* conformément aux besoins de vitam-ui et en se basant sur les informations renseignées dans ``group_vars/all`` et l'inventaire.

Création des *host_vars*
--------------------------

Le script suivant permet de définir, pour les VM déclarées dans l'inventaire, des *host_vars*. Ces informations peuvent être mordiées ensuite.

*Playbook* ::

   ansible-playbook -i <inventaire> generate_hostvars_for_1_network_interface.yml --vault-password-file vault_pass.txt (-e extra_vars)

Création des repositories
-------------------------

ansible-playbook --become -i <inventaire> bootstrap.yml --vault-password-file vault_pass.txt [ --extra-vars=@./environments/vitamui_extra_vars.yml ]


Déploiement
=============

*Playbook* ::

   ansible-playbook -i <inventaire> vitamui.yml --vault-password-file vault_pass.txt [ --extra-vars=@./environments/vitamui_extra_vars.yml ]


Désinstallation
=================

*Playbook* ::

   ansible-playbook -i <inventaire> uninstall.yml --vault-password-file vault_pass.txt [ --extra-vars=@./environments/vitamui_extra_vars.yml ]
