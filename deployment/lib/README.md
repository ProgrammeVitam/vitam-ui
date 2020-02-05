##  INSTALLING CALLBACK LOGGER:

git clone https://github.com/octplane/ansible_stdout_compact_logger
rm -Rf .git/

##  CONFIGURING CALLBACK LOGGER:
add to ansible.cfg in [defaults] section:

callback_plugins            = ./lib/ansible_stdout_compact_logger/callbacks/
stdout_callback             = anstomlog




