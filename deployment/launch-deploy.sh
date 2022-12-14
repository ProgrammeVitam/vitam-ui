#!/usr/bin/env bash

ansible-playbook ansible-vitamui-extra/bootstrap.yml -i environments/hosts-ui-vagrant --vault-password-file vault_pass.txt

./pki/scripts/generate_ca.sh


./pki/scripts/generate_certs.sh environments/hosts-ui-vagrant 


ansible-playbook ansible-vitamui-extra/generate_hostvars_for_1_network_interface.yml -i environments/hosts-ui-vagrant --vault-password-file vault_pass.txt 


./generate_stores.sh environments/hosts-ui-vagrant


ansible-playbook ansible-vitamui/vitamui.yml -i environments/hosts-ui-vagrant --vault-password-file vault_pass.txt 

