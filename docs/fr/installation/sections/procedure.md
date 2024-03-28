# Procédure de déploiement de VitamUI

> **Astuce**
> Nous vous recommandons d'utiliser Git pour le suivi de vos sources de déploiement.
> Ainsi, après chacune des étapes de cette procédure, n'oubliez pas de commiter vos changements.
>
> Cela vous aidera à suivre les modifications apportées à vos sources de déploiement.

## Récupération des sources de déploiement

Les sources de déploiement de Vitam-UI sont disponibles sous Github à l'adresse suivante: <https://github.com/ProgrammeVitam/vitam-ui/tree/develop/deployment>

~~~sh
git clone https://github.com/ProgrammeVitam/vitam-ui.git
~~~

## Préparation des sources de déploiement

Sous le répertoire `deployment/`.

### Préparation du fichier d'inventaire

Vous pouvez utiliser l'inventaire d'exemple [environments/hosts-ui.example](https://github.com/ProgrammeVitam/vitam-ui/blob/develop/deployment/environments/hosts-ui.example) pour préparer votre inventaire de déploiement de Vitam-UI.

Il faudra renseigner pour chacun des groupes de service Vitam-UI les VMs associés.

> **Informations complémentaires**
>
> * Suivez les balises `# EDIT`, elles précisent si les groupes sont Mandatory ou Optionnal.
> * Si vous utilisez le `reverse: nginx`, vous pouvez multi-instancier les composants `[zone_vitamui_ui]` et `[zone_vitamui_admin]`.
> * Si vous configurez un cluster consul dédié à la zone Vitam-UI (groupe `[hosts_vitamui_consul_server]`), vous devrez renseigner la valeur `vitamui_site_name` différente de `vitam_site_name`.
> * `vitamui_reverse_external_dns` doit être défini pour la configuration du reverse proxy d'accès aux composants UI. Cette valeur défini l'url d'accès à la plateforme.

---

### Customisation des paramètres

Les paramétres standards se trouvent dans `environments/group_vars/all/`. Ils sont valorisés avec des valeurs par défaut.

Les paramètres essentiels à renseigner pour le déploiement de Vitam-UI sont externalisés dans le fichier [environments/vitamui_extra_vars.yml](https://github.com/ProgrammeVitam/vitam-ui/blob/develop/deployment/environments/vitamui_extra_vars.yml).

> Conseil: Vous pouvez surcharger d'autres variables dans ce même fichier afin de vous permettre d'avoir une centralisation des spécificités de votre installation.

#### consul_vars.yml

Paramètres de configuration de consul.

Si vous souhaitez démarrer un cluster consul dédié à la zone Vitam-UI, il vous faudra éditer le paramètre `consul_remotes_sites` pour permettre l'interconnexion avec le cluster Vitam.

#### infra.yml

Paramètres de configuration du serveur SMTP et SMS pour cas-server.

#### jvm_opts.yml

Fichier permettant de configurer les paramètres des JVMs.

Par défaut, la configuration mémoire est de `-Xms512m -Xmx512m`.

#### repositories.yml

Configuration des dépôts pour l'installation de Vitam-UI.

L'installation de Vitam-UI nécessite l'accès aux dépôts Vitam.

#### reverse_proxy_conf.yml

Paramètres de configuration du reverse proxy permettant l'accès aux services UI.

Choix possible entre apache ou nginx. Nginx permettant la multi-instanciation des composants UI.

> Info: Le reverse proxy est un composant indispensable à Vitam-UI pour adresser les services UI. Contrairement au reverse Vitam qui est un outil facilitant les accès aux outils de l'administrateur de la plateforme mais qui n'est pas nécessaire au bon fonctionnement de la solution.

#### vitam_vars.yml

Si vous avez édité les paramètres de Vitam (ports, vitam_tenants_usage_external, ...), il sera nécessaire de les reporter ici.

#### vitamui_vars.yml

Contient l'ensemble des paramètres des composants de Vitam-UI.

#### Extra: Configuration des profils de mots de passe

La configuration de la complexité des mots de passe de Vitam-UI est personnalisable.

[Voir la documentation associée](password_configurations.md)

---

### Préparation des secrets

Il est indispensable de modifier les mots de passe des vaults avant de les stocker sous Git.

De plus, il faudra modifier l'ensemble des mots de passe contenus dans chacun des fichiers afin de sécuriser votre installation (notamment dans le cadre d'un déploiement en Production). Les mots de passe par défaut dans l'ansiblerie étant communiqués publiquement sur GitHub.

> **Attention**: Ne commitez jamais ces fichiers dans votre dépôt Git !
>
> * vault_pass.txt
> * vault_pki.pass
> * environments/group_vars/all/vault*.yml.example : Ils ne sont pas chiffrés !

#### Script d'aide à la préparation des vaults

> **Information**: Ce script est fourni à titre d'extra pour aider à la préparation des vaults.
> L'utilisation de Git est fortement recommandé afin de pouvoir tracer les modifications mais aussi pouvoir les annuler en cas de nécessité.

La commande suivante va vous permettre de créer des vaults sécurisés avec des mots de passe forts à partir des fichiers d'exemple fournis.

Il nécessite l'installation de `perl` afin de pouvoir être exécuté.

Si à votre première exécution vous n'avez pas de fichiers vault_pass.txt ou vault_pki.pass, laissez le choix du password vide.

> **Attention**
>
> Ce script est fourni à titre d'aide à la préparation initiale, l'option `-i` va écraser les vaults existants à partir des fichiers `vault*.yml.example` !
> Si vous voulez être sûr qu'il ne va pas écraser vos vaults existant, n'oubliez pas de supprimer les fichiers .example de vos sources.

~~~sh
./scripts/manage_vaults_vitamui.pl -r YES -i YES
~~~

#### vault-vitamui.yml

Contient les mots de passe de la plateforme Vitam-UI.

Il est indispensable de personnaliser ces mots de passe pour votre installation.

~~~sh
ansible-vault edit --vault-password-file vault_pass.txt environments/group_vars/all/vault-vitamui.yml
~~~

> **Important**
>
> * La variable `consul_encrypt` doit être indentique à celle configurée sur Vitam.
> * MAJ des paramètres de connexion au serveur SMTP pour l'envoi des mails de cas-server.

#### vault-mongodb.yml

Contient les mots de passe des bases mongo-vitamui.

Il est indispensable de personnaliser ces mots de passe pour votre installation.

~~~sh
ansible-vault edit --vault-password-file vault_pass.txt environments/group_vars/all/vault-mongodb.yml
~~~

> Attention, les mots de passe ne doivent pas contenir de caractères spéciaux tel que `/`.

#### vault-keystores.yml

Contient les mots de passe des keystores.

Il est indispensable de personnaliser ces mots de passe pour votre installation.

~~~sh
ansible-vault edit --vault-password-file vault_pass.txt environments/group_vars/all/vault-keystores.yml
~~~

---

### Récupération des interfaces réseaux

Avant de procéder à cette étape le fichier d'inventaire (fichier hosts-ui.example) doit être préalablement préparé.

* Récupération pour 1 interface réseau

~~~sh
ansible-playbook --vault-password-file vault_pass.txt -i environments/<hostfile_vitamui> ansible-vitamui-extra/generate_hostvars_for_1_network_interface.yml
~~~

* Récupération pour 2 interfaces réseau

~~~sh
ansible-playbook --vault-password-file vault_pass.txt -i environments/<hostfile_vitamui> ansible-vitamui-extra/generate_hostvars_for_2_network_interface.yml
~~~

> Assurez-vous que les fichiers host_vars des instances Vitam ont bien étés rapatriés lors de l'exécution de ce playbook.
> N'oubliez pas de renseigner les `ip_wan` si nécessaire.
>
> Attention ! Dans le cas d'un déploiement avec 2 interfaces, il subsiste un bug avec consul et la résolution DNS de mongo dans Vitam-UI. Un ticket de support est en cours de résolution pour résoudre ce problème rapidement.

---

## Gestion des certificats

Les certificats générés sont utilisés dans les keystores et truststores pour la communication inter-services.

Le service cas-server autorise les redirections sur les services ui lorsque ces derniers ont des certificats renseignés en base de données.

Enfin, la communication entre Vitam-UI et Vitam est possible seulement si le certificat client de vitamui est généré et copié dans les stores de vitam. Cette opération est faite lors de l'étape de mutualisation ci après.

Avertissement: Les scripts de génération de PKI sont donnés à titre d'exemple. Il est déconseillé de les utiliser en production.

### Génération des CA

~~~sh
./pki/scripts/generate_ca.sh true
~~~

> Le paramètre true permet d'écraser les CA existantes.
>
> CA valables 10 ans.

### Génération des Certificats

~~~sh
./pki/scripts/generate_certs.sh environments/<hostfile_vitamui> true
~~~

> Le paramètre true permet d'écraser les certificats existants.
>
> Certificats valables 3 ans.

Visualiser la fin du script de génération pour comprendre quels services sont adressés par ces certificats.

#### Mise en place de certificats personnalisés pour reverse proxy

Si vous avez des certificats personnalisés pour le reverse proxy, vous pouvez remplacer ceux générés sous `environments/certs/server/hosts/<hostname>/reverse.{crt,key}` par ceux fournis par votre autorité de certification.

> Info: reverse.crt est la concatenation du certificat et de la CA.

Si votre `reverse.key` est sécurisé par un mot de passe, n'oubliez pas de renseigner le paramètre `nginx_cert_key_password` dans le fichier `environments/group_vars/all/vault-vitamui.yml`

~~~sh
ansible-vault edit --vault-password-file vault_pass.txt environments/group_vars/all/vault-vitamui.yml
~~~

### Mutualisation des PKIs entre Vitam & Vitam-UI

Afin de permettre à Vitam-UI de communiquer avec Vitam, il va falloir procéder à un échanges de certificats et des autorités de certifications.

Pour ce faire, il existe un script permettant de faciliter cet échange qui prend les paramètres suivants:

~~~sh
Usage: ./scripts/mutualize_certs_for_vitamui.sh -v <path_to_vitam_certs_dir> -u <path_to_vitamui_certs_dir> [-h]

Description: This script allows you to mutualize PKI between Vitam-UI & Vitam.

Parameters:
  -v <path_to_vitam_certs_dir>   : Path to Vitam certs directory.
  -u <path_to_vitamui_certs_dir> : Path to Vitam-UI certs directory.
  -h : Show the usage.
~~~

Par exemple:

~~~sh
./scripts/mutualize_certs_for_vitamui.sh -v ../../vitam.git/deployment/environments/certs -u ./environments/certs
~~~

À l'issue de l'exécution de ce script, vous devez avoir:

* Côté Vitam:
  * environments/certs/client-external/ca/vitamui_ca-intermediate.crt
  * environments/certs/client-external/ca/vitamui_ca-root.crt

  * environments/certs/client-external/clients/external/vitamui.crt

* Côté Vitam-UI:
  * environments/certs/client-vitam/ca/vitam_ca-intermediate.crt
  * environments/certs/client-vitam/ca/vitam_ca-root.crt

> Attention ! Après cette étape, il sera nécessaire de regénérer les stores de la zone Vitam, suite à l'ajout des certificats de Vitam-UI, et de reconfigurer Vitam en utilisant le `--tags update_vitam_certificates`.
> Voir Le chapitre : [Reconfiguration de Vitam](#reconfiguration-de-vitam)

### Génération des stores (dans Vitam-UI)

~~~sh
./generate_stores.sh true
~~~

> Le paramètre true permet d'écraser les stores existants.

---

## Reconfiguration de Vitam

> Les étapes suivantes se font à partir des sources de déploiement de Vitam.

### Génération des stores suite à la mutualisation avec Vitam-UI (dans Vitam)

Se placer dans le répertoire `deployment/` des sources Vitam et exécuter la commande suivante:

~~~sh
./generate_stores.sh
~~~

### MAJ des certificats (dans Vitam)

~~~sh
ansible-playbook --vault-password-file vault_pass.txt -i environments/<hostfile_vitam> ansible-vitam/vitam.yml --tags update_vitam_certificates
~~~

### Ajout du contexte Vitam-UI

Toujours à partir des sources de déploiement de Vitam, il est nécessaire d'ajouter un contexte dédié à Vitam-UI pour la communication avec Vitam.

Ainsi, dans les sources de déploiement de Vitam, il sera nécessaire de rajouter dans le fichier `deployment/environments/group_vars/all/postinstall_param.yml`

~~~sh
vitam_additional_securityprofiles:
  - name: vitamui-security-profile
    identifier: vitamui-security-profile
    hasFullAccess: true
    permissions: "null"
    contexts:
      - name: vitamui-context
        identifier: vitamui-context
        status: ACTIVE
        enable_control: false
        # No control, idc about permissions :)
        permissions: "[ { \"tenant\": 0, \"AccessContracts\": [], \"IngestContracts\": [] },
                        { \"tenant\": 1, \"AccessContracts\": [], \"IngestContracts\": [] }]"
        certificates: ['external/vitamui.crt']
~~~

Le certificat client `vitamui.crt` doit être présent au niveau du répertoire `environments/certs/client-external/clients/external/` dans le dossier de déploiement de l'installation Vitam.

Puis lancer la commande suivante pour ajouter ce nouveau contexte :

~~~sh
ansible-playbook --vault-password-file vault_pass.txt -i environments/<hostfile_vitam> ansible-vitam-exploitation/add_contexts.yml
~~~

Ce playbook prend en paramétre le contenu du fichier `postinstall_param.yml`. Il réalise la création du security-profile, du contexte et l'ajout en base de données du certificat.

Attention, à l'heure actuelle il n'y a pas d'API permettant de faire un "update" du context, du security-profile, et du certificat.

Pour réaliser un "update" il faut donc faire d'abord une suppression de contexte (playbook `ansible-vitam-exploitation/remove_contexts.yml`) puis de nouveau un ajout de contexte. La suppression de contexte via le playbook `remove_contexts.yml` va prendre comme paramètre les éléments du fichier `postinstall_param.yml`.

---

## Installation de Vitam-UI

### Configuration des dépôts Vitam-UI

Lors du processus d'installation par l'ansiblerie on va chercher durant les différentes phases les packages applicatifs. Ces derniers doivent être renseignés sur chaque machine cible (hosts). Il est donc nécessaire de lancer un playbook de bootstrap qui va mettre à jour les adresses des repositories sur les machines distances.

Pour une distribution Unix de type Centos, on trouvera sur les différentes machines cibles : `/etc/yum.repos.d/vitamui-repositories.repo`

Pour une distribution Unix de type Debian: `/etc/apt/sources.list.d/vitamui-repositories.list`

~~~sh
ansible-playbook --vault-password-file vault_pass.txt -i environments/<hostfile_vitamui> --extra-vars=@./environments/vitamui_extra_vars.yml ansible-vitamui-extra/bootstrap.yml
~~~

> Le playbook bootstrap.yml se base sur le contenu du fichier `environments/group_vars/all/repositories.yml`.

### Lancement du déploiement de Vitam-UI

Le déploiement de Vitam-UI s'effectue à l'aide du playbook `ansible-vitamui/vitamui.yml`.

~~~sh
ansible-playbook --vault-password-file vault_pass.txt -i environments/<hostfile_vitamui> --extra-vars=@./environments/vitamui_extra_vars.yml ansible-vitamui/vitamui.yml
~~~

Ce playbook va déployer l'ensemble du coeur Vitam-UI et les applications associées (Referential, Ingest & Archive-search).

---

## Vérification de l'installation

Afin de valider que Vitam-UI est fonctionnel il faut réaliser quelques contrôles.

### 1°) Contrôle des services

Se rendre sur le portail Consul pour vérifier l'état des services.

Si vous avez déployé un cluster Consul dédié à Vitam-UI, vous devriez avoir un onglet (en haut à gauche), nommé avec le nom de `vitamui_site_name`. Sélectionnez cette valeur pour n'afficher que les services Vitam-UI et vérifiez que ces services soient verts (up).

Dans le cas où vous avez utilisez le cluster Consul de Vitam pour les services de Vitam-UI, les services devraient directement apparaître dans la liste des services.

### 2°) Contrôle de la communication vitamui-vitam

Vitam-UI communique avec Vitam via des requêtes api réalisées dans ses services. On peut via les outils de debug des navigateurs internet (chrome, firefox) voir quels sont les appels réalisés.

Plus facilement, on peut se rendre dans l'application referential et voir les contextes Vitam depuis Vitam-UI. Si les contextes sont bien visibles c'est que la communication Vitam-UI -> Vitam fonctionne.

Si ce n'est pas le cas, le mode debug du navigateur va nous permettre de voir ce qui ne va pas. Dans ce cas là aller en [Annexes](annexes.md) pour de plus amples informations sur les erreurs possiblement rencontrées.
