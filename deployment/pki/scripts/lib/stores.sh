#!/usr/bin/env bash
set -e

################################################################################
################################## Includes  ###################################
################################################################################

. "$(dirname $0)/pki/scripts/lib/commons.sh"

################################################################################
################################## Functions ###################################
################################################################################

# Generate a truststore for a given authority
function generateTruststore {
    local AUTHORITY_PATH=${1}
    local AUTHORITY_NAME=${2}

    local TRUSTSTORE_PATH="${KEYSTORES_DIRECTORY}/${AUTHORITY_NAME}/truststore_${AUTHORITY_NAME}.p12"
    local TRUSTSTORE_PASSWORD=$(getOrSetPassphrase truststores "${AUTHORITY_NAME}")

    if [ -f "${TRUSTSTORE_PATH}" ]; then
        rm -vf "${TRUSTSTORE_PATH:?}"
    fi

    # Loop through all .crt files in the current directory
    for CRT_FILE in "${AUTHORITY_PATH}"/*.crt; do
        # Use the filename (minus extension) as the alias
        local ALIAS="$(basename "${CRT_FILE}" .crt)"

        pki_logger "Importing ${CRT_FILE} as ${ALIAS} in $(basename "${TRUSTSTORE_PATH}")"

        mkdir -p "$(dirname "${TRUSTSTORE_PATH}")"
        keytool -importcert -trustcacerts \
            -file "${CRT_FILE}" \
            -alias "${ALIAS}" \
            -keystore "${TRUSTSTORE_PATH}" \
            -storepass "${TRUSTSTORE_PASSWORD}" \
            -storetype PKCS12 \
            -noprompt
    done

}

function generateKeystore {
    local CERTIFICATE_DIR="${1}"
    local COMPONENT="$(basename ${CERTIFICATE_DIR})"
    local CRT_KEY_PASSWORD="${2}"
    local KEYSTORE_PATH="${3}"
    local KEYSTORE_PASSWORD="${4}"

    if [ -f ${KEYSTORE_PATH} ]; then
        rm -vf ${KEYSTORE_PATH:?}
    fi

    pki_logger "Generate keystore: ${KEYSTORE_PATH}"

    mkdir -p "$(dirname "${KEYSTORE_PATH}")"
    openssl pkcs12 -export \
        -inkey "${CERTIFICATE_DIR}/${COMPONENT}.key" \
        -in "${CERTIFICATE_DIR}/${COMPONENT}.crt" \
        -name "${COMPONENT}" \
        -passin pass:"${CRT_KEY_PASSWORD}" \
        -out "${KEYSTORE_PATH}" \
        -passout pass:"${KEYSTORE_PASSWORD}"

}

################################################################################
##################################    Main    ##################################
################################################################################

function main() {
    cd $(dirname $0)
    init
    ERASE="false"

    if [ "$#" -gt 0 ]; then
        if [ "${1,,}" == "true" ]; then
            ERASE="true"
        fi
    fi

    pki_logger "Input parameters:"
    pki_logger "    -> Overwrite keystores: ${ERASE}"

    KEYSTORES_DIRECTORY="${REPERTOIRE_ROOT}/environments/keystores"

    if [ ! -d ${KEYSTORES_DIRECTORY} ]; then
        pki_logger "Directory ${KEYSTORES_DIRECTORY} does not exist, creating it..."
        mkdir -p ${KEYSTORES_DIRECTORY};
    fi

    # We create vault files if they don't exist.
    initVault   keystores   ${ERASE}

    # Remove old keystores clients & server directories
    find ${KEYSTORES_DIRECTORY:?} -mindepth 1 -maxdepth 1 -type d -exec rm -vrf {} \; #TODO: pk on supprime tout si on a pas mis le erase à true ?

    # For each authorities under environments/certs directory (client-vitam, client-external, vitamui-services)
    for AUTHORITY_PATH in $( ls -d ${CERTIFICATE_DIR}/* ); do
        pki_logger "-------------------------------------------"
        local AUTHORITY_NAME=$(basename ${AUTHORITY_PATH})
        pki_logger "Creating keystores for AUTHORITY: ${AUTHORITY_NAME}"

        # Could be clients or servers
        for TYPE_PATH in $( ls -d ${AUTHORITY_PATH}/{ca,clients,servers} 2>/dev/null || true ); do
            local TYPE_NAME=$(basename ${TYPE_PATH})

            if [ "${TYPE_NAME}" == "ca" ]; then
                # Generate truststore for CA certificates
                pki_logger "Generating truststore for CA certificates: ${AUTHORITY_NAME}"
                generateTruststore "${TYPE_PATH}" "${AUTHORITY_NAME}"
                continue
            fi

            pki_logger "Creating keystores for TYPE: ${AUTHORITY_NAME}/${TYPE_NAME}"

            # Generate keystore for each components except for ui-
            for COMPONENT in $( ls ${TYPE_PATH} | grep -v -e "README" -e "^ui-" ); do
                pki_logger "Creating keystore for COMPONENT: ${AUTHORITY_NAME}/${TYPE_NAME}/${COMPONENT}"

                local COMPONENT_CRT_DIR=${CERTIFICATE_DIR}/${AUTHORITY_NAME}/${TYPE_NAME}/${COMPONENT}
                local TARGET_KEYSTORE=${KEYSTORES_DIRECTORY}/${AUTHORITY_NAME}/${TYPE_NAME}/keystore_${COMPONENT}.p12
                local CRT_KEY_PASSWORD=$(getPassphrase certs "${AUTHORITY_NAME}_${TYPE_NAME}_${COMPONENT}")
                local KEYSTORE_PASSWORD=$(getOrSetPassphrase keystores "${AUTHORITY_NAME}_${TYPE_NAME}_${COMPONENT}")

                generateKeystore    "${COMPONENT_CRT_DIR}" \
                                    "${CRT_KEY_PASSWORD}" \
                                    "${TARGET_KEYSTORE}" \
                                    "${KEYSTORE_PASSWORD}"

            done
        done
    done

    pki_logger "-------------------------------------------"
    pki_logger "End of stores generation"

}
