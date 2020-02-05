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
    echo "Destroys and recreate VM"
    echo "Usage : $(basename $0) [VM]"
    echo -e "\tVM: name of the vm to use in Vagrantfile [vitamui]"
    exit
fi

echo "-------->>> Stopping vm $VM"
vagrant halt $VM

echo "-------->>> destroying vm $VM"
vagrant destroy -f $VM

echo "-------->>> Creating vm $VM"
vagrant up $VM
#echo "-------->>> Reloading network service vm $VM"
##vagrant ssh "$VM" -- 'sudo systemctl restart NetworkManager'
