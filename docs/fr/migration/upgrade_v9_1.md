# Procédure de Montée de version VitamUI V9.1

> Attention: Veuillez appliquer les procédures spécifiques à chacune des versions précédentes en fonction de la version de départ selon la suite suivante: V7.1 -> V8.0 -> V8.1 -> V9.0 -> V9.1.

## Adaptation des sources de déploiement ansible

### Séparation des certificats client / serveur – Appels inter-VitamUI

**Périmètre** : communications mTLS entre services VitamUI (ex. api-gateway → iam, portal → archive-search, etc.)

À partir de la V9.1, les certificats utilisés pour les appels inter-services VitamUI sont explicitement séparés :

*   **Certificat serveur** : présenté par un service VitamUI lorsqu’il reçoit une connexion.
*   **Certificat client** : présenté par un service VitamUI lorsqu’il appelle un autre service VitamUI.

Chaque service VitamUI possède donc :
*   Un keystore serveur.
*   Un keystore client.

Un truststore VitamUI interne est utilisé pour établir la confiance entre services.

**Nouvelle arborescence des dossiers :**
*   Les certificats serveurs sont désormais localisés dans `environments/certs/vitamui-services/server/`.
*   Les keystores serveurs sont désormais localisés dans `environments/keystores/vitamui-services/server/`.
*   Les certificats clients sont désormais localisés dans `environments/certs/vitamui-services/clients/`.
*   Les keystores clients sont désormais localisés dans `environments/keystores/vitamui-services/clients/`.

#### Mise à jour des certificats

À partir de la V9.1, la séparation des certificats serveur/client de VitamUI nécessite la regénération de la PKI.

* Générer la nouvelle autorité de certification `vitamui-services`

  ```sh
  ./pki/scripts/generate_ca.sh true
  ```

  > Le paramètre true permet d'écraser l'autorité de certification existante.

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

### Déploiement de la configuration Prometheus pour les métriques VitamUI

Si vous avez déployé Prometheus sur la zone Vitam et que vous souhaitez pouvoir utiliser les nouveaux dashboards associés, vous devrez renseigner la configuration suivante.

Ajouter au fichier d'inventaire le groupe `[hosts_prometheus]` ainsi que les machines associées correspondantes à l'inventaire de vitam.

```yml
################################################################################
# ZONE VITAM
################################################################################
[vitam:children]
hosts_prometheus

[hosts_prometheus]
# EDIT: Optional used for deploying scraping configurations for VitamUI Components
my-vitam-prometheus-server
```

Le déploiement de la configuration s'effectue à l'aide du playbook: `ansible-vitamui-extra/vitamui_extra.yml --tags prometheus`

---

## Procédures à exécuter AVANT la montée de version

### Mise à jour des dépôts (YUM/APT)

> Cette opération doit être effectuée AVANT la montée de version

Afin de pouvoir déployer la nouvelle version, vous devez mettre à jour la variable ``vitam_repositories`` sous ``environments/group_vars/all/repositories.yml`` afin de renseigner les dépôts à la version cible.

Puis exécutez le playbook suivant :

```sh
ansible-playbook -i environments/<inventaire> ansible-vitamui-extra/bootstrap.yml --ask-vault-pass
```

### Mise à jour de MongoDB 8.0.23

> **Attention**
> Cette opération doit être effectuée après avoir mis à jour les dépôts Vitam en V9.1.
> Cette opération est à effectuer si vous venez des versions de VitamUI suivantes: V8.1.2-, V9.0.0.
> Il est recommandé d'effectuer un backup de la base de données à l'aide de mongodump avant de poursuivre.

Exécutez le playbook suivant à partir de l'ansiblerie de la V9.1 :

```sh
ansible-playbook -i environments/<inventaire> ansible-vitamui-migration/migration_mongodb_80.yml --ask-vault-pass
```

### Arrêt complet de VitamUI

> Cette opération doit être effectuée AVANT la montée de version vers la V9.1.

VitamUI doit être arrêté :

```sh
ansible-playbook -i environments/<inventaire> ansible-vitamui-exploitation/stop_vitamui.yml --ask-vault-pass
```

---

## Application de la montée de version

### Lancement du master playbook VitamUI

> Cette opération doit être effectuée avec les sources de déploiement de la V9.1.

```sh
ansible-playbook -i environments/<inventaire> ansible-vitamui/vitamui.yml --ask-vault-pass
```

---

## Procédures à exécuter APRÈS la montée de version

N/A
