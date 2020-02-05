#!/usr/bin/env bash

ansible-playbook -i ../../../environment/hosts test.yml \
                --tags=config \
                -e consul_conf_dir=/tmp/ \
                -e vitamui_system_user=$(id -un)   \
                -e vitamui_system_group=$(id -gn)

