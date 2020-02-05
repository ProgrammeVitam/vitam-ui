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
    echo "Removes the VM and destroys all files"
    echo "Usage : $(basename $0) [VM]"
    echo -e "\tVM: name of the vm to use in Vagrantfile [vitamui]"
    exit
fi

vagrant halt $VM
vagrant destroy -f $VM
