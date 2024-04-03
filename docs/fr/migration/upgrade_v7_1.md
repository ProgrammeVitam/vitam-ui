# Procédure de Montée de version VitamUI V7.1

> Attention: Veuillez appliquer les procédures spécifiques à chacune des versions précédentes en fonction de la version de départ selon la suite suivante: V5 -> V6RC -> V6 -> V7.0.

## Adaptation des sources de déploiement ansible

### Nouvelle variable vitamui_reverse_external_dns

L'objectif est de différencier le nom du domaine VitamUI de celui de Vitam car ils peuvent être hébergés sur des reverses distincts.

Cette distinction permet notamment d'appliquer le rôle `merge_index` sur le groupe [reverse] afin de fournir les liens d'accès à mongo-express-mongo-vitamui et aux browsers si ils sont déployés.
> Attention, à ne pas déployer en production !

Il est maintenant indispensable de rajouter à votre fichier d'inventaire la variable `vitamui_reverse_external_dns` pointant vers le nom de domaine externe d'appel à VitamUI.

---

## Procédures à exécuter AVANT la montée de version

### Mise à jour des dépôts (YUM/APT)

> Cette opération doit être effectuée AVANT la montée de version

Afin de pouvoir déployer la nouvelle version, vous devez mettre à jour la variable ``vitam_repositories`` sous ``environments/group_vars/all/repositories.yml`` afin de renseigner les dépôts à la version cible.

Puis exécutez le playbook suivant :

```sh
ansible-playbook -i environments/<inventaire> ansible-vitamui-extra/bootstrap.yml --ask-vault-pass
```

### Montée de version vers mongo 6.0

> Attention: Cette montée de version doit être effectuée AVANT la montée de version V7.1 de VitamUI.
> Cette opération doit être effectuée après avoir mis à jour les dépôts Vitam en V7.1.
> Il est recommandé d'effectuer un backup de la base de données à l'aide de mongodump avant de poursuivre.

Exécutez le playbook suivant à partir de l'ansiblerie de la V7.1 :

```sh
ansible-playbook -i environments/<inventaire> ansible-vitamui-migration/migration_mongodb_60.yml --ask-vault-pass
```

### Montée de version vers mongo 7.0

> Attention: Cette montée de version doit être effectuée AVANT la montée de version V7.1 de VitamUI et après la montée de version de MongoDB 6.0 ci-dessus.
> Cette opération doit être effectuée après avoir mis à jour les dépôts Vitam en V7.1.

Exécutez le playbook suivant à partir de l'ansiblerie de la V7.1 :

```sh
ansible-playbook -i environments/<inventaire> ansible-vitamui-migration/migration_mongodb_70.yml --ask-vault-pass
```

Une fois les montées de version de MongoDB réalisées, la montée de version Vitam peut être réalisée.

### Arrêt complet de VitamUI

> Cette opération doit être effectuée AVANT la montée de version vers la V7.1.
> Cette opération doit être effectuée avec les sources de déploiement de l'ancienne version.

VitamUI doit être arrêté :

```sh
ansible-playbook -i environments/<inventaire> ansible-vitamui-exploitation/stop_vitamui.yml --ask-vault-pass
```

---

## Application de la montée de version

### Lancement du master playbook vitam

> Cette opération doit être effectuée avec les sources de déploiement de la V7.1.

```sh
ansible-playbook -i environments/<inventaire> ansible-vitamui/vitamui.yml --ask-vault-pass
```

---

## Procédures à exécuter APRÈS la montée de version

N/A
