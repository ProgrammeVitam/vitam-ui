#!/usr/bin/env bash
set -e

################################################################################
################################## Includes  ###################################
################################################################################

. "$(dirname $0)/lib/commons.sh"

################################################################################
################################## Functions ###################################
################################################################################

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
    local AUTHORITY="${1}"
    local TYPE_CERTIFICAT="${2}"
    local COMPONENT="${3}"
    local SERVICE_HOSTNAME="${4}"
    local SERVICE_DC_HOSTNAME="${5}"
    local REVERSE_SAN="${6}"

    # Correctly set Subject Alternate Name (env var is read inside the openssl configuration file)
    export OPENSSL_SAN="$(getComponentCertificateSan $SERVICE_HOSTNAME $SERVICE_DC_HOSTNAME $REVERSE_SAN)"
    # Correctly set certificate CN (env var is read inside the openssl configuration file)
    export OPENSSL_CN="$(getComponentCertificateCn $SERVICE_HOSTNAME)"
    # Correctly set certificate DIRECTORY (env var is read inside the openssl configuration file)
    export OPENSSL_CRT_DIR=${AUTHORITY}

    pki_logger "Starting process to generate ${TYPE_CERTIFICAT} certificate signed with CA ${AUTHORITY} for component ${COMPONENT}..."
    mkdir -p "${CERTIFICATE_DIR}/${AUTHORITY}/servers/${COMPONENT}"

    # Retrieve the passphrase of the CA_INTERMEDIATE from the vault-ca
    local CA_INTERMEDIATE_PASS=$(getPassphrase ca "ca_intermediate_${AUTHORITY}")
    # set passphrase for the key
    local KEY_PASS=$(setPassphrase certs "${AUTHORITY}_${TYPE_CERTIFICAT}_${COMPONENT}")

    local SERVER_CERTIFICATE_PATH="${CERTIFICATE_DIR}/${AUTHORITY}/servers/${COMPONENT}"

    pki_logger "Generating ${TYPE_CERTIFICAT} key for component ${COMPONENT}..."
    openssl req -newkey "${CRYPTO_SPEC}" \
        -passout pass:"${KEY_PASS}" \
        -keyout "${SERVER_CERTIFICATE_PATH}/${COMPONENT}.key" \
        -out "${SERVER_CERTIFICATE_PATH}/${COMPONENT}.req" \
        -nodes \
        -config "${CONFIG_DIR}/crt-config" \
        -batch

    pki_logger "Generating ${TYPE_CERTIFICAT} crt for component ${COMPONENT}..."
    openssl ca -config "${CONFIG_DIR}/crt-config" \
        -passin pass:"${CA_INTERMEDIATE_PASS}" \
        -out "${SERVER_CERTIFICATE_PATH}/${COMPONENT}.crt" \
        -in "${SERVER_CERTIFICATE_PATH}/${COMPONENT}.req" \
        -extensions extension_${TYPE_CERTIFICAT} -batch

    purge_directory "${SERVER_CERTIFICATE_PATH}"
    purge_directory "${CONFIG_DIR}/${AUTHORITY}"
}

# Generate a client certificate
function generateClientCertificate {
    local AUTHORITY="${1}"
    local TYPE_CERTIFICAT="${2}"
    local COMPONENT="${3}"

    # Correctly set certificate CN (env var is read inside the openssl configuration file)
    export OPENSSL_CN="${COMPONENT}"
    # Correctly set certificate DIRECTORY (env var is read inside the openssl configuration file)
    export OPENSSL_CRT_DIR=${AUTHORITY}

    pki_logger "Starting process to generate ${TYPE_CERTIFICAT} certificate for component ${COMPONENT}..."
    mkdir -p "${CERTIFICATE_DIR}/${AUTHORITY}/clients/${COMPONENT}"

    # Retrieve the passphrase of the CA_INTERMEDIATE from the vault-ca
    local CA_INTERMEDIATE_PASS=$(getPassphrase ca "ca_intermediate_${AUTHORITY}")
    # set passphrase for the key
    local KEY_PASS=$(setPassphrase certs "${AUTHORITY}_${TYPE_CERTIFICAT}_${COMPONENT}")

    local CLIENT_CERTIFICATE_PATH="${CERTIFICATE_DIR}/${AUTHORITY}/${TYPE_CERTIFICAT}/${COMPONENT}"

    pki_logger "Generating ${TYPE_CERTIFICAT} key for component ${COMPONENT}..."
    # TODO: Workaround with -nodes parameter to avoid passphrase.
    # Remove this parameter when we have a solution for providing the passphrase to ansible during deployment.
    openssl req -newkey "${CRYPTO_SPEC}" \
        -passout pass:"${KEY_PASS}" \
        -nodes \
        -keyout "${CLIENT_CERTIFICATE_PATH}/${COMPONENT}.key" \
        -out "${CLIENT_CERTIFICATE_PATH}/${COMPONENT}.req" \
        -config "${CONFIG_DIR}/crt-config" \
        -batch

    pki_logger "Generating ${TYPE_CERTIFICAT} crt signed with ${AUTHORITY} for component ${COMPONENT}..."
    openssl ca -config "${CONFIG_DIR}/crt-config" \
        -passin pass:"${CA_INTERMEDIATE_PASS}" \
        -out "${CLIENT_CERTIFICATE_PATH}/${COMPONENT}.crt" \
        -in "${CLIENT_CERTIFICATE_PATH}/${COMPONENT}.req" \
        -extensions extension_${TYPE_CERTIFICAT} -batch

    # Generating pem only for cas-server and ui-* components...
    # Mandatory for loading the certificates in database 'security -> certificates' for authentification purposes
    if [ "${COMPONENT}" == "cas-server" ] || [[ "${COMPONENT}" == ui-* ]]; then
        pki_logger "Generating ${TYPE_CERTIFICAT} pem for component ${COMPONENT}..."
        openssl x509 \
            -in "${CLIENT_CERTIFICATE_PATH}/${COMPONENT}.crt" \
            -out "${CLIENT_CERTIFICATE_PATH}/${COMPONENT}.pem"
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

    local TYPE_CERTIFICAT="servers"
    local REVERSE_SAN=""

    pki_logger "Creating server certificate for COMPONENT: ${AUTHORITY}/${COMPONENT}"
    pki_logger "DEBUG" "DC_NAME=${DC_NAME}, CONSUL_DOMAIN=${CONSUL_DOMAIN}"

    if [ "${COMPONENT}" == "reverse" ]; then
        REVERSE_SAN=$(read_ansible_var "vitamui_reverse_external_dns" hosts_vitamui_reverseproxy[0])
        pki_logger "DEBUG" "REVERSE_SAN=${REVERSE_SAN}"
    fi

    local CERTIFICATE_FILE="${CERTIFICATE_DIR}/${AUTHORITY}/${TYPE_CERTIFICAT}/${COMPONENT}/${COMPONENT}.crt"
    if [ ! -f "${CERTIFICATE_FILE}" ]; then
         # Create the server certificate
         generateServerCertificate ${AUTHORITY} \
                                   ${TYPE_CERTIFICAT} \
                                   ${COMPONENT} \
                                   "vitamui-${COMPONENT}.service.${CONSUL_DOMAIN}" \
                                   "vitamui-${COMPONENT}.service.${DC_NAME}.${CONSUL_DOMAIN}" \
                                   "${REVERSE_SAN}"
    else
        pki_logger "Certificate ${CERTIFICATE_FILE} already exists, it will not be recreated..."
    fi
}

# Generate client certificate and store the passphrase
function generateClientCertAndStorePassphrase {
    local COMPONENT="${1}"
    local AUTHORITY="${2}"

    local TYPE_CERTIFICAT="clients"

    pki_logger "Creating client certificate for COMPONENT: ${AUTHORITY}/${COMPONENT}"

    local CERTIFICATE_FILE="${CERTIFICATE_DIR}/${AUTHORITY}/${TYPE_CERTIFICAT}/${COMPONENT}/${COMPONENT}.crt"
    if [ ! -f "${CERTIFICATE_FILE}" ]; then
        # Create the client certificate
        generateClientCertificate ${AUTHORITY} \
                                  ${TYPE_CERTIFICAT} \
                                  ${COMPONENT}
    else
        pki_logger "Certificate ${CERTIFICATE_FILE} already exists, it will not be recreated..."
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

# Method to get the CONSUL_DOMAIN of the environment.
# @return The CONSUL_DOMAIN of the environment.
function getConsulDomain {
    echo $(read_ansible_var "consul_domain" "hosts_cas_server[0]")
}

# Method to get the DC_NAME of the environment (vitamui_site_name or vitam_site_name).
# @return The DC_NAME of the environment.
function getDcName {
    local VITAMUI_SITE_NAME=$(read_ansible_var "vitamui_site_name" "hosts_vitamui_consul_server[0]")
    if [[ -z "$VITAMUI_SITE_NAME" || "$VITAMUI_SITE_NAME" =~ "VARIABLEISNOTDEFINED" ]]; then
        local VITAM_SITE_NAME=$(read_ansible_var "vitam_site_name" "hosts_cas_server[0]")
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

    # Parameters check
    if [ "${1}" == "" ]; then
        pki_logger "ERROR" "This script needs to know on which environment you want to apply to !"
        exit 1
    fi
    if [ "$#" -gt 1 ]; then
        if [ "${2,,}" == "true" ]; then
            ERASE="true"
        fi
    fi
    ENVIRONMENT_FILE="${1}"

    if [ ! -f "${ENVIRONMENT_FILE}" ]; then
        pki_logger "ERROR" "Cannot find environment file: ${ENVIRONMENT_FILE}"
        exit 1
    fi

    pki_logger "Input parameters:"
    pki_logger "    -> Environment: ${ENVIRONMENT_FILE}"
    pki_logger "    -> Erase existing certificates: ${ERASE}"

    # Get CONSUL_DOMAIN
    CONSUL_DOMAIN=$(getConsulDomain)
    # Get DC_NAME
    DC_NAME=$(getDcName)

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
