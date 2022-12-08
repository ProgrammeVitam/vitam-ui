#!/bin/bash
set -e

BASEDIR=$(dirname "$0")
wget --user bbenarbia --password 'StyleBleu$1ok' --recursive --directory-prefix=$BASEDIR/repo/ --no-host-directories --cut-dirs=1 --no-parent -R "index.html*" -l3 -k "http://repository.dev.programmevitam.fr/contrib/b070acd87e1ce9c472847499d7648ebf76de8eba/deb/"

#launch it whith :  start_repo.sh "commit_id"
