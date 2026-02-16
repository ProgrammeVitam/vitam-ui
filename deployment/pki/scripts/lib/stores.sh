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
    local AUTHORITY_NAME=${1}

    pki_logger "Creating truststore for CA certificates: ${AUTHORITY_NAME}"

    local AUTHORITY_PATH="${CERTIFICATE_DIR}/${AUTHORITY_NAME}/ca"
    local TRUSTSTORE_PATH="${KEYSTORES_DIRECTORY}/${AUTHORITY_NAME}/truststore_${AUTHORITY_NAME}.p12"
    local TRUSTSTORE_PASSWORD=$(setPassphrase truststores "${AUTHORITY_NAME}")

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

# Generate a keystore for a given component with a given authority and type
function generateKeystore {
    local AUTHORITY_NAME="${1}"
    local TYPE_NAME="${2}"
    local COMPONENT="${3}"

    pki_logger "Creating keystore for COMPONENT: ${AUTHORITY_NAME}/${TYPE_NAME}/${COMPONENT}"

    local COMPONENT_CRT_DIR=${CERTIFICATE_DIR}/${AUTHORITY_NAME}/${TYPE_NAME}/${COMPONENT}
    local TARGET_KEYSTORE=${KEYSTORES_DIRECTORY}/${AUTHORITY_NAME}/${TYPE_NAME}/keystore_${COMPONENT}.p12
    local CRT_KEY_PASSWORD=$(getPassphrase certs "${AUTHORITY_NAME}_${TYPE_NAME}_${COMPONENT}")
    local KEYSTORE_PASSWORD=$(setPassphrase keystores "${AUTHORITY_NAME}_${TYPE_NAME}_${COMPONENT}")

    if [ -f ${TARGET_KEYSTORE} ]; then
        rm -vf ${TARGET_KEYSTORE:?}
    fi

    mkdir -p "$(dirname "${TARGET_KEYSTORE}")"
    openssl pkcs12 -export \
        -inkey "${COMPONENT_CRT_DIR}/${COMPONENT}.key" \
        -in "${COMPONENT_CRT_DIR}/${COMPONENT}.crt" \
        -name "${COMPONENT}" \
        -passin pass:"${CRT_KEY_PASSWORD}" \
        -out "${TARGET_KEYSTORE}" \
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
    find ${KEYSTORES_DIRECTORY:?} -mindepth 1 -maxdepth 1 -type d -exec rm -vrf {} \;

    # Generate stores for each authorities
    for AUTHORITY_NAME in $(get_autorities); do
        AUTHORITY_PATH="${CERTIFICATE_DIR}/${AUTHORITY_NAME}"

        # Verify the directory exists before processing
        if [ -d "$AUTHORITY_PATH" ]; then
            pki_logger "-------------------------------------------"
            pki_logger "Creating keystores or truststore for AUTHORITY: ${AUTHORITY_NAME}"

            # Could be ca, clients or servers
            for TYPE_PATH in $( ls -d ${AUTHORITY_PATH}/{ca,clients,servers} 2>/dev/null || true ); do
                local TYPE_NAME=$(basename ${TYPE_PATH})

                if [ "${TYPE_NAME}" == "ca" ]; then
                    # Generate truststore for CA certificates
                    generateTruststore "${AUTHORITY_NAME}"
                    continue
                fi

                pki_logger "Creating keystores for TYPE: ${AUTHORITY_NAME}/${TYPE_NAME}"

                # Generate keystore for each components except for ui- & reverse
                for COMPONENT in $( ls ${TYPE_PATH} | grep -v -e "README" -e "^ui-" -e "reverse" ); do
                    generateKeystore    "${AUTHORITY_NAME}" \
                                        "${TYPE_NAME}" \
                                        "${COMPONENT}"
                done
            done
        else
            pki_logger "Skipping: $AUTHORITY_PATH not found"
        fi
    done

    pki_logger "-------------------------------------------"
    pki_logger "End of stores generation procedure"

}
