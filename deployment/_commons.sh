#!/usr/bin/env bash

set -e
SCRIPT_DIR="$(readlink -f `dirname $0`)"
cd $SCRIPT_DIR

# Adjust ssh perms:
chmod 0700 ./ssh/
chmod 0600 ./ssh/*_rsa
