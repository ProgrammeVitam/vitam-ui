#!/usr/bin/env bash

#
#  Start the vitamui services
#
set -e
SCRIPT_DIR="$(readlink -f `dirname $0`)"
cd $SCRIPT_DIR
. ./_commons.sh

# ---
if [ -z $VITAMUI_DEPLOYEMENT_HOSTS ] ; then
    VITAMUI_DEPLOYEMENT_HOSTS=environment/hosts
fi

ansible-playbook -i $VITAMUI_DEPLOYEMENT_HOSTS playbooks/start_vitamui.yml "$@"
