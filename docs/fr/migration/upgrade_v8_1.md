# Procédure de Montée de version VitamUI V8.1

> Attention: Veuillez appliquer les procédures spécifiques à chacune des versions précédentes en fonction de la version de départ selon la suite suivante: V7.1 -> V8.0 -> V8.1.

## Adaptation des sources de déploiement ansible

### Fusion des couches external & internal de VitamUI

Dans le fichier d'inventaire, les groupes suivants ont étés fusionnés:

* hosts_vitamui_iam_external & hosts_vitamui_iam_internal -> hosts_vitamui_iam
* hosts_vitamui_ingest_external & hosts_vitamui_ingest_internal -> hosts_vitamui_ingest
* hosts_vitamui_archive_search_external & hosts_vitamui_archive_search_internal -> hosts_vitamui_archive_search
* hosts_vitamui_collect_external & hosts_vitamui_collect_internal -> hosts_vitamui_collect
* hosts_vitamui_referential_external & hosts_vitamui_referential_internal -> hosts_vitamui_referential

Ainsi, vous pouvez devrez renommer aussi de votre côté les noms des groupes afin de n'en conserver qu'un seul avec le nouveau nom tel qu'attendu.

Au niveau des fichiers de `deployment/environments/group_vars/all/vitamui_vars.yml`

Si vous avez été amené à modifier les paramètres par défaut définis pour chacun des sous composants de vitamui:*, il faudra reporter les modifications selon la nouvelle convention de nommage.

En effet, les composants UI sont tous préfixés vitamui.ui_xxx

Les composants fusionnés ont perdu la référence à external/internal et par défaut, les paramètres de externals ont étés transférés sur la couche fusionnée.

Ainsi, les ports des couches internals ne sont plus utilisés.

vitamui.xxx_internal -> supprimé
vitamui.xxx_external -> modifié en vitamui.xxx

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

### Nettoyage des anciens composants avant migration

Exécuter le playbook de migration suivant à l'aide de l'ancien fichier d'inventaire.

Ce playbook va venir supprimer tous les anciens paquets installés pour permettre le déploiement des nouveaux.

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
