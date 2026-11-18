# Procédure de Montée de version VitamUI V10.0

> Attention: Veuillez appliquer les procédures spécifiques à chacune des versions précédentes en fonction de la version de départ selon la suite suivante: V7.1 -> V8.0 -> V8.1 -> V9.0 -> V9.1 -> V10.0.

## Adaptation des sources de déploiement ansible

### Vérifications de sécurité activées par défaut

À partir de la V10.0, plusieurs vérifications (clés SSH, certificats TLS) sont activées par défaut. Les points suivants sont à contrôler avant de déployer avec les sources de la V10.0.

#### Vérification des clés SSH des hôtes (`ansible.cfg`)

`host_key_checking` vaut désormais `True` dans `deployment/ansible.cfg` et l'inventaire d'exemple (`hosts-ui.example`) ne désactive plus `StrictHostKeyChecking`.

* Les clés des hôtes cibles doivent être présentes dans le `known_hosts` de l'utilisateur qui lance ansible sur le serveur de déploiement, par exemple :

  ```sh
  ssh-keyscan -H <hôte> >> ~/.ssh/known_hosts
  ```

  > Vérifier l'empreinte des clés collectées avant de les ajouter.

* Les inventaires qui positionnent déjà `ansible_ssh_common_args='-o StrictHostKeyChecking=no ...'` ne sont pas concernés.
* Pour conserver temporairement l'ancien comportement : `export ANSIBLE_HOST_KEY_CHECKING=False`.

#### Vérification des certificats des services amont (`vitamui_proxy_ssl_verify`)

La nouvelle variable `vitamui_proxy_ssl_verify` (`environments/group_vars/all/vitamui_vars.yml`) vaut `true` par défaut. Lorsque les services amont sont appelés en HTTPS, la chaîne de leur certificat est vérifiée avec les CA présentes dans `environments/certs/vitamui-services/ca/` :

| Composant | Service amont vérifié | Vérification du nom |
|-----------|-----------------------|---------------------|
| Frontends (nginx) | API gateway | Oui (`vitamui-api-gateway.service.<consul_domain>`) |
| Reverse proxy nginx | Serveur CAS | Oui (`vitamui-cas-server.service.<consul_domain>`) |
| Reverse proxy apache | Tous les backends HTTPS : serveur CAS et UIs en `secure: true` | Non (backends adressés par IP) |

* Les certificats de l'API gateway et du serveur CAS (et, avec le reverse apache, des UIs en `secure: true`) doivent être émis par une CA présente dans `environments/certs/vitamui-services/ca/`. C'est le cas des certificats générés par la PKI de VitamUI (`pki/scripts/generate_certs.sh`).
* Le certificat de l'API gateway doit contenir le nom `vitamui-api-gateway.service.<consul_domain>` et, avec le reverse proxy nginx, celui du serveur CAS le nom `vitamui-cas-server.service.<consul_domain>` (SAN). C'est le cas des certificats générés par la PKI de VitamUI.
* Le déploiement échoue si `environments/certs/vitamui-services/ca/` ne contient aucun fichier `*.crt`.
* Symptôme d'un certificat non reconnu : erreurs HTTP 502 sur l'appel concerné et erreur de vérification du certificat amont dans les logs nginx / apache.
* Pour conserver l'ancien comportement (certificats amont non vérifiés) : positionner `vitamui_proxy_ssl_verify: false`.

#### Registre de conteneurs (mode conteneur, beta)

Le registre n'est plus déclaré systématiquement dans `insecure-registries` de docker. Un registre servi en HTTP simple ou avec un certificat non reconnu par l'hôte nécessite `container_repository.tls_verify: false` (`environments/group_vars/all/repositories.yml`).

#### Vérification du serveur SMTP (SMTPS)

Dans `environments/group_vars/all/infra.yml` :

* `smtp.smtps.checkserveridentity` vaut désormais `true` : le nom du serveur SMTP doit correspondre à son certificat.
* `smtp.smtps.trust` n'est plus positionné à `"*"` par défaut : le certificat du serveur SMTP doit être reconnu par le truststore de la JVM.

Pour un serveur SMTPS dont le certificat n'est pas reconnu (ou dont le nom ne correspond pas), renseigner `smtp.smtps.trust` avec le nom du serveur et / ou `checkserveridentity: false` dans l'inventaire.

#### Navigateur de fichiers `/browse` (playbook extra, hors production)

Le rôle `browser` ne sert plus que les répertoires `data/` et `log/` de `/vitamui`. Les autres répertoires (`conf/`, `app/`, `script/`, `tmp/`…), qui contiennent des mots de passe, ainsi que les keystores et fichiers de clés, ne sont plus servis.

---

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
