# Procédure de Montée de version VitamUI V8.0

> Attention: Veuillez appliquer les procédures spécifiques à chacune des versions précédentes en fonction de la version de départ selon la suite suivante: V6 -> V7.0 -> V7.1.

## Adaptation des sources de déploiement ansible

N/A

---

## Procédures à exécuter AVANT la montée de version

### Mise à jour des dépôts (YUM/APT)

> Cette opération doit être effectuée AVANT la montée de version

Afin de pouvoir déployer la nouvelle version, vous devez mettre à jour la variable ``vitam_repositories`` sous ``environments/group_vars/all/repositories.yml`` afin de renseigner les dépôts à la version cible.

Puis exécutez le playbook suivant :

```sh
ansible-playbook -i environments/<inventaire> ansible-vitamui-extra/bootstrap.yml --ask-vault-pass
```

### Arrêt complet de VitamUI

> Cette opération doit être effectuée AVANT la montée de version vers la V8.0.
> Cette opération doit être effectuée avec les sources de déploiement de l'ancienne version.

VitamUI doit être arrêté :

```sh
ansible-playbook -i environments/<inventaire> ansible-vitamui-exploitation/stop_vitamui.yml --ask-vault-pass
```

---

## Application de la montée de version

### Lancement du master playbook vitam

> Cette opération doit être effectuée avec les sources de déploiement de la V8.0.

```sh
ansible-playbook -i environments/<inventaire> ansible-vitamui/vitamui.yml --ask-vault-pass
```

---

## Procédures à exécuter APRÈS la montée de version

N/A
