#!/usr/bin/env bash
set -e

################################################################################
################################## Includes  ###################################
################################################################################

. "$(dirname $0)/lib/commons.sh"

################################################################################
################################## Functions ###################################
################################################################################

# Generate the path of a server certificate
function getServerCertificatePath {
    local AUTHORITY="${1}"
    local COMPONENT="${2}"
    echo "${CERTIFICATE_DIR}/${AUTHORITY}/servers/${COMPONENT}"
}

# Generate the Subject Alternate Name for a server certificate
function getComponentCertificateSan {
    local SERVICE_HOSTNAME="${1}"
    local SERVICE_DC_HOSTNAME="${2}"
    local REVERSE_SAN="${3}"

    if [ -n "${REVERSE_SAN}" ]; then
        echo "DNS:${SERVICE_HOSTNAME},DNS:${SERVICE_DC_HOSTNAME},DNS:${REVERSE_SAN}"
    else
        echo "DNS:${SERVICE_HOSTNAME},DNS:${SERVICE_DC_HOSTNAME}"
    fi
}

# Generate the CN Name for a server certificate
function getComponentCertificateCn {
    local SERVICE_HOSTNAME="${1}"
    echo "${SERVICE_HOSTNAME}"
}

# Generate a server certificate
function generateServerCertificate {
    local COMPOSANT="${1}"
    local KEY_PASS="${2}"
    local INTERMEDIATE_CA_KEY="${3}"
    local TYPE_CERTIFICAT="${4}"
    local AUTHORITY="${5}"
    local SERVICE_HOSTNAME="${6}"
    local SERVICE_DC_HOSTNAME="${7}"
    local REVERSE_SAN="${8}"

    # Correctly set Subject Alternate Name (env var is read inside the openssl configuration file)
    export OPENSSL_SAN="$(getComponentCertificateSan $SERVICE_HOSTNAME $SERVICE_DC_HOSTNAME $REVERSE_SAN)"
    # Correctly set certificate CN (env var is read inside the openssl configuration file)
    export OPENSSL_CN="$(getComponentCertificateCn $SERVICE_HOSTNAME)"
    # Correctly set certificate DIRECTORY (env var is read inside the openssl configuration file)
    export OPENSSL_CRT_DIR=${AUTHORITY}

    pki_logger "Starting process to generate ${TYPE_CERTIFICAT} certificate signed with CA ${AUTHORITY} for ${COMPOSANT}..."
    local SERVER_CERTIFICATE_PATH=$(getServerCertificatePath ${AUTHORITY} ${COMPOSANT})
    mkdir -p "${SERVER_CERTIFICATE_PATH}"
    pki_logger "Generating ${TYPE_CERTIFICAT} key for ${COMPOSANT}..."
    openssl req -newkey "${CRYPTO_SPEC}" \
        -passout pass:"${KEY_PASS}" \
        -keyout "${SERVER_CERTIFICATE_PATH}/${COMPOSANT}.key" \
        -out "${SERVER_CERTIFICATE_PATH}/${COMPOSANT}.req" \
        -nodes \
        -config "${CONFIG_DIR}/crt-config" \
        -batch

    pki_logger "Generating ${TYPE_CERTIFICAT} crt for ${COMPOSANT}..."
    openssl ca -config "${CONFIG_DIR}/crt-config" \
        -passin pass:"${INTERMEDIATE_CA_KEY}" \
        -out "${SERVER_CERTIFICATE_PATH}/${COMPOSANT}.crt" \
        -in "${SERVER_CERTIFICATE_PATH}/${COMPOSANT}.req" \
        -extensions extension_${TYPE_CERTIFICAT} -batch

    purge_directory "${SERVER_CERTIFICATE_PATH}"
    purge_directory "${CONFIG_DIR}/${AUTHORITY}"
}

# Generate the path of a client certificate
function getClientCertificatePath {
    local AUTHORITY="${1}"
    local COMPOSANT="${2}"
    echo "${CERTIFICATE_DIR}/${AUTHORITY}/clients/${COMPOSANT}"
}

# Generate a client certificate
function generateClientCertificate {
    local COMPOSANT="${1}"
    local KEY_PASS="${2}"
    local CA_INTERMEDIATE_PASS="${3}"
    local TYPE_CERTIFICAT="${4}"
    local AUTHORITY="${5}"

    # Correctly set certificate CN (env var is read inside the openssl configuration file)
    export OPENSSL_CN="${COMPOSANT}"
    # Correctly set certificate DIRECTORY (env var is read inside the openssl configuration file)
    export OPENSSL_CRT_DIR=${AUTHORITY}

    pki_logger "Starting process to generate ${TYPE_CERTIFICAT} certificate for ${COMPOSANT}..."
    local CLIENT_CERTIFICATE_PATH=$(getClientCertificatePath ${AUTHORITY} ${COMPOSANT})
    mkdir -p "${CLIENT_CERTIFICATE_PATH}"
    pki_logger "Generating ${TYPE_CERTIFICAT} key for ${COMPOSANT}..."
    # TODO: Workaround with -nodes parameter to avoid passphrase.
    # Remove this parameter when we have a solution for providing the passphrase to ansible during deployment.
    openssl req -newkey "${CRYPTO_SPEC}" \
        -passout pass:"${KEY_PASS}" \
        -nodes \
        -keyout "${CLIENT_CERTIFICATE_PATH}/${COMPOSANT}.key" \
        -out "${CLIENT_CERTIFICATE_PATH}/${COMPOSANT}.req" \
        -config "${CONFIG_DIR}/crt-config" \
        -batch

    pki_logger "Generating ${TYPE_CERTIFICAT} crt signed with ${AUTHORITY} for ${COMPOSANT}..."
    openssl ca -config "${CONFIG_DIR}/crt-config" \
        -passin pass:"${CA_INTERMEDIATE_PASS}" \
        -out "${CLIENT_CERTIFICATE_PATH}/${COMPOSANT}.crt" \
        -in "${CLIENT_CERTIFICATE_PATH}/${COMPOSANT}.req" \
        -extensions extension_${TYPE_CERTIFICAT} -batch

    pki_logger "Generating ${TYPE_CERTIFICAT} pem only for cas-server and ui-* components..."
    # Mandatory for loading the certificates in database 'security -> certificates' for authentification purposes
    if [ "${COMPOSANT}" == "cas-server" ] || [[ "${COMPOSANT}" == ui-* ]]; then
        pki_logger "Generating ${TYPE_CERTIFICAT} pem for ${COMPOSANT}..."
        openssl x509 \
            -in "${CLIENT_CERTIFICATE_PATH}/${COMPOSANT}.crt" \
            -out "${CLIENT_CERTIFICATE_PATH}/${COMPOSANT}.pem"
    fi

    purge_directory "${CLIENT_CERTIFICATE_PATH}"
    purge_directory "${CONFIG_DIR}/${AUTHORITY}"
}

# Generate a server and a client certificate and store passphrase
function generateServerAndClientCertAndStorePassphrase {
    local COMPONENT="${1}"
    local AUTHORITY="${2}"
    generateServerCertAndStorePassphrase "${COMPONENT}" "${AUTHORITY}"
    generateClientCertAndStorePassphrase "${COMPONENT}" "${AUTHORITY}"
}

# Generate a server certificate and store passphrase
function generateServerCertAndStorePassphrase {
    local COMPONENT="${1}"
    local AUTHORITY="${2}"

    pki_logger "DEBUG" "generateServerCertAndStorePassphrase called with $# args: COMPONENT=$1, AUTHORITY=$2"

    local TYPE_CERTIFICAT="servers"
    local REVERSE_SAN=""

    # Retrieve the passphrase of the CA_INTERMEDIATE from the vault-ca
    CA_INTERMEDIATE_PASS=$(getPassphrase ca "ca_intermediate_${AUTHORITY}")
    DC_NAME=$(getDcName)

    if [ "${COMPONENT}" == "reverse" ]; then
        REVERSE_SAN=$(read_ansible_var "vitamui_reverse_external_dns" hosts_vitamui_reverseproxy[0])
        pki_logger "DEBUG" "REVERSE_SAN=${REVERSE_SAN}"
    fi

    pki_logger "DEBUG" "DC_NAME=${DC_NAME}, CONSUL_DOMAIN=${CONSUL_DOMAIN}"

    local SERVER_CERTIFICATE_PATH=$(getServerCertificatePath ${AUTHORITY} ${COMPONENT})
    if [ ! -f "${SERVER_CERTIFICATE_PATH}/${COMPONENT}.crt" ]; then
         # Generate the passphrase
         local KEY_PASS=$(generatePassphrase)
         # Create the certificate
         generateServerCertificate ${COMPONENT} \
                                   ${KEY_PASS} \
                                   ${CA_INTERMEDIATE_PASS} \
                                   ${TYPE_CERTIFICAT} \
                                   ${AUTHORITY} \
                                   "vitamui-${COMPONENT}.service.${CONSUL_DOMAIN}" \
                                   "vitamui-${COMPONENT}.service.${DC_NAME}.${CONSUL_DOMAIN}" \
                                   "${REVERSE_SAN}"
        # Store the key to the vault
        setPassphrase certs "${AUTHORITY}_${TYPE_CERTIFICAT}_${COMPONENT}" "${KEY_PASS}"
    else
        pki_logger "Le certificat ${AUTHORITY} - ${TYPE_CERTIFICAT} - ${COMPONENT}.crt existe déjà, il ne sera pas recréé..."
    fi
}

# Generate client certificate and store the passphrase
function generateClientCertAndStorePassphrase {
    local COMPONENT="${1}"
    local AUTHORITY="${2}"

    pki_logger "DEBUG" "generateClientCertAndStorePassphrase called with $# args: COMPONENT=$1, AUTHORITY=$2"

    local TYPE_CERTIFICAT="clients"

    local CLIENT_CERTIFICATE_PATH=$(getClientCertificatePath ${AUTHORITY} ${COMPONENT})
    if [ ! -f "${CLIENT_CERTIFICATE_PATH}/${COMPONENT}.crt" ]; then
        # Get the CA_INTERMEDIATE passphrase from the vault-ca
        local CA_INTERMEDIATE_PASS=$(getPassphrase ca "ca_intermediate_${AUTHORITY}")

        # Generate the key
        local KEY_PASS=$(generatePassphrase)
        # Create the certificate
        generateClientCertificate ${COMPONENT} \
                                  ${KEY_PASS} \
                                  ${CA_INTERMEDIATE_PASS} \
                                  ${TYPE_CERTIFICAT} \
                                  ${AUTHORITY}
        # Store the key to the vault
        setPassphrase certs "${AUTHORITY}_${TYPE_CERTIFICAT}_${COMPONENT}" "${KEY_PASS}"
    else
        pki_logger "Le certificat ${AUTHORITY} - ${TYPE_CERTIFICAT} - ${COMPONENT} existe déjà, il ne sera pas recréé..."
    fi
}

# Copy the CA from pki/<AUTHORITY>/ca to environments/certs/<AUTHORITY>/ca
function copyCAFromPki {
    local AUTHORITY="${1}"

    mkdir -p "${CERTIFICATE_DIR}/${AUTHORITY}/ca"
    pki_logger "Copying CA of ${AUTHORITY}"
    for CA in $(ls ${CA_DIR}/${AUTHORITY}/*.crt); do
        cp -vf "${CA}" "${CERTIFICATE_DIR}/${AUTHORITY}/ca/$(basename ${CA})"
    done
}

function getConsulDomain {
    echo $(read_ansible_var "consul_domain" "hosts_cas_server[0]")
}

function getDcName {
    # Get DC_NAME
    VITAMUI_SITE_NAME=$(read_ansible_var "vitamui_site_name" "hosts_vitamui_consul_server[0]")
    if [[ -z "$VITAMUI_SITE_NAME" || "$VITAMUI_SITE_NAME" =~ "VARIABLEISNOTDEFINED" ]]; then
        VITAM_SITE_NAME=$(read_ansible_var "vitam_site_name" "hosts_cas_server[0]")
        echo $VITAM_SITE_NAME
    else
        echo $VITAMUI_SITE_NAME
    fi
}

function generateCerts {
    # To override
    pki_logger "Generation of certificates"
}

################################################################################
##################################    Main    ##################################
################################################################################

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

    pki_logger "Input parameters:"
    pki_logger "    -> Environnement: ${ENVIRONNEMENT}"
    pki_logger "    -> Erase existing certificates: ${ERASE}"

    # Get consul_domain
    CONSUL_DOMAIN=$(getConsulDomain)

    # Cleaning or creating vault file for certs
    initVault   certs   ${ERASE}

    if [ "${ERASE}" == "true" ]; then
        if [ -d ${CERTIFICATE_DIR} ]; then
            # We remove all generated certs
            find ${CERTIFICATE_DIR:?} -type f -name *.crt -exec rm -vf {} \;
            find ${CERTIFICATE_DIR:?} -type f -name *.key -exec rm -vf {} \;
            find ${CERTIFICATE_DIR:?} -type f -name *.pem -exec rm -vf {} \;
            find ${CERTIFICATE_DIR:?} -type d -empty -delete
        fi
    fi
    if [ ! -d ${CERTIFICATE_DIR} ]; then
        pki_logger "Directory ${CERTIFICATE_DIR} does not exist, creating it..."
        mkdir -p ${CERTIFICATE_DIR}
    fi
    if [ ! -d ${TEMP_CERTS} ]; then
        pki_logger "Directory ${TEMP_CERTS} does not exist, creating it..."
        mkdir -p ${TEMP_CERTS}
    fi

    generateCerts

    if [ -d ${TEMP_CERTS} ]; then
        pki_logger "=============================================="
        pki_logger "Cleaning of temporary tempcerts directories"
        rm -vRf ${TEMP_CERTS:?}
    fi
    pki_logger "=============================================="
    pki_logger "End of certificates creation procedure"
}
