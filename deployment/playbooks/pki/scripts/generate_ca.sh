#!/usr/bin/env bash
set -e

######################################################################
############################# Includes  ##############################
######################################################################

. $(dirname $0)/lib/functions.sh


######################################################################
############################# Functions ##############################
######################################################################

# Génération de la CA root
function generate_ca_root {
    local MDP_CAROOT_KEY="${1}"
    local CA_ROOT_CN="${2}"

    if [ -f ${REPERTOIRE_CA}/ca-root.crt ]; then
        pki_logger "Le certificat ${REPERTOIRE_CA}/ca-root.crt existe déjà. Vider le dossier pour le regénérer."
        return
    fi

    # Correctly set certificate CN (env var is read inside the openssl configuration file)
    export OPENSSL_CN=${CA_ROOT_CN}
    # Correctly set certificate DIRECTORY (env var is read inside the openssl configuration file)
    export OPENSSL_CA_DIR=

    #Create index.txt file
    touch ${REPERTOIRE_CONFIG}/index.txt
    #Create serial file
    openssl rand -hex 16 > ${REPERTOIRE_CONFIG}/serial.txt

    pki_logger "Create CA request..."
    openssl req \
        -config ${REPERTOIRE_CONFIG}/ca-config \
        -new \
        -out ${REPERTOIRE_CA}/ca-root.req \
        -keyout ${REPERTOIRE_CA}/ca-root.key \
        -passout pass:${MDP_CAROOT_KEY} \
        -batch

    pki_logger "Create CA certificate..."
    openssl ca \
        -config ${REPERTOIRE_CONFIG}/ca-config \
        -selfsign \
        -extensions extension_ca_root \
        -in ${REPERTOIRE_CA}/ca-root.req \
        -passin pass:${MDP_CAROOT_KEY} \
        -out ${REPERTOIRE_CA}/ca-root.crt \
        -batch
}


######################################################################
#############################    Main    #############################
######################################################################

cd $(dirname $0)/../..

pki_logger "Lancement de la procédure de création des CA"
pki_logger "=============================================="
if [ ! -d ${REPERTOIRE_CA} ]; then
    pki_logger "Répertoire ${REPERTOIRE_CA} absent ; création..."
    mkdir -p ${REPERTOIRE_CA};
fi
if [ ! -d ${TEMP_CERTS} ]; then
    pki_logger "Création du répertoire de travail temporaire tempcerts sous ${TEMP_CERTS}..."
    mkdir -p ${TEMP_CERTS}
fi

# Création des répertoires pour le CA ROOT

pki_logger "Création de CA root pour ${ITEM}..."
generate_ca_root carootkeypassword ca_root_vitamui # FIXME : parameters for passwords

purge_directory "${REPERTOIRE_CONFIG}"
purge_directory "${REPERTOIRE_CA}"

pki_logger "----------------------------------------------"

pki_logger "=============================================="
pki_logger "Fin de la procédure de création des CA"
