# Procédure de Montée de version VitamUI V8.1

> Attention: Veuillez appliquer les procédures spécifiques à chacune des versions précédentes en fonction de la version de départ selon la suite suivante: V7.1 -> V8.0 -> V8.1.

## Adaptation des sources de déploiement ansible

### Fusion des couches externals & internals de VitamUI

Évolution majeure de cette nouvelle release permettant de simplifier l'architecture applicative de VitamUI et aussi de réduire les ressources nécessaires à son fonctionnement.

Ainsi, vous pouvez en profiter pour revoir l'architecture associée à votre déploiement (cf. Chapitre Prérequis de la DIN VitamUI).

* Modification du fichier d'inventaire

  Les groupes suivants ont étés fusionnés:

  | Avant                                                                             | Après                        |
  |-----------------------------------------------------------------------------------|------------------------------|
  | hosts_vitamui_iam_external <br/> hosts_vitamui_iam_internal                       | hosts_vitamui_iam            |
  | hosts_vitamui_ingest_external <br/> hosts_vitamui_ingest_internal                 | hosts_vitamui_ingest         |
  | hosts_vitamui_archive_search_external <br/> hosts_vitamui_archive_search_internal | hosts_vitamui_archive_search |
  | hosts_vitamui_collect_external <br/> hosts_vitamui_collect_internal               | hosts_vitamui_collect        |
  | hosts_vitamui_referential_external <br/> hosts_vitamui_referential_internal       | hosts_vitamui_referential    |
  | hosts_vitamui_security_internal                                                   | hosts_vitamui_security       |
  | hosts_vitamui_pastis_external                                                     | hosts_vitamui_pastis         |

  Ainsi, vous devrez renommer/supprimer les noms des groupes afin de n'en conserver qu'un seul avec le nouveau nom tel qu'attendu.

  > PS: Si vous modifiez les machines associées à votre inventaire, n'oubliez pas de rejouer le playbook `ansible-vitamui-extra/generate_hostvars_for_1_network_interface.yml` ou `ansible-vitamui-extra/generate_hostvars_for_2_network_interfaces.yml` selon votre cas.

* Modification de la configuration des services

  Dans les fichiers de configuration suivants:

  * `deployment/environments/group_vars/all/vitamui_vars.yml`
  * `environments/group_vars/all/jvm_opts.yml`

  > La suite ne s'applique que si vous avez surchargez les valeurs par défaut des services `vitamui:*`. Autrement, vous pouvez reprendre intégralement les fichiers livrés dans cette release.

  Vous devrez supprimer les références aux anciens noms des services et/ou de reporter vos modifications selon la nouvelle convention de nommage.

  En effet, les composants UI sont maintenant tous préfixés `vitamui.ui_xxx`.

  Renommez les composants suivants:

  * `vitamui.identity` -> `vitamui.ui_identity`
  * `vitamui.identity_admin` -> `vitamui.ui_identity_admin`
  * `vitamui.referential` -> `vitamui.ui_referential`
  * `vitamui.portal` -> `vitamui.ui_portal`
  * `vitamui.ingest` -> `vitamui.ui_ingest`
  * `vitamui.archive_search` -> `vitamui.ui_archive_search`
  * `vitamui.collect` -> `vitamui.ui_collect`
  * `vitamui.pastis` -> `vitamui.ui_pastis`
  * `vitamui.design_system` -> `vitamui.ui_design_system`

  Les composants applicatifs ont perdu la notion de `-external` & `-internal` et, par défaut, les paramètres des externals ont étés transférés sur la couche fusionnée.

  Ainsi, les ports des couches internals des services fusionnés ne sont plus utilisés. Vous pouvez donc retirer les ports suivants: `7201, 7205, 7208, 7209, 7210, 8201, 8205, 8208, 8209, 8210`

  * `vitamui.xxx_internal` -> supprimé (exception pour `vitamui.security_internal` renommé en `vitamui.security`).
  * `vitamui.xxx_external` -> modifié en `vitamui.xxx`.

### Mise à jour des certificats

À partir de la V8.1, la fusion des couches externals & internals nécessite la regénération des certificats.

* Générer les nouveaux certificats

  ```sh
  ./pki/scripts/generate_certs.sh environments/<inventaire> true
  ```

  > Le paramètre true permet d'écraser les certificats existants.

* Mutualisation des PKIs entre Vitam & VitamUI

  Afin de permettre à VitamUI de communiquer avec Vitam, il va falloir procéder à un échanges de certificats et des autorités de certifications.

  Pour ce faire, il existe un script permettant de faciliter cet échange qui prend les paramètres suivants:

  ```sh
  ./scripts/mutualize_certs_for_vitamui.sh -v ../../vitam.git/deployment/environments/certs -u ./environments/certs
  ```

  > Attention ! Après cette étape, il sera nécessaire de regénérer les stores de la zone Vitam, suite à l'ajout des certificats de VitamUI, et de reconfigurer Vitam en utilisant le `--tags update_vitam_certificates`.

* Regénérer les stores de VitamUI

  ```sh
  ./generate_stores.sh true
  ```

  > Le paramètre true permet d'écraser les stores existants.

### Configuration des jetons de communication interne CAS

> Cette opération doit être effectuée en cas de mise à jour majeure depuis une version v8.0.0 vers une version v8.1.x.

Une nouvelle clé `vitamui.cas_server.secret_token` doit être éditée dans le fichier de configuration `environments/group_vars/all/vault-vitamui.yml`. Elle permet de sécuriser les appels d'API entre le CAS Server et IAM.

```sh
ansible-vault edit --vault-password-file vault_pass.txt environments/group_vars/all/vault-vitamui.yml
```

> Attention, la clé devrait être configurée avec une clé alphanumérique longue et sans caractères spéciaux.

---

## Procédures à exécuter AVANT la montée de version

### Mise à jour des dépôts (YUM/APT)

> Cette opération doit être effectuée AVANT la montée de version

Afin de pouvoir déployer la nouvelle version, vous devez mettre à jour la variable ``vitam_repositories`` sous ``environments/group_vars/all/repositories.yml`` afin de renseigner les dépôts à la version cible.

Puis exécutez le playbook suivant :

```sh
ansible-playbook -i environments/<inventaire> ansible-vitamui-extra/bootstrap.yml --ask-vault-pass
```

### Montée de version vers MongoDB 8.0

> Attention: Cette montée de version doit être effectuée AVANT la montée de version V8.1 de VitamUI.
> Cette opération doit être effectuée après avoir mis à jour les dépôts Vitam en V8.1.
> Il est recommandé d'effectuer un backup de la base de données à l'aide de mongodump avant de poursuivre.

Exécutez le playbook suivant à partir de l'ansiblerie de la V8.1 :

```sh
ansible-playbook -i environments/<inventaire> ansible-vitamui-migration/migration_mongodb_80.yml --ask-vault-pass
```

### Arrêt complet de VitamUI

> Cette opération doit être effectuée AVANT la montée de version vers la V8.1.
> Cette opération doit être effectuée avec les sources de déploiement de l'ancienne version.

VitamUI doit être arrêté :

```sh
ansible-playbook -i environments/<inventaire> ansible-vitamui-exploitation/stop_vitamui.yml --ask-vault-pass
```

### Nettoyage des anciens composants externals & internals fusionnés en une couche

Ce playbook va venir supprimer tous les anciens paquets externals & internals installés.

> Cette opération doit être effectuée AVANT la montée de version vers la V8.1.
> Cette opération doit être effectuée avec l'inventaire de l'ancienne version.

```sh
ansible-playbook -i environments/<inventaire-version-precedente> ansible-vitamui-migration/migration_vitamui_81.yml --ask-vault-pass
```

> Si vous souhaitez supprimer les répertoires et les données associées à ces anciens composants, vous pouvez ajouter le paramètre `-e delete_data=true` à la commande précédente.

---

## Application de la montée de version

### Lancement du master playbook vitam

> Cette opération doit être effectuée avec les sources de déploiement de la V8.1.

```sh
ansible-playbook -i environments/<inventaire> ansible-vitamui/vitamui.yml --ask-vault-pass
```

---

## Procédures à exécuter APRÈS la montée de version

N/A
