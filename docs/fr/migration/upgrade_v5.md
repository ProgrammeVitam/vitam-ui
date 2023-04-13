# Mise à jour V5

> **Important !**
> La mise à jour vers la V5 s'opère à partir de la V5rc ou de la R16.
> Si vous effectuez la montée de version à partir de la R16, veuillez appliquer les procédures décrites dans le chapitre: [Mise à jour V5rc](upgrade_v5rc.md)

---

## Application de la montée de version

### Lancement du master playbook vitamui

> **Important !**
> Sous Debian, si vous appliquez la montée de version depuis la V5.RC, vous devrez rajouter le paramètre ``-e force_vitamui_version=5.2`` aux commandes suivantes. Sinon les packages vitamui ne seront pas correctement mis à jour. En effet, Debian considère que 5.rc.X > 5.X.

```sh
ansible-playbook --ask-vault-pass --extra-vars=@./environments/vitamui_extra_vars.yml -i environments/<hostfile_vitamui> ansible-vitamui/vitamui.yml
```
