Role Name
=========

Role allowing to deploy default assets for XAM apps.

Requirements
------------

None.

Role Variables
--------------

### asset_path
STRING - Specify the directory where assets will be installed.

### asset_user
STRING - Specify the owner of assets.

### asset_group
STRING - Specify the group linked to the previous owner of assets.


Dependencies
------------

None

Example Playbook
----------------

```
- hosts: vitamui
  roles:
    - { role: assets-xam }
```

License
-------

BSD

Author Information
------------------

An optional section for the role authors to include contact information, or a website (HTML is not allowed).
