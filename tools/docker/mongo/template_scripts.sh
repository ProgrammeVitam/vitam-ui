#!/usr/bin/env bash

set -e

INPUT_TEMPLATES_DIR="$(readlink -f ./database_scripts/)"
OUTPUT_TEMPLATES_DIR="$(readlink -f ./mongo-entrypoint/)"
VITAMUI_DEPLOYMENT_DIR="$(readlink -f ../../../deployment)"
DEVELOPPEMENT_CONFIG_FILE="$(readlink -f ./deployment_dev_config.yml)"


# Clean old scripts
rm -Rf $OUTPUT_TEMPLATES_DIR/*

### Override deployment/ansible.cfg config for template scropt
# Use default ansible stdout format
export ANSIBLE_CALLBACK_PLUGINS=/usr/share/ansible/plugins/callback
export ANSIBLE_STDOUT_CALLBACK=skippy


cd $VITAMUI_DEPLOYMENT_DIR
# Used by deployment/roles/tools/vitamui-mongod-templater
ansible-playbook -i environment/hosts playbooks/tools/database_scripts_templater.yml \
                    -e mongod_source_template_dir="$INPUT_TEMPLATES_DIR" \
                    -e mongod_output_template_dir="$OUTPUT_TEMPLATES_DIR" \
                    --extra-vars @$DEVELOPPEMENT_CONFIG_FILE $@
RC=$?
if [ $RC != "0" ] ; then
    echo "Error happened during script generation"
    exit $RC
fi
