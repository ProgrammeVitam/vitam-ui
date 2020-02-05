#!/bin/bash

SYS_USER=vitamuiroot
SYS_USER_HOME=/home/$SYS_USER
SYS_USER_PASSSWD=vitamuiroot

# Copy keys to allow ssh key logging
echo "======> SSH KEYS configuration"
sudo mkdir -p $SYS_USER_HOME/.ssh/
sudo cat /vagrant/ssh/vitamuiroot_rsa.pub >> $SYS_USER_HOME/.ssh/authorized_keys
sudo chown -R $SYS_USER:$SYS_USER $SYS_USER_HOME/.ssh
sudo chmod 600 $SYS_USER_HOME/.ssh/*
