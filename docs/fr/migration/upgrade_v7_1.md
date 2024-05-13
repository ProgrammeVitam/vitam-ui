# Procédure de Montée de version VitamUI V7.1

> Attention: Veuillez appliquer les procédures spécifiques à chacune des versions précédentes en fonction de la version de départ selon la suite suivante: V5 -> V6RC -> V6 -> V7.0.

## Adaptation des sources de déploiement ansible

### Mise à jour du fichier d'inventaire

De nouveaux groupes et paramètres ont fait leur apparition dans le fichier d'inventaire.

Veuillez vous référer à l'inventaire de référence: `environments/hosts-ui.example`.

* Ajout du nouveau groupe `[hosts_vitamui_api_gateway]` au sein de la zone `[zone_vitamui_app:children]`: Obligatoire pour permettre de déployer ce nouveau composant.
* Ajout du nouveau groupe `[hosts_logstash]` au sein de la zone `[vitam:children]`: Obligatoire si le groupe `[hosts_vitamui_logstash]` n'est pas défini et que `syslog.name: filebeat` (par défaut).
* Ajout du nouveau groupe `[reverse]` au sein de la zone `[vitam:children]`: Optionnel, uniquement dans le cas d'un déploiement des extra VitamUI (non recommandé en production).
* Ajout de la variable `vitamui_reverse_external_dns`: Obligatoire pour définir l'url d'accès à VitamUI.

### Mise à jour des certificats

#### Recharting des applications frontend

Suite au recharting des webapps front, leurs ressources sont maintenant hébergées sur un serveur nginx.

Ainsi, il peut-être opportun de redispatcher les composants des groupes `[hosts_ui_*]` sur les même machines que celles du groupe `[hosts_vitamui_reverseproxy]` afin de mutualiser l'utilisation des ressources.

Si vous modifiez la répartition des services UI dans votre inventaire, vous devrez regénérer les certificats pour ces composants et ainsi supprimer les précédents via la commande suivante:

```sh
find environments/certs/server/hosts/ -name ui-* -type f -delete
```

#### Nouveau composant applicatif API Gateway

À partir de la V7.1, le nouveau composant applicatif `api-gateway` a été introduit et nécessite un certificat.

De plus, si vous avez modifié la répartition des services UI, ces commandes seront aussi nécessaires.

* Générer les nouveaux certificats

  ```sh
  ./pki/scripts/generate_certs.sh environments/<inventaire>
  ```

* Regénérer les stores

  ```sh
  ./generate_stores.sh true
  ```

### Nouvelle variable vitamui_reverse_external_dns

L'objectif est de différencier le nom du domaine VitamUI de celui de Vitam car ils peuvent être hébergés sur des reverses distincts.

Cette distinction permet notamment d'appliquer le rôle `merge_index` sur le groupe `[reverse]` afin de fournir les liens d'accès à mongo-express-mongo-vitamui et aux browsers si ils sont déployés.
> Attention, à ne pas déployer en production !

Il est maintenant indispensable de rajouter à votre fichier d'inventaire la variable `vitamui_reverse_external_dns` pointant vers le nom de domaine externe d'appel à VitamUI.

### Modification de la durée de rétention des logs par défaut

Par défaut on conserve maintenant 365j de logs (accesslogs & applicatif) dans une limite de 5GB (par composant). De plus, nous avons réduit la quantité de logs gc de `32*64m=2048m` à `8*32m=256m`.

Il est toujours possible de personnaliser ce paramétrage par défaut via les variables suivantes:

* Pour les gc:
  * `vitamui_defaults.jvm_opts.gc` ou par composant en utilisant la variable `vitamui.<composant>.jvm_opts.gc`.

* Pour les accesslogs:
  * `vitamui_defaults.services.access_retention_days: 365` ou par composant en utilisant la variable `vitamui.<composant>.access_retention_days: 365`.
  * `vitamui_defaults.services.access_total_size_cap: 5GB` ou par composant en utilisant la variable `vitamui.<composant>.access_total_size_cap: 5GB`.

* Pour les logs applicatifs:
  * `vitamui_default.services.log.logback_max_history: 365` ou par composant en utilisant la variable `vitamui.<composant>.log.logback_max_history: 365`.
  * `vitamui_default.services.log.logback_total_size_cap: 5GB` ou par composant en utilisant la variable `vitam.<composant>.log.logback_total_size_cap: 5GB`.

### Modification de la méthodologie de concentration des logs

Un nouveau composant applicatif (Filebeat) permettant de collecter les logs dans le cluster elasticsearch-log a été ajouté.

La méthode de collecte via rsyslog et syslog-ng sera donc dépréciée dans les futures releases.

Vous pouvez continuer à utiliser les précédentes méthodes de concentation de logs via la configuration du paramètre `syslog.name: filebeat` (rsyslog, syslog-ng).

### Nouveau mode de déploiement en container (beta)

> Attention, à ne pas utiliser en production.

Pour permettre le déploiement en mode conteneur de VitamUI, vous devez configurer les valeurs suivantes:

Dans le fichier de configuration des repositories `environments/group_vars/all/main/repositories.yml`

```yml
install_mode: container # Default to legacy

container_repository:
  registry_url:
  username:
  password:
```

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

### Migration de la gateway

> Cette opération doit être effectuée AVANT la montée de version vers la V7.1.
> Cette opération doit être effectuée avec les sources de déploiement de la V7.1 mais avec l'inventaire de l'ancienne version.

Executez le script de migration vers l'API Gateway pour supprimer les anciens services UI Java et mettre à jour les certificats en base de données pour VitamUI.

```sh
ansible-playbook -i environments/<inventaire-version-precedente> ansible-vitamui-migration/migration_gateway.yml --ask-vault-pass
```

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
