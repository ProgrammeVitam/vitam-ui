Role Name
=========

Role allowing to deploy default assets for Vitamui apps.

Requirements
------------

None.

Role Variables
--------------

* `assets_path`: STRING - Specify the directory where assets will be installed.
* `assets_user`: STRING - Specify the owner of assets.
* `assets_group`: STRING - Specify the group linked to the previous owner of assets.
* `assets_default_files`: STRING - Custom assets override.

Dependencies
------------

None

Example Playbook
----------------

```yml
- hosts: vitamui
  roles:
    - { role: assets_copy }
```

License
-------

CeCILL-C

Author Information
------------------

Programme Vitam
