#!/bin/bash

echo "======> FORCE NETWORK reconfiguration"
# Note that vagrant create an NON NETWORKMANAGER CONTROLLED interface in vm
sudo sudo nmcli connection reload
sudo systemctl restart network.service
