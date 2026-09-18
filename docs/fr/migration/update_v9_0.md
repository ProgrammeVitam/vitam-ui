# Mise à jour mineure / bugfix V9.0.x

## Adaptation des sources de déploiement ansible

### Neutralisation du tri sur les champs de métadonnées analysés

Le tri côté serveur sur certains champs de métadonnées analysés, tel que `Unit.Title`, peut provoquer l'expiration des recherches sur les tenants volumineux (champ Elasticsearch de type texte/fielddata). Un mécanisme de blocage du tri par champ est désormais disponible, piloté par configuration Ansible.

Par défaut, le tri reste activé sur l'ensemble des champs : la variable `query_non_sortable_fields` n'étant déclarée dans aucun fichier de `group_vars`, aucune action n'est requise pour conserver ce comportement.

Pour le désactiver sur un ou plusieurs champs, par exemple en cas d'expiration de recherche constatée, une action est nécessaire : déclarer la variable globalement via `vitamui_defaults.services.query_non_sortable_fields` ou par composant via `vitamui.<composant>.query_non_sortable_fields` :

```yaml
vitamui_defaults:
  services:
    query_non_sortable_fields: { 'Unit': ['Title'] }
```

---

## Procédures à exécuter AVANT la montée de version

### Mise à jour des dépôts (YUM/APT)

Afin de pouvoir déployer la nouvelle version, vous devez mettre à jour la variable ``vitam_repositories`` sous ``environments/group_vars/all/repositories.yml`` afin de renseigner les dépôts à la version cible.

Puis exécutez le playbook suivant :

```sh
ansible-playbook -i environments/<inventaire> ansible-vitamui-extra/bootstrap.yml --ask-vault-pass
```

### Mise à jour de MongoDB vers la version 8.0.23

> **Attention**
> Cette opération doit être effectuée après avoir mis à jour les dépôts Vitam en V9.0.
> Cette opération est à effectuer si vous venez des versions de VitamUI suivante: V9.0.0.
> Il est recommandé d'effectuer un backup de la base de données à l'aide de mongodump avant de poursuivre.

Exécutez le playbook suivant à partir de l'ansiblerie de la V9.0 :

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
