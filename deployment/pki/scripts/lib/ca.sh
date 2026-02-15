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
    local AUTHORITY_NAME="${1}"

    pki_logger "Creating CA-root for authority ${AUTHORITY_NAME}..."

    # set passphrase for ca-root and store it in the vault-ca
    local CA_ROOT_PASS=$(setPassphrase ca "ca_root_${AUTHORITY_NAME}")

    # Correctly set certificate CN (env var is read inside the openssl configuration file)
    export OPENSSL_CN="ca-root_${AUTHORITY_NAME}"
    pki_logger "OPENSSL_CN : ${OPENSSL_CN}"
    # Correctly set certificate DIRECTORY (env var is read inside the openssl configuration file)
    export OPENSSL_CA_DIR="${AUTHORITY_NAME}"
    pki_logger "OPENSSL_CA_DIR : ${OPENSSL_CA_DIR}"

    local CA_DIR=${CA_DIR}/${OPENSSL_CA_DIR}
    if [ ! -d ${CA_DIR} ]; then
        pki_logger "Creating directory ${CA_DIR}"
        mkdir -p ${CA_DIR};
    fi

    pki_logger "Creating CA-root request for authority ${AUTHORITY_NAME}..."
    openssl req \
        -config ${CONFIG_DIR}/ca-config \
        -new \
        -out ${CA_DIR}/ca-root.req \
        -keyout ${CA_DIR}/ca-root.key \
        -passout pass:${CA_ROOT_PASS} \
        -batch

    pki_logger "Signing CA-root certificate for authority ${AUTHORITY_NAME}..."
    openssl ca \
        -config ${CONFIG_DIR}/ca-config \
        -selfsign \
        -extensions extension_ca_root \
        -in ${CA_DIR}/ca-root.req \
        -passin pass:${CA_ROOT_PASS} \
        -out ${CA_DIR}/ca-root.crt \
        -batch
}

# Generate intermediate CA
function generate_ca_intermediate {
    local AUTHORITY_NAME="${1}"

    pki_logger "Creating CA-intermediate for authority ${AUTHORITY_NAME}..."

    # get passphrase for ca-root from the vault-ca
    local CA_ROOT_PASS=$(getPassphrase ca "ca_root_${AUTHORITY_NAME}")
    # set passphrase for ca-intermediate and store it in the vault-ca
    local CA_INTERMEDIATE_PASS=$(setPassphrase ca "ca_intermediate_${AUTHORITY_NAME}")

    # Correctly set certificate CN (env var is read inside the openssl configuration file)
    export OPENSSL_CN="ca-intermediate_${AUTHORITY_NAME}"
    pki_logger "OPENSSL_CN : ${OPENSSL_CN}"
    # Correctly set certificate DIRECTORY (env var is read inside the openssl configuration file)
    export OPENSSL_CA_DIR=${AUTHORITY_NAME}
    pki_logger "OPENSSL_CA_DIR : ${OPENSSL_CA_DIR}"

    local CA_DIR=${CA_DIR}/${OPENSSL_CA_DIR}
    if [ ! -d ${CA_DIR} ]; then
        pki_logger "Creating directory ${OPENSSL_CA_DIR}"
        mkdir -p ${CA_DIR};
    fi

    pki_logger "Creating CA-intermediate request for authority ${AUTHORITY_NAME}..."
    openssl req \
        -config ${CONFIG_DIR}/ca-config \
        -new \
        -newkey ${CRYPTO_SPEC} \
        -out ${CA_DIR}/ca-intermediate.req \
        -keyout ${CA_DIR}/ca-intermediate.key \
        -passout pass:${CA_INTERMEDIATE_PASS} \
        -batch

    pki_logger "Signing CA-intermediate certificate for authority ${AUTHORITY_NAME}..."
    openssl ca \
        -config ${CONFIG_DIR}/ca-config \
        -extensions extension_ca_intermediate \
        -in ${CA_DIR}/ca-intermediate.req \
        -passin pass:${CA_ROOT_PASS} \
        -out ${CA_DIR}/ca-intermediate.crt \
        -batch
}

# Initialize CA configuration
function init_config_ca {
    local CA_DIR="${1}"

    # Deleting existing configuration but fail if variables are undefined
    rm -vRf "${CONFIG_DIR:?}/${CA_DIR:?}"
    mkdir -p "${CONFIG_DIR}/${CA_DIR}"
    touch "${CONFIG_DIR}/${CA_DIR}/index.txt"
    echo '01' > "${CONFIG_DIR}/${CA_DIR}/serial"
    touch "${CONFIG_DIR}/${CA_DIR}/crlnumber"
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
            find "${CA_DIR:?}/" -mindepth 1 -maxdepth 1 -type d -exec rm -vRf {} \;
        fi
        if [ -d ${CONFIG_DIR} ]; then
            # We remove all configurations linked to CA (except main config files)
            find "${CONFIG_DIR:?}/" -mindepth 1 -maxdepth 1 -type d -exec rm -vRf {} \;
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
    for AUTHORITY_NAME in $(get_autorities); do
        mkdir -p ${CA_DIR}/${AUTHORITY_NAME}
        init_config_ca ${AUTHORITY_NAME}

        if [ ! -f ${CA_DIR}/${AUTHORITY_NAME}/ca-root.crt ]; then
            # Generate ca-root for authority & store passphrase in the vault-ca
            generate_ca_root ${AUTHORITY_NAME}
        else
            pki_logger "CA-root for authority ${AUTHORITY_NAME} already exists, it will not be recreated..."
        fi
        if [ ! -f ${CA_DIR}/${AUTHORITY_NAME}/ca-intermediate.crt ]; then
            # Generate ca-intermediate for authority & store passphrase in the vault-ca
            generate_ca_intermediate ${AUTHORITY_NAME}
            purge_directory "${CONFIG_DIR}/${AUTHORITY_NAME}"
            purge_directory "${CA_DIR}/${AUTHORITY_NAME}"
        else
            pki_logger "CA-intermediate for authority ${AUTHORITY_NAME} already exists, it will not be recreated..."
        fi
        pki_logger "----------------------------------------------"
    done
    if [ -d ${TEMP_CERTS} ]; then
        pki_logger "=============================================="
        pki_logger "Cleaning of temporary tempcerts directories"
        rm -vRf ${TEMP_CERTS:?}
    fi
    pki_logger "=============================================="
    pki_logger "End of CA creation procedure"

}
