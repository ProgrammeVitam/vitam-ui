# Procédure de Montée de version VitamUI V10.0

> Attention: Veuillez appliquer les procédures spécifiques à chacune des versions précédentes en fonction de la version de départ selon la suite suivante: V7.1 -> V8.0 -> V8.1 -> V9.0 -> V9.1 -> V10.0.

## Adaptation des sources de déploiement ansible



## Procédures à exécuter AVANT la montée de version

### Mise à jour des dépôts (YUM/APT)

> Cette opération doit être effectuée AVANT la montée de version

Afin de pouvoir déployer la nouvelle version, vous devez mettre à jour la variable ``vitam_repositories`` sous ``environments/group_vars/all/repositories.yml`` afin de renseigner les dépôts à la version cible.

Puis exécutez le playbook suivant :

```sh
ansible-playbook -i environments/<inventaire> ansible-vitamui-extra/bootstrap.yml --ask-vault-pass
```


## Application de la montée de version

### Lancement du master playbook VitamUI

> Cette opération doit être effectuée avec les sources de déploiement de la V10.0.

```sh
ansible-playbook -i environments/<inventaire> ansible-vitamui/vitamui.yml --ask-vault-pass
```

---

## Procédures à exécuter APRÈS la montée de version

N/A
