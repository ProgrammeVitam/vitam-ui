#!/usr/bin/env bash

#
# Installs vitamui solution
#
set -e
SCRIPT_DIR="$(readlink -f `dirname $0`)"
cd $SCRIPT_DIR
. ./_commons.sh

# ---- init args:
CREATE_REPO="n"
REMOTE_REPO="y"
VITAMUI_VERSION="develop"
GENERATE_HOSTS_FILE="n"
EXTRA_DEV_SCRIPTS="y"
GENERATE_KEYSTORE="y"

INSTALL_ARGS=()
ANSIBLE_EXTRA_ARGS=()

# ---- parse cli:
function usage(){
    echo "`basename $0` [options] [ANSIBLE_EXTRA_ARGS]"
    echo "With options in:"
    echo "  -h|--help:                          Prints help"
    echo "  -c|--createrepo:                    Create local rpm repository before installing"
    echo "  -r|--remoterepo:                    Use remote rpm repository instead of creating one"
    echo "  -l|--local-dns:                     Generate localhost-*.vitamui.com"
    echo "  -n|--no-extra-dev-scripts:          Deactivate extra dev scripts from tools/docker/mongod/ execution"
    echo "  -k|--no-keystore-gen:               Deactivate keystore regeneration"
    echo "  -v|--vitamuiversion=[VITAMUI_VERSION]     vitamui rpm version to install. Default: develop"
}

while [ "$1" != "" ]; do
    case "$1" in
        -h | --help)                    usage; exit              ;; # quit and show usage
        -c | --createrepo)              CREATE_REPO="y"          ;;
        -r | --remoterepo)              REMOTE_REPO="y"          ;;
        -l | --local-dns)               GENERATE_HOSTS_FILE="y"  ;;
        -n | --no-extra-dev-scripts)    EXTRA_DEV_SCRIPTS="n"    ;;
        -k | --no-keystore-gen)         GENERATE_KEYSTORE="n"    ;;
        -v | --version)                 VITAMUI_VERSION="$2"; shift ;;
        *)                              ANSIBLE_EXTRA_ARGS+=("$1")     ;;
    esac
    shift
done

# ----
# Hosts file fixé pour le deploiement vagrant :
export VITAMUI_DEPLOYEMENT_HOSTS=environment/hosts.all.vagrant
chmod +x *.sh

#.....
[ "$CREATE_REPO" == "y" ] &&         ( echo ">>> Creating local RPM repository" ; ./create_local_repo.sh )
[ "$GENERATE_HOSTS_FILE" == "y" ] && ( echo ">>> Creating hosts entries for localhost-*.vitamui.com " ; ./setup_local_dns.sh )
[ "$GENERATE_KEYSTORE" == "y" ] &&   ( echo ">>> Generating keystore" ; cd ${SCRIPT_DIR}/playbooks/pki/scripts/; ./generate_stores.sh)

# ----
# Installation de vitamui:
# On précisera pour l'installation le chemin vers le repository local, la valeur par defaut
# On ajoutera les scripts mongod de dev avec le flag mongo_extra_scripts
cd ${SCRIPT_DIR}

# Export vitamui version to setup
export VITAMUI_VERSION="$VITAMUI_VERSION"

# Create command line args:

[ "$REMOTE_REPO" == "n" ]       &&  (   INSTALL_ARGS+=("-e"); INSTALL_ARGS+=("vitamui_repository_url='file:///var/vitamui-yum-repository/'") )
[ "$EXTRA_DEV_SCRIPTS" == "n" ] &&  (   INSTALL_ARGS+=("-e"); INSTALL_ARGS+=("'mongo_extras_scripts=true'") )

if [ "$REMOTE_REPO" == "n" ]; then
    INSTALL_ARGS+=("-e")
    INSTALL_ARGS+=("vitamui_repository_url='file:///var/vitamui-yum-repository/'")
fi

if [ "$EXTRA_DEV_SCRIPTS" == "y" ]; then
    INSTALL_ARGS+=("-e")
    INSTALL_ARGS+=("mongo_extras_scripts=true")
fi

echo "--------------------------------------------------"
echo ">>> Installing VITAMUI"
time ${SCRIPT_DIR}/install.sh ${INSTALL_ARGS[*]} ${ANSIBLE_EXTRA_ARGS[*]}

