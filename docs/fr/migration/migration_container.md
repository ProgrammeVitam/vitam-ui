# Migration d'un vitamui legacy vers un vitamui conteneurisé

## Adaptation des sources de déploiement ansible

Il faut éditer le contenu du fichier `environments/group_vars/all/repositories.yml`. Pour cela il faut rajouter les paramètres présentés dans l'exemple:

```yml
install_mode: container

container_repository:
    registry_url: https://docker.programmevitam.fr/
    username: ''
    password: ''

vitamui_container_version: <vitamui_version>
```

> Attention: Dans le cas d'utilisation d'une registry interne il vous faudra effectuer une synchronisation à partir de la registry docker du Programme Vitam: https://docker.programmevitam.fr

## Procédures à exécuter AVANT la migration

### Arrêt complet de Vitamui

> Attention: Cette opération doit être effectuée AVANT la migration vers le mode conteneurisé.

Vitamui doit être arrêté :

```sh
ansible-playbook -i environments/<inventaire> ansible-vitamui-exploitation/stop_vitamui.yml --ask-vault-pass
```

## Application de la migration

> Attention: Il faut s'assurer que la variable `install_mode: container` est bien configurée.

```sh
ansible-playbook -i environments/<inventaire> ansible-vitamui-migration/remove_legacy_packages.yml --ask-vault-pass
```

### Lancement du master playbook vitamui

```sh
ansible-playbook -i environments/<inventaire> ansible-vitamui/vitamui.yml --ask-vault-pass
```

## Procédures à exécuter APRÈS la migration

N/A
