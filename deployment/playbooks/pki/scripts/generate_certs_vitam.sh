#!/usr/bin/env bash
set -e

######################################################################
############################# Includes  ##############################
######################################################################

. "$(dirname $0)/lib/functions.sh"


######################################################################
############################# Functions ##############################
######################################################################


function generateCertificateVitam {
    local COMPONENT_NAME="${1}"
    local CERT_CN="${2}"
    local MDP_KEY="${3}"
    local MDP_CAINTERMEDIATE_KEY="${4}"
    local TYPE_CERTIFICAT="client"

    if [ -f ${REPERTOIRE_VITAM_CERTIFICAT}/${COMPONENT_NAME}.crt ]; then
        pki_logger "Le certificat ${REPERTOIRE_VITAM_CERTIFICAT}/${COMPONENT_NAME}.crt existe déjà."
        return
    fi

    # Correctly set certificate CN (env var is read inside the openssl configuration file)
    export OPENSSL_CN="${CERT_CN}"

    mkdir -p "${REPERTOIRE_VITAM_CERTIFICAT}"
    pki_logger "Generation de la clé..."
    openssl req -newkey "${PARAM_KEY_CHIFFREMENT}" \
        -passout pass:"${MDP_KEY}" \
        -keyout "${REPERTOIRE_VITAM_CERTIFICAT}/${COMPONENT_NAME}.key" \
        -out "${REPERTOIRE_VITAM_CERTIFICAT}/${COMPONENT_NAME}.req" \
        -config "${REPERTOIRE_CONFIG}/crt-config" \
        -batch

    pki_logger "Generation du certificat signé ..."
    openssl ca -config "${REPERTOIRE_CONFIG}/crt-config" \
        -passin pass:"${MDP_CAINTERMEDIATE_KEY}" \
        -out "${REPERTOIRE_VITAM_CERTIFICAT}/${COMPONENT_NAME}.crt" \
        -in "${REPERTOIRE_VITAM_CERTIFICAT}/${COMPONENT_NAME}.req" \
        -extensions extension_${TYPE_CERTIFICAT} -batch

    pki_logger "Transformation en PEM ..."
     openssl x509 \
        -in "${REPERTOIRE_VITAM_CERTIFICAT}/${COMPONENT_NAME}.crt" \
        -out "${REPERTOIRE_VITAM_CERTIFICAT}/${COMPONENT_NAME}.pem"


    pki_logger "Certificat généré ${REPERTOIRE_VITAM_CERTIFICAT}/${COMPONENT_NAME}.crt"

    purge_directory "${REPERTOIRE_VITAM_CERTIFICAT}"
    purge_directory "${REPERTOIRE_CONFIG}"
}

function import_public_certificate_pem(){
    # Imports public certificate from domain and save it to pem file
    # $1: domain to fetch certificate
    # $2: result pem file

    openssl s_client \
			-showcerts \
			-connect "$1:443" </dev/null 2>/dev/null | openssl x509 -text  > $2
}


######################################################################
#############################    Main    #############################
######################################################################

cd $(dirname $0)/../..
mkdir -p "$TEMP_CERTS"

CLIENT_NAME=$1
KEY_PASSWORD=$2

#pki_logger "Importing cert from public domain"
#import_public_certificate_pem recette-portal.vitamui.com ${REPERTOIRE_CA}/vitamui.com.crt

pki_logger "Génération des certificats pour VITAM"
generateCertificateVitam    ${CLIENT_NAME}       ${CLIENT_NAME}      ${KEY_PASSWORD}  carootkeypassword

pki_logger "Fin de script"
