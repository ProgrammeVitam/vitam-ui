Role Name
=========

Installs private vitamui repository for yum installer.

Requirements
------------

Role Variables
--------------

Url for the lab yum repository

    vitamui_repository_url

Name of the repository in yum.d:

    vitamui_repository_name

Dependencies
------------

None

Example Playbook
----------------

Add vitamui repository

    - hosts: servers
      roles:
         - tools/vitamui-repository

License
-------

Author Information
------------------
