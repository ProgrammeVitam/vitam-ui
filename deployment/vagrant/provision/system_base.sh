#!/bin/bash

echo "======> Disabling selinux"
sudo setenforce 0
# FOr next reboot
sudo sed -i 's/^SELINUX=.*/SELINUX=disabled/g'  /etc/selinux/config

sudo yum update
sudo yum upgrade

echo "======> Setup basic packages"
sudo yum install -y net-tools nano htop




