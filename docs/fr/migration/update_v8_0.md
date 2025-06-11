# Mise à jour mineure / bugfix V8.0.x

## Adaptation des sources de déploiement ansible

### Mise à jour des certificats

> Cette opération doit être effectuée en cas de mise à jour mineure depuis une version v8.0.0 vers une version v8.0.1+ (v8.0.1 ou supérieure).
> Cette opération doit être effectuée avec les sources de déploiement de la V8.0.1+.

Il est fortement recommandé de re-générer la PKI pour corriger le nom de domaine des certificats (Common Name / Subject Alternative Name) des services VitamUI afin qu'ils soient conformes au domaine consul associé.

En particulier, si les certificats sont générés via une PKI de production, il faudra veiller à bien les préfixer par `vitamui-` (exemple `vitamui-iam-external.service.consul` et `vitamui-iam-external.service.site1.consul`, au lieu de `iam-external.service.consul` et `iam-external.service.site1.consul`)

Si vous utilisez la PKI d'exemple de VitamUI, vous pouvez procéder à la re-génération des nouveaux certificats comme suit :

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

* Re-générer les stores de VitamUI

  ```sh
  ./generate_stores.sh true
  ```

  > Le paramètre true permet d'écraser les stores existants.

> **Important :** La mise à jour des certificats est fortement recommandée. Dans le cas où vous ne souhaitez pas la réaliser, il faudra alors désactiver les contrôles de certificats via la clé `vitamui_defaults.services.ssl_hostname_verification`.

### Configuration des jetons de communication interne CAS

> Cette opération doit être effectuée en cas de mise à jour majeure depuis une version v8.0.0 vers une version v8.0.1+ (version 8.0.1 ou supérieure).

Une nouvelle clé `vitamui.cas_server.secret_token` doit être éditée dans le fichier de configuration `environments/group_vars/all/vault-vitamui.yml`. Elle permet de sécuriser les appels d'API entre le CAS Server et IAM.

```sh
ansible-vault edit --vault-password-file vault_pass.txt environments/group_vars/all/vault-vitamui.yml
```

> Attention, la clé devrait être configurée avec une clé alphanumérique longue et sans caractères spéciaux.

---

## Procédures à exécuter AVANT la montée de version

### Mise à jour des dépôts (YUM/APT)

Afin de pouvoir déployer la nouvelle version, vous devez mettre à jour la variable ``vitam_repositories`` sous ``environments/group_vars/all/repositories.yml`` afin de renseigner les dépôts à la version cible.

Puis exécutez le playbook suivant :

```sh
ansible-playbook -i environments/<inventaire> ansible-vitamui-extra/bootstrap.yml --ask-vault-pass
```

### Réinitialisation des certificats en bdd

En cas de re-génération des certificats, ils doivent être mis à jour en base de données.

```sh
ansible-playbook -i environments/<inventaire> ansible-vitamui-exploitation/reinit_security_certificates.yml --ask-vault-pass
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
