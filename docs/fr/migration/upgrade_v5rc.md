# Mise à jour V5rc

> **Important !**
> La mise à jour vers la v5rc s'opère à partir de la R16.

## Nouvelle base de donnée archivesearch

Avant de procéder à l'application de la montée de version, dans la préparation de vos sources de deploiement, il est indispensable de rajouter la configuration de cette nouvelle base dans le fichier `environments/group_vars/all/vault-mongodb.yml`.

```yaml
    archivesearch:
        db: archivesearch
        user: api-archive-search
        password: changeit_archivesearch
        roles: '[{ role: "readWrite", db: "archivesearch" }]'
```

N'oubliez pas d'éditer le password avec un mot de passe sécurisé pour votre installation.
