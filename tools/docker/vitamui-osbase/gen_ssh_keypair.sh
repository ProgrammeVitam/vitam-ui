#!/usr/bin/env bash

#
# Generates keypair for ssh osbase container
#
ssh-keygen -b 2048 -t rsa -f ./ssh/vitamuiadmin_rsa  -q -N "" -C ""
