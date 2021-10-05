# Procédure de déploiement de VitamUI

## Récupération des sources de déploiement

Les sources de déploiement de Vitam-UI sont disponibles sous github à l'adresse suivante:
 <https://github.com/ProgrammeVitam/vitam-ui/tree/develop/deployment>

Vous pouvez cloner directement la branche master_4.0.x pour obtenir les derniers éléments de la release R16 de Vitam-UI.

~~~
git clone https://github.com/ProgrammeVitam/vitam-ui.git --branch master_4.0.x
~~~

## Préparation des sources de déploiement

### Préparation des variables utilisés par l'ansiblerie

Se rendre dans deployment/environments/group_vars/all/ et balayer le contenu des fichiers présents. 
Remplacer les variables valorisées avec "changeme" par la valeur appropriée pour votre installation.

Fichiers à prendre en compte:

  deployment/environments/group_vars/all/repositories.yml

  Changer le hash de commit ou les liens selon votre repository local ou distant:

~~~console
vitam_repositories:
- key: vitam-java
  value: "http://pic-prod-repository.vitam-env/commit/VITAM_COMMIT/rpm/vitam-core/"
  proxy: _none_
- key: vitam-doc
  value: "http://pic-prod-repository.vitam-env/commit/VITAM_COMMIT/rpm/vitam-extras/"
  proxy: _none_
- key: vitam-external
  value: "http://pic-prod-repository.vitam-env/commit/VITAM_COMMIT/rpm/vitam-external/"
  proxy: _none_
- key: vitam-product
  value: "http://pic-prod-repository.vitam-env/commit/VITAM_COMMIT/rpm/vitam-product/"
  proxy: _none_
- key: vitam-griffins
  value: "http://pic-prod-repository.vitam-env/griffins/VITAM_GRIFFINS/rpm/"
  proxy: _none_
- key: vitamui
  value: "http://pic-prod-repository.vitam-env/contrib/VITAMUI_COMMIT/rpm/"
  proxy: _none_
~~~

deployment/environments/group_vars/all/vitamui_vars.yml

```console
 # site name utilisé par l'url de connexion et le nom de rattachement consul
vitamui_site_name: "monsite-ui"
# site name utilisé par l'url vitam et le nom de rattachement consul vitam
vitam_site_name: "monsite" 

# Services Vitam nécessaires à l'intéraction avec VitamUI.
# (Ces services doivent être accessibles)
vitam_vars:
  functional_administration:
    port_admin: 18004
  access_external:
    host: "access-external.service.monsite.consul"
    port_service: 8444
  ingest_external:
    host: "ingest-external.service.monsite.consul"
    port_service: 8443

# Rattachement au consul de Vitam sous un autre onglet dans l'interface
# portail consul (onglet nommé par la variable name)
consul_remote_sites:
  - vitamui:
    name: "monsite-ui"
    wan: ["vitam-env-monsite-vm.domain-env"] 
    # addresse ip ou nom de machine du consul serveur Vitam

# Informations concernant le serveur smtp utilisé par le service
# Cas-Server pour l'envoi de mail pour les mots de passe perdus.
smtp:
  host: "monhost.smpt.fr"   
  port:  2525
  protocol: "smtps" 
  user: "do-not-answer"
  password: "passwordAChanger"
  test_smtp_connection: false
  auth: true  
  tls_enable: true 
  cas:
    sender: "do-not-answer@domain.fr"
    expiration: 1440 # temps d'expiration en minutes


# Variables utilisées pour réaliser un backup et un restore 
# de collections pour la base de données VitamUI
# Ces variables sont utilisées par les playbooks mongo_backup.yml 
# et mongo_restore.yml.
mongo_dump_folder: /backup/mongod/
mongo_backup_reinstall:
  - db: "iam"
    collections: ["applications","customers","externalParameters","groups","owners",
    "profiles", "sequences","subrogations","tenants","users","providers"]
  - db: "admin"
    collections: []
```

### Configuration des profils de mots de passe (Ansiblerie):

La configuration de la complexité des mots de passe est externalisé du serveur CAS, la configuration actuelle est basée sur des profils de configurations.

L'exploitant / l'installateur de VITAMUI, peut choisir le profil de configuration personnalisé par instance.

Pour répondre aux exigeances de la complexité des mots de passes de l'ANSSI (Agence Nationale de la Sécurité des Systèmes d'Informations), un profil dédié est configuré par défaut.
Ce profil est nommé `'anssi'`, l'exploitant peut le changer en choisissant le profil custom, qui garde les anciens comportements.

#### Configuration par défaut (Profil anssi):
```
# Password configuration
vitamui_password_configurations:
  anssiPolicyPattern: '(^(?=(?:.*[a-z]){2,})(?=(?:.*[A-Z]){2,})(?=(?:.*[\d]){2,})[A-Za-zÀ-ÿ0-9$@!%*#£?&=\-\/:;\(\)"\.,\?!''\[\]{}^\+\=_\\\|~<>`]{${password.length},}$)|(^(?=(.*[$@!%*#£?&=\-\/:;\(\)"\.,\?!''\[\]{}^\+\=_\\\|~<>`]){2,})(?=(?:.*[A-Z]){2,})(?=(?:.*[\d]){2,})[A-Za-zÀ-ÿ0-9$@!%*#£?&=\-\/:;\(\)"\.,\?!''\[\]{}^\+\=_\\\|~<>`]{${password.length},}$)|(^(?=(.*[$@!%*#£?&=\-\/:;\(\)"\.,\?!''\[\]{}^\+\=_\\\|~<>`]){2,})(?=(?:.*[a-z]){2,})(?=(?:.*[\d]){2,})[A-Za-zÀ-ÿ0-9$@!%*#£?&=\-\/:;\(\)"\.,\?!''\[\]{}^\+\=_\\\|~<>`]{${password.length},}$)|(^(?=(.*[$@!%*#£?&=\-\/:;\(\)"\.,\?!''\[\]{}^\+\=_\\\|~<>`]){2,})(?=(?:.*[a-z]){2,})(?=(?:.*[A-Z]){2,})[A-Za-zÀ-ÿ0-9$@!%*#£?&=\-\/:;\(\)"\.,\?!''\[\]{}^\+\=_\\\|~<>`]{${password.length},}$)'
  password:
    profile: "anssi" # default profile is anssi, for (Agence Nationale de la Sécurité des Systèmes d'Information), use custom profile otherwise
    length: 12
    max_old_password: 12
    check_occurrence: true
    occurrences_chars_number: 3
    constraints:
      defaults:
        fr:
          messages:
            - Avoir une taille d'au moins ${password.length} caractères
          special-chars:
            title: 'Contenir au moins 2 caractères issus de chaque catégorie, pour au moins 3 des catégories suivantes:'
            messages:
              - Minuscules (a-z)
              - Majuscules (A-Z)
              - Numériques (0-9)
              - Caractères spéciaux (!"#$%&£'()*+,-./:;<=>?@[]^_`{|}~)
        en:
          messages:
            - Have a size of at least ${password.length} characters
          special-chars:
            title: 'Contain at least 2 characters from each category, for at least 3 of the following categories:'
            messages:
              - Uppercases (a-z)
              - Lowercases (A-Z)
              - Digital (0-9)
              - Special Characters (!"#$%&£'()*+,-./:;<=>?@[]^_`{|}~)
        de:
          messages:
            - Mindestens ${password.length} Zeichen lang sein
          special-chars:
            title: 'Mindestens 2 Zeichen aus jeder Kategorie enthalten, für mindestens 3 der folgenden Kategorien:'
            messages:
              - Großbuchstaben (a-z)
              - Kleinbuchstaben (A-Z)
              - Digital (0-9)
              - Spezielle Charaktere (!"#$%&£'()*+,-./:;<=>?@[]^_`{|}~)
```

#### Configuration pour le profil personnalisé (profil custom):
```
# Password configuration
vitamui_password_configurations:
  customPolicyPattern: '^(?=.*[$@!%*#£?&=\-\/:;\(\)"\.,\?!''\[\]{}^\+\=_\\\|~<>`])(?=.*[a-z])(?=.*[A-Z])(?=.*[\d])[A-Za-zÀ-ÿ0-9$@!%*#£?&=\-\/:;\(\)"\.,\?!''\[\]{}^\+\=_\\\|~<>`]{${password.length},}$'
  password:
    profile: "custom"
    length: 8
    max_old_password: 3
    constraints:
      customs:
        fr:
          title: 'Pour des raisons de sécurité, votre mot de passe doit:'
          messages:
            - Au moins ${password.length} caractères
            - Des minuscules et des majuscules
            - Au moins un chiffre et un caractère spécial
            - Etre différent des ${password.max-old-password} derniers mots de passe
        en:
          title: 'For security reasons, your password must:'
          messages:
            - At least ${password.length} characters
            - Lowercase and uppercase
            - At least one number and one special character
            - Be different from the last ${password.max-old-password} passwords
        de:
          title: 'Aus Sicherheitsgründen muss Ihr Passwort:'
          messages:
            - Mindestens ${password.length} Zeichen
            - Klein- und Großbuchstaben
            - Mindestens eine Zahl und ein Sonderzeichen
            - Unterscheiden Sie sich von den letzten ${password.max-old-password} Passwörtern
```

##### Explication de la configuration:

###### Configuration communes:
- `vitamui_password_configurations` est le bloc racine, qui se situe dans le fichier `vitamui_vars.yml`
- `anssiPolicyPattern`: L'expression régulière pour le profil ANSSI.
- `customPolicyPattern`: L'expression régulière pour le profil personnalisé (custom).
> L'expression régulière qui sera utilisé, dépendra du profil choisi.
- `password`: Le préfixe de configuration qui sera chargé dans le fichier de configuration pricipale du serveur CAS.
- `profile`: Le nom du profil à utiliser (par défault `anssi`), ou bien `custom`, pour éviter des erreurs de configuration au chargement du serveur, La déclaration d'un nom de profil devrait etre cohérent avec le bloc de configuration adéquat (voir explicaion de ces blocs au dessous).
- `length`: La taille du mot de passe (par défaut 12 pour le profil anssi, 8 pour le profil personnalisé).
- `max_old_password`: Le nombre de mots de passe anciens à ne pas réutiliser (par défaut 12 pour le profil anssi, 3 pour le profil custom).
- `check_occurrence`: Le boolean permettant de vérifier la présence des occurrences du nom d'utilisateur dans le mot de passe (par défaut à `true` pour le profil anssi, `false` ou absent pour le profil custom).
- `occurrences_chars_number`: Le nombre de caractères issues du nom d'utilisateur tolérables à utiliser dans le mot de passe (par défaut à `3` pour le profil anssi, `0` ou absent pour le profil custom).
- `constraints`: bloc des différentes contraintes des mots de passe par profile. 

###### Configuration pour le profil ANSSI:
Le sous bloc `defaults` du bloc `constraints` concerne les configurations par défault par bloc de langue.

Ce bloc contient la liste des messages personnalisés, et les différentes contraintes en termes des caractères alphanumérique, spéciaux, miniscules, majiscules etc..

###### Configuration pour le profil personnalisé:
Le sous bloc `customs` du bloc `constraints` concerne les configurations du profil personnalisé par bloc de langue.

Ce bloc contient la liste des messages personnalisés, et les différentes contraintes en termes des caractères alphanumérique, spéciaux, miniscules, majiscules etc..

###### Les langues supportées par CAS à ce jour:
Les langues supportées par CAS sont: la langue Français, la langue Anglaise  et l'Allemande.

Il est fortement recommandé de définir les trois blocs des différentes langues, pour garder la cohérence avec les différentes interfaces du serveur d'authentification CAS.
### Configuration des profils de mots de passe (YAML):

La configuration finale transcrite dans les fichiers de configurations serveurs:

Profil ANSSI:
```yml
# Password configuration
password:
  profile: "anssi"
  length: 12
  max-old-password: 12
  check-occurrence: true
  occurrences-chars-number: 3
  constraints:
      defaults:
          fr:
              messages:
                  - Avoir une taille d'au moins ${password.length} caractères
              special-chars:
                  title: 'Contenir au moins 2 caractères issus de chaque catégorie, pour au moins 3 des catégories suivantes:'
                  messages:
                      - Minuscules (a-z)
                      - Majuscules (A-Z)
                      - Numériques (0-9)
                      - Caractères spéciaux (!"#$%&£'()*+,-./:;<=>?@[]^_`{|}~)
          en:
              messages:
                  - Have a size of at least ${password.length} characters
              special-chars:
                  title: 'Contain at least 2 characters from each category, for at least 3 of the following categories:'
                  messages:
                      - Uppercases (a-z)
                      - Lowercases (A-Z)
                      - Digital (0-9)
                      - Special Characters (!"#$%&£'()*+,-./:;<=>?@[]^_`{|}~)
          de:
              messages:
                  - Mindestens ${password.length} Zeichen lang sein
              special-chars:
                  title: 'Mindestens 2 Zeichen aus jeder Kategorie enthalten, für mindestens 3 der folgenden Kategorien:'
                  messages:
                      - Großbuchstaben (a-z)
                      - Kleinbuchstaben (A-Z)
                      - Digital (0-9)
                      - Spezielle Charaktere (!"#$%&£'()*+,-./:;<=>?@[]^_`{|}~)
```

Profil CUSTOM:

```yml
# Password configuration
password:
  profile: "custom"
  length: 8
  max-old-password: 3
  constraints:
      customs:
          fr:
              title: 'Pour des raisons de sécurité, votre mot de passe doit:'
              messages:
                  - Au moins ${password.length} caractères
                  - Des minuscules et des majuscules
                  - Au moins un chiffre et un caractère spécial (!"#$%&£'()*+,-./:;<=>?@[]^_`{|}~)
          en:
              title: 'For security reasons, your password must:'
              messages:
                  - At least ${password.length} characters
                  - Lowercase and uppercase
                  - At least one number and one special character (!"#$%&£'()*+,-./:;<=>?@[]^_`{|}~)
          de:
              title: 'Aus Sicherheitsgründen muss Ihr Passwort:'
              messages:
                  - Mindestens ${password.length} Zeichen
                  - Klein- und Großbuchstaben
                  - Mindestens eine Zahl und ein Sonderzeichen (!"#$%&£'()*+,-./:;<=>?@[]^_`{|}~)
```
> Note:
en cas de changement manuelle par l'administrateur système du nombre de mots passe anciens à utiliser, le changement devra se faire au niveau CAS et iam-internal.
> Le redémarrage de ces deux composants est nécessaire.

Voir le document d'exploitation qui contient différents exemples de configurations par profil.

>[!NOTE]
> L'authentification est transparente pour les utilisateurs qui possèdent déjà des comptes VITAMUI, jusqu'à expiration de leurs mots de passe.
> Et lors du changement de mots de passe suite à expiration de celui ci, ils verront les nouvelles contraintes exigées par la plateforme.

### Préparation du fichier d'inventaire

Vous pouvez utiliser cet exemple pour préparer l'inventaire de déploiement de Vitam-UI.

<https://github.com/ProgrammeVitam/vitam-ui/blob/master_4.0.x/deployment/environments/hosts.vitamui>

> Attention ! Pour l'instant, il n'est pas possible de multi-instancier les composants de Vitam-UI; ainsi, l'ensemble 
des composants doivent être instanciés une seule fois (mais possiblement sur plusieurs vms). Des tickets sont en cours
 pour résoudre ce problème rapidement.

Renseigner selon votre cible les variables ansible suivantes:

```console
# exemple pour un user ssh centos 
# sur les machines cibles de l'installation
ansible_ssh_user=centos 
ansible_become=true

# nom du site vitam distant
vitam_site_name=changeme

# url d'accès à VitamUI
vitam_reverse_external_dns=changeme 

# adresse du proxy si un proxy est présent sur l'infrastructure 
# cible pour sortir sur internet
http_proxy_environnement= 
```


Exemple de variables utilisées dans le fichier inventaire:

```
[hosts:vars]
dns_servers=["10.207.11.11","10.207.11.12"]
ansible_ssh_user=centos
ansible_become=true
consul_domain=consul
url_prefix="https://{{ vitamui_site_name }}.env.programmevitam.fr"

# Reverse configuration
vitam_reverse_external_dns="{{ vitamui_site_name }}.env.programmevitam.fr"
vitam_reverse_external_protocol=https
reverse_proxy_port=80
http_proxy_environnement="http://proxy-adress.domain-env:3128"

mongo_shard_id=0

[hosts_vitamui_mongod:vars]
mongo_cluster_name=mongo-vitamui

```

### Customisation des paramètres

Les paramétres standards se trouvent dans deployment/environments/group_vars/all/. Ils sont valorisés avec des valeurs par défaut.

Les paramètres essentiels à renseigner pour le déploiement de Vitam-UI sont externalisés dans le fichier `environments/vitamui_extra_vars.yml`. 

<https://github.com/ProgrammeVitam/vitam-ui/blob/master_4.0.x/deployment/environments/vitamui_extra_vars.yml>

> Attention ! Actuellement il y a un bug avec la configuration sms, `sms.enabled: false` posera un problème au 
lancement de cas-server. Mais vous pouvez supprimer le bloc renseignant ces éléments.


### Préparation des secrets

> Conseil: Il est fortement recommandé de modifier les mots de passe des vaults !

Éditer les fichiers suivants à l'aide de la commande
```console 
ansible-vault edit --vault-password-file vault-pass.txt
```

* MAJ de la variable `consul_encrypt` dans le fichier 
~~~console
deployment/environments/group_vars/all/vault_consul.yml
~~~
qui doit être indentique à la valeur de `consul_encrypt` de Vitam.

* MAJ des mots de passe mongodb dans le fichier 
~~~console
deployment/environments/group_vars/all/vault_mongodb.yml.
~~~
Attention, les mots de passe ne doivent pas contenir de caractères spéciaux tel que `/`.

* MAJ des mots de passe des keystores dans le fichier 
~~~console
deployment/environments/group_vars/all/vault-keystores.yml`
~~~

### Récupération des interfaces réseaux

Avant de procéder à cette étape le fichier d'inventaire (fichier host de vitamui) doit être préalablement préparé.

```console 
ansible-playbook -i environments/<hostfile environnement> 
generate_hostvars_for_1_network_interface.yml --vault-password-file vault_pass.txt
```

> Information: Actuellement, le rôle permettant de générer les interfaces pour 2 réseaux n'est pas fourni, vous
 pouvez récupérer celui de Vitam pour générer vos fichiers host_vars avec 2 interfaces.
> Attention ! Dans le cas d'un déploiement avec 2 interfaces, il subsiste un bug avec consul et la résolution DNS 
de mongo dans Vitam-UI. Un ticket de support est en cours de résolution pour résoudre ce problème rapidement.


## Gestion des certificats

Les certificats générés sont utilisés dans les keystores et truststores pour la communication inter-services.

Le service cas-server autorise les redirections sur les services ui lorsque ces derniers ont des certificats renseignés 
en base de données.

Enfin, la communication vitamui-vitam est possible seulement si le certificat client de vitamui est généré et copié 
dans les stores de vitam. Cette opération est faite lors de l'étape de mutualisation ci après.

Avertissement: Les scripts de génération de PKI sont donnés à titre d'exemple. Il est déconseillé de les utiliser 
en production.

### Clean des certificats exemples

Afin de générer toute la pki, supprimer le répertoire "/deployement/environments/certs/"

Dans "deployment" exécuter :
```console
rm -rf /environments/certs/"
```

### Génération des CA

```console
./pki/scripts/generate_ca.sh
```

> CA valables 10 ans.

### Génération des Certificats

```console
./pki/scripts/generate_certs.sh environments/<hostfile environnement>
```

> Certificats valables 3 ans.

Visualiser la fin du script de génération pour comprendre quels services sont adressés par ces certificats.

### Mutualisation des PKIs entre Vitam & Vitam-UI

Créer un fichier script mutualization_pki.sh et y mettre le contenu suivant:

```sh
#!/usr/bin/env bash

#
# Scripts de mutualisation des pki vitamui/vitam pour un environnement
#

set -e

## PARAMS à configurer
VITAMUI_CERTS_DIR="vitamui.git/deployment/environments/certs"
VITAM_CERTS_DIR="vitam.git/deployment/environments/certs"

echo "################## VitamUi -> Vitam ##################"
VITAM_CERTS_EXTERNAL_DIR="${VITAM_CERTS_DIR}/client-external"

echo "Importing VitamUi CA (allowing Vitam to identify VitamUi services)"
mkdir -p "${VITAM_CERTS_EXTERNAL_DIR}/ca"
for CA in $(ls ${VITAMUI_CERTS_DIR}/client-vitam/ca/ca*.crt); do
   cp -f "${CA}" "${VITAM_CERTS_EXTERNAL_DIR}/ca/vitamui_$(basename ${CA})"
done

echo "Importing VitamUi certs (allowing Vitam to link VitamUI's requests
 to its security context)"
mkdir -p "${VITAM_CERTS_EXTERNAL_DIR}/clients/external"
for CA in $(ls ${VITAMUI_CERTS_DIR}/client-vitam/clients/vitamui/*.crt); do
   cp -f "${CA}" "${VITAM_CERTS_EXTERNAL_DIR}/clients/external/$(basename ${CA})"
done
echo "######################################################"

echo "################## Vitam -> VitamUi ##################"
VITAMUI_CERTS_VITAM_DIR="${VITAMUI_CERTS_DIR}/client-vitam"

echo "Importing Vitam CA (allowing VitamUi to identify Vitam services)"
mkdir -p "${VITAMUI_CERTS_VITAM_DIR}/ca"
for CA in $(ls ${VITAM_CERTS_DIR}/server/ca/ca*.crt); do
   cp -f "${CA}" "${VITAMUI_CERTS_VITAM_DIR}/ca/vitam_$(basename ${CA})"
done
echo "######################################################"
```

Exécuter le script:

```console
./mutualization_pki.sh
```
Changer si nécessaire les paths de vitamui et vitam selon installation.

> Attention ! À l'issue de cette étape, il sera aussi nécessaire de regénérer les stores de la zone Vitam suite à l'ajout des certificats de Vitam-UI
et de reconfigurer Vitam en utilisant le `--tags update_vitam_certificates`.

​
### Génération des stores (dans Vitamui)

Dans le cas de keystores existants il faut rajouter "true" en paramètre à la commande suivante afin d'écraser les anciens keystores.

```console
./generate_stores.sh (true)
```

## Reconfiguration de Vitam

> Les étapes suivantes se font à partir des sources de déploiement de Vitam.

### Génération des stores suite à la mutualisation avec Vitam-UI (dans Vitam)

Se placer dans le répertoire "/deployement" des sources vitam et exécuter la commande suivante:

```console
./generate_stores.sh
```

### MAJ des certificats (dans Vitam)

```console
ansible-playbook -i environments/<hostfile environnement> --vault-password-file
vault-pass.txt ansible-vitam/vitam.yml --tags update_vitam_certificates
```

### Ajout du contexte Vitam-UI (Toujours dans le dossier déploiement de vitam)

Il est nécessaire d'ajouter un contexte dédié à Vitam-UI pour la communication avec Vitam.

Ainsi, dans les sources de déploiement de Vitam, il sera nécessaire de rajouter dans le fichier `deployment/environments/group_vars/all/postinstall_param.yml`

```console
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
```

Le certificat client vitamui.crt doit être présent au niveau du répertoire environments/certs/client-external/clients/external dans le dossier de déploiement
de l'installation Vitam.


Puis lancer la commande suivante pour ajouter ce nouveau contexte :

```console
ansible-playbook -i environments/<hostfile environnement> --vault-password-file
vault_pass.txt ansible-vitam-exploitation/add_contexts.yml
```

Ce playbook prend en paramétre de manière transparente ce qui a été définit dans le fichier postinstall_param.yml. Il réalise la création
du security-profile, du contexte, et l'ajout en base de données du certificat.

Attention, à l'heure actuelle il n'y a pas d'API permettant de faire un "update" du context, du security-profile, et du certificat. 
Pour réaliser un "update" il faut donc faire d'abord une suppression de contexte (playbook ansible-vitam-exploitation/remove_contexts.yml)
puis de nouveau un ajout de contexte. 
La suppression de contexte via le playbook remove_context.yml va prendre comme paramètre les éléments du fichier postinstall_param.yml.


## Configuration des dépôts Vitam-UI

Lors du processus d'installation par l'ansiblerie on va chercher durant les différentes phases les packages applicatifs. Ces derniers doivent
être renseignés sur chaque machine cible (hosts). Il est donc nécessaire de lancer un playbook de bootstrap qui va mettre à jour les addresses
des repositories sur les machines distances.

Pour une distribution Unix de type Centos, on trouvera sur les différentes machines cibles :
/etc/yum.repos.d/vitamui-repositories.repo

Pour une distribution Unix de type Debian:
/etc/apt/sources.list.d/vitamui-repositories.list

```console
ansible-playbook  -i environments/<hostfile environnement> --vault-password-file
vault_pass.txt --extra-vars=@./environments/vitamui_extra_vars.yml bootstrap.yml
```

## Lancement du déploiement de Vitam-UI

Le déploiement de Vitam-UI est découpé en plusieurs playbooks. 

* Core
```console
ansible-playbook -i environments/<hostfile environnement> --vault-password-file
vault_pass.txt --extra-vars=@./environments/vitamui_extra_vars.yml vitamui.yml`
```

* Referential
```console
ansible-playbook -i environments/<hostfile environnement> --vault-password-file
vault_pass.txt --extra-vars=@./environments/vitamui_extra_vars.yml vitamui_referential.yml
```

* Ingest
```console
ansible-playbook -i environments/<hostfile environnement> --vault-password-file
vault_pass.txt --extra-vars=@./environments/vitamui_extra_vars.yml vitamui_ingest.yml
```

* Archive-search
```console
ansible-playbook -i environments/<hostfile environnement> --vault-password-file
vault_pass.txt --extra-vars=@./environments/vitamui_extra_vars.yml vitamui_archive_search.yml
```

NB: L'installation du reverse est comprise dans le playbook 'core'. Le playbook core est nécessaire à l'installation des 
playbooks suivants. Il doit être exécuté en 1er. 
  
## Check d'installation

Afin de valider que Vitamui est fonctionnel il faut réaliser quelques contrôles.

### 1°) Contrôle des services
Se rendre dans la page des outils de l'environnement Vitam et cliquer sur "portail consul".

Dans la page consul cliquer en haut à gauche sur l'onglet nommé par le datacenter utilisé (cela correspond généralement 
au nom de l'environnement), et aller sur le 2ème choix. Cela affiche les services vitamui uniquement.

Vérifier que ces services soient verts (up).

### 2°) Contrôle de la communication vitamui-vitam
Vitamui communique avec Vitam via des requêtes api réalisées dans ses services. On peut via les outils de debug des 
navigateurs internet (chrome, firefox) voir quels sont les appels réalisés. 

Plus facilement, on peut se rendre dans l'application referential et voir les contextes vitam depuis vitamui. Si les
contextes sont bien visibles c'est que la communication vitamui-vitam fonctionne.

Si ce n'est pas le cas, le mode debug du navigateur va nous permettre de voir ce qui ne va pas. Dans ce cas là aller en 
"Annexe" pour de plus amples informations sur les erreurs possiblement rencontrées.
