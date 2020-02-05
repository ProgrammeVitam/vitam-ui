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

# Directory where all rpm are found
DEFAULT_VITAMUI_RPM_SOURCE_DIRECTORY=`realpath $SCRIPT_DIR/..`
VITAMUI_RPM_SOURCE_DIRECTORY=${1:-$DEFAULT_VITAMUI_RPM_SOURCE_DIRECTORY}
VITAMUI_RPM_SOURCE_DIRECTORY=`realpath $VITAMUI_RPM_SOURCE_DIRECTORY`

if [ $# -gt 0 ]
then
    shift
fi

ansible-playbook -i $VITAMUI_DEPLOYEMENT_HOSTS playbooks/create_local_repo.yml -e vitamui_rpm_source_directory="$VITAMUI_RPM_SOURCE_DIRECTORY"  "$@"
