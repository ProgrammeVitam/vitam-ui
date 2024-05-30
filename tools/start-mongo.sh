#!/bin/sh

pip install virtualenv
virtualenv .virtualenvs/vitam-ui
. .virtualenvs/vitam-ui/bin/activate
pip install ansible==2.9.27
cd tools/docker/mongo || return 1
./start_dev.sh
