#!/usr/bin/env bash

#
# Generate a key with passphrase for testing
#
set -e

[[  $# -eq 0 ]]  && ( echo "$(basename $0) [key_path]";  exit 1)
[[ -f  "$1" ]]	 && ( echo "Key already exists [key_path]" ; exit 1)

KEY=$1
ssh-keygen -f ./$KEY -q -N ""
