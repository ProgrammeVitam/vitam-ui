#!/usr/bin/env bash

#
# Installs vitamui solution
#
set -e
SCRIPT_DIR="$(readlink -f `dirname $0`)"
cd $SCRIPT_DIR
. ./_commons.sh

# ---- init args:

# ---- parse cli:

# ----
# Hosts file fixée pour le deploiement vagrant :
export VITAMUI_DEPLOYEMENT_HOSTS=environment/hosts.all.vagrant
chmod +x *.sh

# ----
# Installation de vitamui:
# On précisera pour l'installation le chemin vers le repository local, la valeur par defaut
# On ajoutera les scripts mongod de dev
echo "Uninstalling vitamui"

${SCRIPT_DIR}/uninstall.sh
