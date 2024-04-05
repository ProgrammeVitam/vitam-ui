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

### Mise à jour des certificats VitamUI pour l'activation des composants avec l'API GW

> Cette opération doit être effectuée AVANT la montée de version vers la V7.1.
> Cette opération doit être effectuée avec les sources de déploiement de la V7.1.

Cette opération est nécessaire car à partir de la V7.1, un nouveau composant applicatif nécessite de nouveaux certificats.

* Générer les certificats de VitamUI

  ```sh
  ./pki/scripts/generate_certs.sh environments/<inventaire> true
  ```

* Mutualiser les certificats entre Vitam et Vitam-UI

  ```sh
  ./scripts/mutualize_certs_for_vitamui.sh -v <path_to_vitam_certs_dir> -u <path_to_vitamui_certs_dir>
  ```

* generate_stores pour Vitam and Vitam-UI

  > Une fois que toutes les étapes précédentes de génération et mutualisation des certificats ont été correctement effectués, vous pouvez générer les stores.

  ```sh
  # Avec les sources de déploiement Vitam
  ./generate_stores.sh
  # Avec les sources de déploiement de VitamUI
  ./generate_stores.sh true
  ```

* Mettre à jour les certificats de Vitam

  ```sh
  ansible-playbook -i environments/<inventaire> ansible-vitam/vitam.yml --tags update_vitam_certificates --ask-vault-pass
  ```

* Mettre à jour le contexte applicatif de VitamUI d'appel à Vitam

  ```sh
  ansible-playbook -i environments/<inventaire> ansible-vitam-exploitation/remove_contexts.yml -e security_profile_id=vitamui-security-profile --ask-vault-pass
  ansible-playbook -i environments/<inventaire> ansible-vitam-exploitation/add_contexts.yml -e security_profile_id=vitamui-security-profile --ask-vault-pass
  ```

### Migration de la gateway

> Cette opération doit être effectuée AVANT la montée de version vers la V7.1.
> Cette opération doit être effectuée avec les sources de déploiement de la V7.1 mais avec l'inventaire de l'ancienne version.

Executez le script de migration vers l'API Gateway pour supprimer les anciens services UI Java et mettre à jour les certificats en base de données pour VitamUI.

```sh
ansible-playbook -i environments/<inventaire-version-precedente> ansible-vitamui-migration/migration_gateway.yml --ask-vault-pass
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
