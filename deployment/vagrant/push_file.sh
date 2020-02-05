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

function help(){
    echo "Push a local file/directory to VM."
    echo "$(basename $0) VM_NAME LOCAL_PATH REMOTE_DEST"
    echo -e "\tVM: name of the vm to use in Vagrantfile [vitamui]"
    echo -e "\LOCAL_PATH: local path of the file/directory to push in the VM. Directories are copîed recursively"
    echo -e "\tREMOTE_DEST: Remote directory/file path. Must be absolute. Non existing dir in REMOTE_DEST will not be created"
}

if [ "$1" == "-h" ] ; then
    help
    exit
fi

if [ "$#" != 3 ]; then
    help
    exit
fi

IP=`cat Vagrantfile | sed '/^$/d' | grep -v "#"  | grep -A 10 config.vm.define  | grep -A 10 "\"$VM\""  | grep -oE '([0-9]{1,3}[\.]){3}[0-9]{1,3}'`
SSH_DIR_OPTS=""
if [ -d "$2" ] ; then
    SSH_DIR_OPTS="-r"
fi
scp -o "UserKnownHostsFile=/dev/null" -o "StrictHostKeyChecking=no" $SSH_DIR_OPTS  -i .vagrant/machines/$VM/virtualbox/private_key "$2" "vagrant@$IP:/$3"
