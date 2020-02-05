#!/bin/bash

SYS_USER=vitamuiroot
SYS_USER_HOME=/home/$SYS_USER
SYS_USER_PASSSWD=vitamuiroot

# Init vitamuisys user$
echo "======> vitamuiroot user configuration"
sudo adduser --home $SYS_USER_HOME --uid 9001 $SYS_USER
sudo echo $SYS_USER_PASSSWD | passwd $SYS_USER --stdin
sudo usermod -aG wheel $SYS_USER
