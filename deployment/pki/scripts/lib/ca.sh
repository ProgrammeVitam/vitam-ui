#!/usr/bin/env bash
set -e

################################################################################
################################## Includes  ###################################
################################################################################

. "$(dirname $0)/lib/commons.sh"

################################################################################
################################## Functions ###################################
################################################################################

# Generate root CA
function generate_ca_root {
    local CA_ROOT_PASS="${1}"
    local AUTHORITY="${2}"

    # Correctly set certificate CN (env var is read inside the openssl configuration file)
    export OPENSSL_CN=ca_root_${AUTHORITY}
    pki_logger "OPENSSL_CN : ${OPENSSL_CN}"
    # Correctly set certificate DIRECTORY (env var is read inside the openssl configuration file)
    export OPENSSL_CA_DIR="${AUTHORITY}"
    pki_logger "OPENSSL_CA_DIR : ${OPENSSL_CA_DIR}"

    local CA_DIR=${CA_DIR}/${OPENSSL_CA_DIR}
    if [ ! -d ${CA_DIR} ]; then
        pki_logger "Create directory ${CA_DIR}"
        mkdir -p ${CA_DIR};
    fi

    pki_logger "Create CA-root request..."
    openssl req \
        -config ${CONFIG_DIR}/ca-config \
        -new \
        -out ${CA_DIR}/ca-root.req \
        -keyout ${CA_DIR}/ca-root.key \
        -passout pass:${CA_ROOT_PASS} \
        -batch

    pki_logger "Sign CA-root certificate..."
    openssl ca \
        -config ${CONFIG_DIR}/ca-config \
        -selfsign \
        -extensions extension_ca_root \
        -in ${CA_DIR}/ca-root.req \
        -passin pass:${CA_ROOT_PASS} \
        -out ${CA_DIR}/ca-root.crt \
        -batch

    pki_logger "Convert CA-root certificate to PEM format..."
    openssl x509 \
        -in ${CA_DIR}/ca-root.crt \
        -out ${CA_DIR}/ca-root.pem \
        -outform PEM
}

# Generate intermediate CA
function generate_ca_intermediate {
    local CA_INTERMEDIATE_PASS="${1}"
    local CA_ROOT_PASS="${2}"
    local AUTHORITY="${3}"

    # Correctly set certificate CN (env var is read inside the openssl configuration file)
    export OPENSSL_CN=ca_intermediate_${AUTHORITY}
    pki_logger "OPENSSL_CN : ${OPENSSL_CN}"
    # Correctly set certificate DIRECTORY (env var is read inside the openssl configuration file)
    export OPENSSL_CA_DIR=${AUTHORITY}
    pki_logger "OPENSSL_CA_DIR : ${OPENSSL_CA_DIR}"

    local CA_DIR=${CA_DIR}/${OPENSSL_CA_DIR}
    if [ ! -d ${CA_DIR} ]; then
        pki_logger "Create directory ${OPENSSL_CA_DIR}"
        mkdir -p ${CA_DIR};
    fi

    pki_logger "Create CA-intermediate request..."
    openssl req \
        -config ${CONFIG_DIR}/ca-config \
        -new \
        -newkey ${CRYPTO_SPEC} \
        -out ${CA_DIR}/ca-intermediate.req \
        -keyout ${CA_DIR}/ca-intermediate.key \
        -passout pass:${CA_INTERMEDIATE_PASS} \
        -batch

    pki_logger "Sign CA-intermediate certificate..."
    openssl ca \
        -config ${CONFIG_DIR}/ca-config \
        -extensions extension_ca_intermediate \
        -in ${CA_DIR}/ca-intermediate.req \
        -passin pass:${CA_ROOT_PASS} \
        -out ${CA_DIR}/ca-intermediate.crt \
        -batch

    pki_logger "Convert CA-intermediate certificate to PEM format..."
    openssl x509 \
        -in ${CA_DIR}/ca-intermediate.crt \
        -out ${CA_DIR}/ca-intermediate.pem \
        -outform PEM
}

# Initialize CA configuration
function init_config_ca {
    local CA_DIR="${1}"

    # Suppression de la configuration existante.
    rm -Rf "${CONFIG_DIR}/${CA_DIR}"
    mkdir -p "${CONFIG_DIR}/${CA_DIR}"
    touch "${CONFIG_DIR}/${CA_DIR}/index.txt"
    echo '01' > "${CONFIG_DIR}/${CA_DIR}/serial"
    touch "${CONFIG_DIR}/${CA_DIR}/crlnumber"
}

function get_autorities() {
    # To override
    echo ""
}

################################################################################
##################################    Main    ##################################
################################################################################

function main() {

    # FIXME Why ? it seems to be related to the variable 'dir' set in the configuration of certificates.
    cd $(dirname $0)/../..
    init

    ERASE="false"

    if [ "$#" -gt 0 ]; then
        if [ "${1,,}" == "true" ]; then
            ERASE="true"
        fi
    fi

    pki_logger "Input parameters:"
    pki_logger "    -> Erase existing CAs: ${ERASE}"

    # Cleaning or creating vault file for CA
    initVault   ca    ${ERASE}

    if [ "${ERASE}" == "true" ]; then
        if [ -d ${CA_DIR} ]; then
            # We remove all generated CA
            find "${CA_DIR}/" -mindepth 1 -maxdepth 1 -type d -exec rm -Rf {} \;
        fi
        if [ -d ${CONFIG_DIR} ]; then
            # We remove all configurations linked to CA (except main config files)
            find "${CONFIG_DIR}/" -mindepth 1 -maxdepth 1 -type d -exec rm -Rf {} \;
        fi
    fi

    pki_logger "Starting CA creation process"
    pki_logger "=============================================="
    if [ ! -d ${CA_DIR} ]; then
        pki_logger "Directory ${CA_DIR} does not exist, creating it..."
        mkdir -p ${CA_DIR};
    fi
    if [ ! -d ${TEMP_CERTS} ]; then
        pki_logger "Directory ${TEMP_CERTS} does not exist, creating it..."
        mkdir -p ${TEMP_CERTS}
    fi

    # Create CA per authorities
    AUTHORITIES="$(get_autorities)"
    for AUTHORITY in ${AUTHORITIES[@]}
    do
        mkdir -p ${CA_DIR}/${AUTHORITY}
        init_config_ca ${AUTHORITY}

        if [ ! -f ${CA_DIR}/${AUTHORITY}/ca-root.crt ]; then
            pki_logger "Creation of CA-root for ${AUTHORITY}..."
            # Generate CA_ROOT_PASS & store it in the vault-ca
            CA_ROOT_PASS=$(generatePassphrase)
            setComponentPassphrase ca "ca_root_${AUTHORITY}" "${CA_ROOT_PASS}"
            generate_ca_root ${CA_ROOT_PASS} ${AUTHORITY}
        else
            pki_logger "CA-root for ${AUTHORITY} already exists, it will not be recreated..."
        fi
        if [ ! -f ${CA_DIR}/${AUTHORITY}/ca-intermediate.crt ]; then
            pki_logger "Creation of CA-intermediate for ${AUTHORITY}..."
            # Generate CA_INTERMEDIATE_PASS & store it in the vault-ca
            CA_INTERMEDIATE_PASS=$(generatePassphrase)
            setComponentPassphrase ca "ca_intermediate_${AUTHORITY}" "${CA_INTERMEDIATE_PASS}"
            generate_ca_intermediate ${CA_INTERMEDIATE_PASS} ${CA_ROOT_PASS} ${AUTHORITY}

            purge_directory "${CONFIG_DIR}/${AUTHORITY}"
            purge_directory "${CA_DIR}/${AUTHORITY}"
        else
            pki_logger "CA-intermediate for ${AUTHORITY} already exists, it will not be recreated..."
        fi
        pki_logger "----------------------------------------------"
    done
    if [ -d ${TEMP_CERTS} ]; then
        pki_logger "=============================================="
        pki_logger "Cleaning of temporary tempcerts directories"
        rm -Rf ${TEMP_CERTS}
    fi
    pki_logger "=============================================="
    pki_logger "End of CA creation procedure"
}
