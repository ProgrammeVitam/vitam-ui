#!/usr/bin/env bash
set -e

######################################################################
############################# Includes  ##############################
######################################################################

. $(dirname $0)/lib/ca.sh

######################################################################
#########################    Overriding    ###########################
######################################################################

function get_autorities() {
    echo "server client-external client-vitam"
}

# Génération de la CA intermédiaire
function init_config_ca {
    local CA_DIR="${1}"

    # Suppression de la configuration existante.
    rm -Rf "${REPERTOIRE_CONFIG}/${CA_DIR}"
    mkdir -p "${REPERTOIRE_CONFIG}/${CA_DIR}"
    touch "${REPERTOIRE_CONFIG}/${CA_DIR}/index.txt"
    echo '01' > "${REPERTOIRE_CONFIG}/${CA_DIR}/serial"
    touch "${REPERTOIRE_CONFIG}/${CA_DIR}/crlnumber"
}

######################################################################
#############################    Main    #############################
######################################################################

main "$@"
