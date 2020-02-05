#!/usr/bin/env bash

#
# Setup /ec/hosts file with localhost vitamui application name resolution
#
set -e
SCRIPT_DIR="$(readlink -f `dirname $0`)"
cd $SCRIPT_DIR
. ./_commons.sh

# ---
if [ -z $VITAMUI_DEPLOYEMENT_HOSTS ] ; then
    VITAMUI_DEPLOYEMENT_HOSTS=environment/hosts
fi

ansible-playbook -i $VITAMUI_DEPLOYEMENT_HOSTS playbooks/generate_etc_host_file.yml "$@"
