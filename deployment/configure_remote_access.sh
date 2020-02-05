#!/usr/bin/env bash

#
# Create rpm repository (used mainly for local deployment)
#
set -e
SCRIPT_DIR="$(readlink -f `dirname $0`)"
cd $SCRIPT_DIR

# ----
if [ -z $VITAMUI_DEPLOYEMENT_HOSTS ] ; then
    VITAMUI_DEPLOYEMENT_HOSTS=environment/hosts.configure_remote_access
fi

ansible-playbook -i $VITAMUI_DEPLOYEMENT_HOSTS playbooks/configure_remote_access.yml "$@"
