# Mise à jour V6

## Procédures à exécuter AVANT la montée de version

### Mise à jour des dépôts (YUM/APT)

> Cette opération doit être effectuée AVANT la montée de version.

Afin de pouvoir déployer la nouvelle version, vous devez mettre à jour la variable `vitam_repositories` afin de renseigner les dépôts à la version cible.

Puis exécutez le playbook suivant:

```sh
ansible-playbook --ask-vault-pass --extra-vars=@./environments/vitamui_extra_vars.yml -i environments/<hostfile_vitamui> ansible-vitamui-extra/bootstrap.yml
```

### Montée de version mineure de mongo 5.0.13 -> 5.0.14

> Cette opération doit être effectuée après avoir mis à jour les dépôts en V6.

Exécutez le playbook suivant à partir de l'ansiblerie de la V6:

```sh
ansible-playbook --ask-vault-pass --extra-vars=@./environments/vitamui_extra_vars.yml -i environments/<hostfile_vitamui> ansible-vitamui-migration/migration_mongodb_50.yml
```

---

## Application de la montée de version

### Lancement du master playbook vitamui

> **Important !**
> Sous Debian, si vous appliquez la montée de version depuis la V6.RC, vous devrez rajouter le paramètre ``-e force_vitamui_version=6.0`` aux commandes suivantes. Sinon les packages vitamui ne seront pas correctement mis à jour. En effet, Debian considère que 6.rc.X > 6.X.

```sh
ansible-playbook --ask-vault-pass --extra-vars=@./environments/vitamui_extra_vars.yml -i environments/<hostfile_vitamui> ansible-vitamui/vitamui.yml
```
