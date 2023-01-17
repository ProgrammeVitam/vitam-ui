# Mise à jour V6rc

## Montée de version mongodb de 4.2 (et moins) vers 5.0.x

Avant de procéder à l'application de la montée de version vitamui, il est indispensable d'effectuer la montée de version des binaires mongodb.

On procéde en 2 temps:
1) montée de version vers 4.4.x
2) montée de version en 5.0.x

Récupérer les sources de déploiement de vitamui 6.rc. Renseigner le fichier repository.yml dans environment/group_vars/all/ avec les bons éléments
permettant d'avoir les binaires de vitamui 6.rc et également ceux de vitam 6.rc.

Les binaires 4.4.x et les binaires 5.0.x mongodb se trouvent dans vitam-external. La migration vitamui mongodb va s'en servir.

### 1) Montée de version mongodb en 4.4.x

Lancer le playbook suivant :

~~~sh
ansible-playbook --vault-password-file vault_pass.txt -i environments/<hostfile_vitamui> --extra-vars=@./environments/vitamui_extra_vars.yml ansible-vitamui-migration/migration_mongodb_44.yml
~~~

Ce playbook change le paramètre setFeatureCompatibility en 4.2 , puis il effectue l'upgrade et l'installation des binaires mongodb 4.4.x.

### 2) Montée de version mongodb en 5.0.x

Lancer le playbook suivant :

~~~sh
ansible-playbook --vault-password-file vault_pass.txt -i environments/<hostfile_vitamui> --extra-vars=@./environments/vitamui_extra_vars.yml ansible-vitamui-migration/migration_mongodb_50.yml
~~~

Ce playbook change le paramètre setFeatureCompatibility en 4.4.x, puis il effectue l'upgrade et l'installation des binaires mongodb 5.0.x.

Dans les 2 étapes précédentes nous effectuons seulement la montée de version d'une instance mongod utilisée par vitamui. Dans le cadre d'un cluster
il faudra privillégier les opérations de montée de version mongodb utilisées dans vitam.
