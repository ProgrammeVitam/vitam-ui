#!/usr/bin/env bash

#
# Create rpm repository (used mainly for local deployment)
#
set -e
SCRIPT_DIR="$(readlink -f `dirname $0`)"
cd $SCRIPT_DIR
. ./_commons.sh

# ----
if [ -z $VITAMUI_DEPLOYEMENT_HOSTS ] ; then
    VITAMUI_DEPLOYEMENT_HOSTS=environment/hosts
fi

EXTRA_TAGS=""
# Filter test if tags are in the calling args:
args=("$@")
for ((i=0; i<"${#args[@]}"; ++i)); do
    case ${args[i]} in
        --tags)
            unset args[i];
            EXTRA_TAGS=",${args[i+1]}"
            unset args[i+1];
            break;;
    esac
done

ansible-playbook -i $VITAMUI_DEPLOYEMENT_HOSTS playbooks/vitamui.yml --tags "fstemplate${EXTRA_TAGS}" -e fstemplate_push_mode=not -e vitamui_template_debug=true "${args[@]}"
