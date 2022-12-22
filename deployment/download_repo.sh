#!/bin/bash
set -e

BASEDIR=$(dirname "$0")
wget --user bbenarbia --password 'StyleBleu$1ok' --recursive --directory-prefix=$BASEDIR/repo/ --no-host-directories --cut-dirs=1 --no-parent -R "index.html*" -l3 -k "http://repository.dev.programmevitam.fr/contrib/c4e8b600a6a48f4cba5faa2571afb0f78909619f/deb/"

#launch it whith :  start_repo.sh "commit_id"
