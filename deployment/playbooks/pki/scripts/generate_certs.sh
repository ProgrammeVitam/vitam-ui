#!/usr/bin/env bash
set -e

######################################################################
############################# Includes  ##############################
######################################################################

. "$(dirname $0)/lib/functions.sh"


######################################################################
############################# Functions ##############################
######################################################################

# Génération d'un certificat client
function generateCertificate {
    local COMPONENT_NAME="${1}"
    local CERT_CN="${2}"
    local MDP_KEY="${3}"
    local MDP_CAINTERMEDIATE_KEY="${4}"
    local TYPE_CERTIFICAT="client"

    if [ -f ${REPERTOIRE_CERTIFICAT}/${COMPONENT_NAME}/${COMPONENT_NAME}.crt ]; then
        pki_logger "Le certificat ${COMPONENT_NAME}/${COMPONENT_NAME}.crt existe déjà. Supprimer le dossier pour le regénérer."
        return
    fi

    # Correctly set certificate CN (env var is read inside the openssl configuration file)
    export OPENSSL_CN="${CERT_CN}"

    pki_logger "Création du certificat ${TYPE_CERTIFICAT} pour ${COMPONENT_NAME}"
    mkdir -p "${REPERTOIRE_CERTIFICAT}/${COMPONENT_NAME}"
    pki_logger "Generation de la clé..."
    openssl req -newkey "${PARAM_KEY_CHIFFREMENT}" \
        -passout pass:"${MDP_KEY}" \
        -keyout "${REPERTOIRE_CERTIFICAT}/${COMPONENT_NAME}/${COMPONENT_NAME}.key" \
        -out "${REPERTOIRE_CERTIFICAT}/${COMPONENT_NAME}/${COMPONENT_NAME}.req" \
        -config "${REPERTOIRE_CONFIG}/crt-config" \
        -batch

    pki_logger "Generation du certificat signé ..."
    openssl ca -config "${REPERTOIRE_CONFIG}/crt-config" \
        -passin pass:"${MDP_CAINTERMEDIATE_KEY}" \
        -out "${REPERTOIRE_CERTIFICAT}/${COMPONENT_NAME}/${COMPONENT_NAME}.crt" \
        -in "${REPERTOIRE_CERTIFICAT}/${COMPONENT_NAME}/${COMPONENT_NAME}.req" \
        -extensions extension_${TYPE_CERTIFICAT} -batch

    pki_logger "Transformation en PEM ..."
    openssl x509 \
        -in "${REPERTOIRE_CERTIFICAT}/${COMPONENT_NAME}/${COMPONENT_NAME}.crt" \
        -out "${REPERTOIRE_CERTIFICAT}/${COMPONENT_NAME}/${COMPONENT_NAME}.pem"

    purge_directory "${REPERTOIRE_CERTIFICAT}/${COMPONENT_NAME}"
    purge_directory "${REPERTOIRE_CONFIG}"
}


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

    purge_directory "${REPERTOIRE_VITAM_CERTIFICAT}"
    purge_directory "${REPERTOIRE_CONFIG}"
}

function import_public_certificate_pem(){
    # Imports public certificate from domain and save it to pem file
    # $1: domain to fetch certificate
    # $2: result pem file

    openssl s_client \
			-showcerts \
			-connect "$1:443" </dev/null 2>/dev/null | openssl x509 -text > $2
}


######################################################################
#############################    Main    #############################
######################################################################

cd $(dirname $0)/../..
mkdir -p "$TEMP_CERTS"

# Generate clients certificates
pki_logger "Génération des certificats "
# Method                    # Component name                    #CN                                             #key password       #caroot password
generateCertificate         cas-server                          cas-server.service.consul                       jkspasswd           carootkeypassword
generateCertificate         ui-portal                           ui-portal.service.consul                        jkspasswd           carootkeypassword
generateCertificate         ui-identity                         ui-identity.service.consul                      jkspasswd           carootkeypassword
generateCertificate         ui-identity-admin                   ui-identity-admin.service.consul                jkspasswd           carootkeypassword
generateCertificate         iam-external                        iam-external.service.consul                     jkspasswd           carootkeypassword
generateCertificate         iam-internal                        iam-internal.service.consul                     jkspasswd           carootkeypassword
generateCertificate         security-internal                   security-internal.service.consul                jkspasswd           carootkeypassword
generateCertificate         nginx 		                        *.vitamui.com                                   jkspasswd           carootkeypassword


pki_logger "Génération des certificats pour VITAM"
generateCertificateVitam   vitamui-vitam-external                 vitamui-vitam-external                             jkspasswd            carootkeypassword
generateCertificateVitam   vitamui-vitam-reverse                  vitamui-vitam-reverse                              jkspasswd            carootkeypassword
generateCertificateVitam   vitamui-client-example                 vitamui-client-example                             jkspasswd            carootkeypassword

pki_logger "Fin de script"
