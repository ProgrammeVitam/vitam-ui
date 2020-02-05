#!/usr/bin/env bash

#
# Boots VM vitamui
#
set -e

SCRIPT_DIR="$(readlink -f `dirname $0`)"
cd $SCRIPT_DIR
. $SCRIPT_DIR/_common.sh

VM=${1}

if [ "$1" == "-h" ] ; then
    echo "Ping Vagrantfile configured IP for the VM"
    echo "Usage : $(basename $0) [VM]"
    echo -e "\tVM: name of the vm to use in Vagrantfile [vitamui]"
    exit
fi

IP=`cat Vagrantfile | sed '/^$/d' | grep -v "#"  | grep -A 10 config.vm.define  | grep -A 10 "\"$VM\""  | grep -oE '([0-9]{1,3}[\.]){3}[0-9]{1,3}'`

echo -en "Pinging: $IP ... "
ping -t 1 -c 1 $IP > /dev/null && echo "OK" || (
    echo "NOT REACHABLE";
    echo "Dumping Ip addr in VM: ";
    vagrant ssh $VM -- 'sudo ip addr'
)
