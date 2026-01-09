# Mise à jour mineure / bugfix V8.1.x

## Adaptation des sources de déploiement ansible

N/A

---

## Procédures à exécuter AVANT la montée de version

### Mise à jour des dépôts (YUM/APT)

Afin de pouvoir déployer la nouvelle version, vous devez mettre à jour la variable ``vitam_repositories`` sous ``environments/group_vars/all/repositories.yml`` afin de renseigner les dépôts à la version cible.

Puis exécutez le playbook suivant :

```sh
ansible-playbook -i environments/<inventaire> ansible-vitamui-extra/bootstrap.yml --ask-vault-pass
```

### Mise à jour de MongoDB vers la version 8.0.17

> **Attention**
> Cette opération doit être effectuée après avoir mis à jour les dépôts Vitam en V8.1.
> Cette opération est à effectuer si vous venez des versions de VitamUI suivante: V8.1.2-.
> Il est recommandé d'effectuer un backup de la base de données à l'aide de mongodump avant de poursuivre.

Exécutez le playbook suivant à partir de l'ansiblerie de la V8.1 :

```sh
ansible-playbook -i environments/<inventaire> ansible-vitamui-migration/migration_mongodb_80.yml --ask-vault-pass
```

### Arrêt complet de VitamUI

```sh
ansible-playbook -i environments/<inventaire> ansible-vitamui-exploitation/stop_vitamui.yml --ask-vault-pass
```

---

## Application de la montée de version

### Lancement du master playbook vitamui

```sh
ansible-playbook -i environments/<inventaire> ansible-vitamui/vitamui.yml --ask-vault-pass
```

---

## Procédures à exécuter APRÈS la montée de version

N/A
