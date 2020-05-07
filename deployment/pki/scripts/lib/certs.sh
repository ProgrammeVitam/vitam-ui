#!/usr/bin/env bash
set -e

######################################################################
############################# Includes  ##############################
######################################################################

. "$(dirname $0)/lib/commons.sh"

######################################################################
############################# Functions ##############################
######################################################################

# Génération du chemin d'un certificat serveur
function getHostCertificatePath {
    local TYPE_CERTIFICAT="${1}"
    local HOSTNAME="${2}"
    echo "${REPERTOIRE_CERTIFICAT}/${TYPE_CERTIFICAT}/hosts/${HOSTNAME}"
}

# Génération du SubjectAlternate Name pour les certificats serveur.
function getHostCertificateSan {
    local HOSTNAME="${1}"
    local SERVICE_HOSTNAME="${2}"
    local SERVICE_DC_HOSTNAME="${3}"
    echo "DNS:${SERVICE_HOSTNAME},DNS:${HOSTNAME},DNS:${SERVICE_DC_HOSTNAME}"
}

# Génération du CN Name pour les certificats serveur.
function getHostCertificateCn {
    local SERVICE_DC_HOSTNAME="${1}"
    echo "${SERVICE_DC_HOSTNAME}"
}

# Génération d'un certificat serveur
function generateHostCertificate {
    local COMPOSANT="${1}"
    local CERT_KEY="${2}"
    local INTERMEDIATE_CA_KEY="${3}"
    local HOSTNAME="${4}"
    local TYPE_CERTIFICAT="${5}"
    local SERVICE_HOSTNAME="${6}"
    local SERVICE_DC_HOSTNAME="${7}"

    # Correctly set Subject Alternate Name (env var is read inside the openssl configuration file)
    export OPENSSL_SAN="$(getHostCertificateSan $HOSTNAME $SERVICE_HOSTNAME $SERVICE_DC_HOSTNAME)"
    # Correctly set certificate CN (env var is read inside the openssl configuration file)
    export OPENSSL_CN="$(getHostCertificateCn $SERVICE_DC_HOSTNAME)"
    # Correctly set certificate DIRECTORY (env var is read inside the openssl configuration file)
    export OPENSSL_CRT_DIR=${TYPE_CERTIFICAT}

    pki_logger "Création du certificat ${TYPE_CERTIFICAT} pour ${COMPOSANT} hébergé sur ${HOSTNAME}..."
    local HOST_CERTIFICATE_PATH=$(getHostCertificatePath ${TYPE_CERTIFICAT} ${HOSTNAME})
    mkdir -p "${HOST_CERTIFICATE_PATH}"
    pki_logger "Generation de la clé..."
    openssl req -newkey "${PARAM_KEY_CHIFFREMENT}" \
        -passout pass:"${CERT_KEY}" \
        -keyout "${HOST_CERTIFICATE_PATH}/${COMPOSANT}.key" \
        -out "${HOST_CERTIFICATE_PATH}/${COMPOSANT}.req" \
        -nodes \
        -config "${REPERTOIRE_CONFIG}/crt-config" \
        -batch

    pki_logger "Generation du certificat signé avec CA ${TYPE_CERTIFICAT}..."
    openssl ca -config "${REPERTOIRE_CONFIG}/crt-config" \
        -passin pass:"${INTERMEDIATE_CA_KEY}" \
        -out "${HOST_CERTIFICATE_PATH}/${COMPOSANT}.crt" \
        -in "${HOST_CERTIFICATE_PATH}/${COMPOSANT}.req" -batch
       # -extensions extension_${TYPE_CERTIFICAT} -batch

    openssl x509 \
        -in "${HOST_CERTIFICATE_PATH}/${COMPOSANT}.crt" \
        -out "${HOST_CERTIFICATE_PATH}/${COMPOSANT}.pem"

    purge_directory "${HOST_CERTIFICATE_PATH}"
    purge_directory "${REPERTOIRE_CONFIG}/${TYPE_CERTIFICAT}"
}

# Génération du chemin d'un certificat de timestamping
function getTimestampCertificatePath {
    local TYPE_CERTIFICAT="${1}"
    local HOSTNAME="${2}"
    echo "${REPERTOIRE_CERTIFICAT}/${TYPE_CERTIFICAT}/vitam"
}

# Génération d'un certificat de timestamping ; le nom du certificat est dérivé de son usage
function generateTimestampCertificate {
    local USAGE="${1}"
    local CERT_KEY="${2}"
    local INTERMEDIATE_CA_KEY="${3}"
    local TYPE_CERTIFICAT="${4}"
    local CN_VALEUR="${USAGE}"

    # Correctly set certificate CN (env var is read inside the openssl configuration file)
    export OPENSSL_CN="${CN_VALEUR}"
    # Correctly set certificate DIRECTORY (env var is read inside the openssl configuration file)
    export OPENSSL_CRT_DIR=${TYPE_CERTIFICAT}

    pki_logger "Création du certificat ${TYPE_CERTIFICAT} pour usage ${USAGE}"
    local TIMESTAMP_CERTIFICATE_PATH=$(getTimestampCertificatePath ${TYPE_CERTIFICAT})
    mkdir -p "${TIMESTAMP_CERTIFICATE_PATH}"
    pki_logger "Generation de la clé..."
    openssl req -newkey "${PARAM_KEY_CHIFFREMENT}" \
        -passout pass:"${CERT_KEY}" \
        -keyout "${TIMESTAMP_CERTIFICATE_PATH}/${USAGE}.key" \
        -out "${TIMESTAMP_CERTIFICATE_PATH}/${USAGE}.req" \
        -nodes \
        -config "${REPERTOIRE_CONFIG}/crt-config" \
        -batch

    pki_logger "Generation du certificat signé avec CA ${TYPE_CERTIFICAT}..."
    openssl ca -config "${REPERTOIRE_CONFIG}/crt-config" \
        -passin pass:"${INTERMEDIATE_CA_KEY}" \
        -out "${TIMESTAMP_CERTIFICATE_PATH}/${USAGE}.crt" \
        -in "${TIMESTAMP_CERTIFICATE_PATH}/${USAGE}.req" \
        -extensions extension_${TYPE_CERTIFICAT} -batch

    purge_directory "${TIMESTAMP_CERTIFICATE_PATH}"
    purge_directory "${REPERTOIRE_CONFIG}/${TYPE_CERTIFICAT}"
}


# Génération du chemin d'un certificat client
function getClientCertificatePath {
    local CLIENT_TYPE="${1}"
    local CLIENT_NAME="${2}"
    echo "${REPERTOIRE_CERTIFICAT}/${CLIENT_TYPE}/clients/${CLIENT_NAME}"
}

# Génération d'un certificat client
function generateClientCertificate {
    local CLIENT_NAME="${1}"
    local MDP_KEY="${2}"
    local MDP_CAINTERMEDIATE_KEY="${3}"
    local CLIENT_TYPE="${4}"
    local TYPE_CERTIFICAT="client"

    # Correctly set certificate CN (env var is read inside the openssl configuration file)
    export OPENSSL_CN="${CLIENT_NAME}"
    # Correctly set certificate DIRECTORY (env var is read inside the openssl configuration file)
    export OPENSSL_CRT_DIR=${CLIENT_TYPE}

    pki_logger "Création du certificat ${TYPE_CERTIFICAT} pour ${CLIENT_NAME}"
    local CLIENT_CERTIFICATE_PATH=$(getClientCertificatePath ${CLIENT_TYPE} ${CLIENT_NAME})
    mkdir -p "${CLIENT_CERTIFICATE_PATH}"
    pki_logger "Generation de la clé..."
    openssl req -newkey "${PARAM_KEY_CHIFFREMENT}" \
        -passout pass:"${MDP_KEY}" \
        -keyout "${CLIENT_CERTIFICATE_PATH}/${CLIENT_NAME}.key" \
        -out "${CLIENT_CERTIFICATE_PATH}/${CLIENT_NAME}.req" \
        -config "${REPERTOIRE_CONFIG}/crt-config" \
        -batch

    pki_logger "Generation du certificat signé avec ${CLIENT_TYPE}..."
    openssl ca -config "${REPERTOIRE_CONFIG}/crt-config" \
        -passin pass:"${MDP_CAINTERMEDIATE_KEY}" \
        -out "${CLIENT_CERTIFICATE_PATH}/${CLIENT_NAME}.crt" \
        -in "${CLIENT_CERTIFICATE_PATH}/${CLIENT_NAME}.req" \
        -extensions extension_${TYPE_CERTIFICAT} -batch

    purge_directory "${CLIENT_CERTIFICATE_PATH}"
    purge_directory "${REPERTOIRE_CONFIG}/${CLIENT_TYPE}"
}

# Génération des certificats serveur et stockage de la passphrase pour tous les hosts d'un host group donné
function generateHostCertAndStorePassphrase {
    local COMPONENT="${1}"
    local HOSTS_GROUP="${2}"

    # Récupération du password de la CA_INTERMEDIATE dans le vault-ca
    CA_INTERMEDIATE_PASSWORD=$(getComponentPassphrase ca "ca_intermediate_server")

    # sed "1 d" : remove the first line
    for SERVER in $(ansible -i ${ENVIRONNEMENT_FILE} --list-hosts ${HOSTS_GROUP} ${ANSIBLE_VAULT_PASSWD}| sed "1 d"); do
        
        local SERVER_CERTIFICATE_PATH=$(getHostCertificatePath "server" ${SERVER})
        if [ ! -f "${SERVER_CERTIFICATE_PATH}/${COMPONENT}.crt" ]; then
            # Generate the key
            local CERT_KEY=$(generatePassphrase)
            # Create the certificate
            generateHostCertificate ${COMPONENT} \
                                    ${CERT_KEY} \
                                    ${CA_INTERMEDIATE_PASSWORD} \
                                    ${SERVER} \
                                    "server" \
                                    "${COMPONENT}.service.${CONSUL_DOMAIN}" \
                                    "${COMPONENT}.service.${CONSUL_DOMAIN}"
            # Store the key to the vault
            setComponentPassphrase certs "server_${COMPONENT}_key" \
                                        "${CERT_KEY}"
        else
            pki_logger "Le certificat SERVER - ${SERVER} - ${COMPONENT}.crt existe déjà. Il ne sera pas recrée..."
        fi
    done
}

# Génération d'un certificat timestamp (utilise la fonction de génération de certificats serveur)
function generateTimestampCertAndStorePassphrase {
    local USAGE="${1}"

    # Récupération du password de la CA_INTERMEDIATE dans le vault-ca
    CA_INTERMEDIATE_PASSWORD=$(getComponentPassphrase ca "ca_intermediate_timestamping")
    local TIMESTAMP_CERTIFICAT_TYPE="timestamping"
    local TIMESTAMP_CERTIFICATE_PATH=$(getTimestampCertificatePath ${TIMESTAMP_CERTIFICAT_TYPE})
    if [ ! -f "${SERVER_CERTIFICATE_PATH}/${USAGE}.crt" ]; then
        # Generate the key
        local CERT_KEY=$(generatePassphrase)
        # Create the certificate
        generateTimestampCertificate ${USAGE} \
                                    ${CERT_KEY} \
                                    ${CA_INTERMEDIATE_PASSWORD}
                                    ${TIMESTAMP_CERTIFICAT_TYPE}
        # Store the key to the vault
        setComponentPassphrase certs "timestamping_${USAGE}_key" \
                                    "${CERT_KEY}"
    else
        pki_logger "Le certificat ${TIMESTAMP_CERTIFICAT_TYPE} - ${USAGE}.crt existe déjà. Il ne sera pas recrée..."
    fi
}

# Génération du certificat client et stockage de la passphrase
function generateClientCertAndStorePassphrase {
    local COMPONENT="${1}"
    local CLIENT_TYPE="${2}"

    local CLIENT_CERTIFICATE_PATH=$(getClientCertificatePath ${CLIENT_TYPE} ${COMPONENT})
    if [ ! -d "${CLIENT_CERTIFICATE_PATH}" ]; then
        # Récupération du password de la CA_INTERMEDIATE dans le vault-ca
        CA_INTERMEDIATE_PASSWORD=$(getComponentPassphrase ca "ca_intermediate_${CLIENT_TYPE}")

        # Generate the key
        local CERT_KEY=$(generatePassphrase)
        # Create the certificate
        generateClientCertificate ${COMPONENT} \
                                ${CERT_KEY} \
                                ${CA_INTERMEDIATE_PASSWORD} \
                                ${CLIENT_TYPE}
        # Store the key to the vault
        setComponentPassphrase certs "client_${CLIENT_TYPE}_${COMPONENT}_key" \
                                    "${CERT_KEY}"
    else
        pki_logger "Le certificat CLIENT - ${CLIENT_TYPE} - ${COMPONENT} existe déjà. Il ne sera pas recrée..."
    fi
}

# Recopie de la CA de pki/CA vers environments/cert/cert-type/CA
function copyCAFromPki {
    local CERT_TYPE="${1}"

    mkdir -p "${REPERTOIRE_CERTIFICAT}/${CERT_TYPE}/ca"
    pki_logger "Copie des CA de ${CERT_TYPE}"
    for CA in $(ls ${REPERTOIRE_CA}/${CERT_TYPE}/*.crt); do
        cp -f "${CA}" "${REPERTOIRE_CERTIFICAT}/${CERT_TYPE}/ca/$(basename ${CA})"
    done
}

function getConsulDomain {
    echo $(read_ansible_var "consul_domain" "hosts_vitamui_iam_internal[0]")
}

function generateCerts {
    # To override
    pki_logger "Generation of certificates"
}

######################################################################
#############################    Main    #############################
######################################################################

function main {

    # FIXME Why ? it seems to be related to the variable 'dir' set in the configuration of certificates.
    cd $(dirname $0)/../..
    init

    ERASE="false"

    # Vérification des paramètres
    if [ "${1}" == "" ]; then
        pki_logger "ERROR" "This script needs to know on which environment you want to apply to !"
        exit 1
    fi
    if [ "$#" -gt 1 ]; then
        if [ "${2,,}" == "true" ]; then
            ERASE="true"
        fi
    fi
    ENVIRONNEMENT="${1}"
    ENVIRONNEMENT_FILE="${1}"

    if [ ! -f "${ENVIRONNEMENT_FILE}" ]; then
        pki_logger "ERROR" "Cannot find environment file: ${ENVIRONNEMENT_FILE}"
        exit 1
    fi

    pki_logger "Paramètres d'entrée:"
    pki_logger "    -> Environnement: ${ENVIRONNEMENT}"
    pki_logger "    -> Ecraser les certificats existants: ${ERASE}"

    # Get consul_domain
    CONSUL_DOMAIN=$(getConsulDomain)

    # Cleaning or creating vault file for certs
    initVault   certs   ${ERASE}

    if [ "${ERASE}" == "true" ]; then
        if [ -d ${REPERTOIRE_CERTIFICAT} ]; then
            # We remove all generated certs
            find ${REPERTOIRE_CERTIFICAT} -type f -name *.crt -exec rm -f {} \;
            find ${REPERTOIRE_CERTIFICAT} -type f -name *.key -exec rm -f {} \;
            find ${REPERTOIRE_CERTIFICAT} -type f -name *.pem -exec rm -f {} \;
            find ${REPERTOIRE_CERTIFICAT} -type d -empty -delete
        fi
    fi
    if [ ! -d ${REPERTOIRE_CERTIFICAT} ]; then
        pki_logger "Création du répertoire des certicats sous ${REPERTOIRE_CERTIFICAT}..."
        mkdir -p ${REPERTOIRE_CERTIFICAT}
    fi
    if [ ! -d ${TEMP_CERTS} ]; then
        pki_logger "Création du répertoire de travail temporaire tempcerts sous ${TEMP_CERTS}..."
        mkdir -p ${TEMP_CERTS}
    fi

    generateCerts

    if [ -d ${TEMP_CERTS} ]; then
        pki_logger "=============================================="
        pki_logger "Nettoyage du répertoire de travail temporaire tempcerts"
        rm -Rf ${TEMP_CERTS}
    fi
    pki_logger "=============================================="
    pki_logger "Fin de la procédure de création des certificats"
}
