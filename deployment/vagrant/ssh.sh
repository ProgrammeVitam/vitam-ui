#!/usr/bin/env bash

#
# Boots VM vitamui
#
set -e
SCRIPT_DIR="$(readlink -f `dirname $0`)"
cd $SCRIPT_DIR
. $SCRIPT_DIR/_common.sh

# ---
VM=${1}

if [ "$1" == "-h" ] ; then
    echo "Open ssh connection with vagrant user in VM"
    echo "Usage : $(basename $0) VM"
    echo ""
    echo "Execute command on VM: "
    echo "Usage : $(basename $0) VM -- CMD [ARGS]..."
    echo ""
    echo -e "\tVM: name of the vm to use in Vagrantfile [vitamui]"
    exit
fi

shift || true
vagrant ssh $VM "$@"
