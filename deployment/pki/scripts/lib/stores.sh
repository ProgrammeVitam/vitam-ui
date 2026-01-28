#!/usr/bin/env bash
set -e

################################################################################
################################## Includes  ###################################
################################################################################

. "$(dirname $0)/pki/scripts/lib/commons.sh"

################################################################################
################################## Functions ###################################
################################################################################

# Import a certificate into a keystore
function addCrtInKeystore {
    local STORE="${1}"
    local MDP_STORE="${2}"
    local CERTIFICAT="${3}"
    local ALIAS="${4}"

    keytool -import -keystore ${STORE} \
        -storetype PKCS12 \
        -file ${CERTIFICAT} \
        -storepass ${MDP_STORE} \
        -keypass ${MDP_STORE} \
        -noprompt \
        -alias ${ALIAS}
}

# Import a CA certificate into a keystore
function addCaInKeystore {
    local STORE="${1}"
    local MDP_STORE="${2}"
    local CERTIFICAT="${3}"
    local ALIAS="${4}"

    keytool -import -trustcacerts -keystore ${STORE} \
        -storetype PKCS12 \
        -file ${CERTIFICAT} \
        -storepass ${MDP_STORE} \
        -keypass ${MDP_STORE} \
        -noprompt \
        -alias ${ALIAS}
}

# Generate a p12 and a pem from a certificate
function crtKeyToP12 {
    local BASEFILE="${1}"
    local KEY_PASS="${2}"
    local KEYPAIR_NAME="${3}"
    local MDP_P12="${4}"
    local TARGET_FILE="${5}"

    openssl pkcs12 -export \
        -inkey "${BASEFILE}/${KEYPAIR_NAME}.key" \
        -in "${BASEFILE}/${KEYPAIR_NAME}.crt" \
        -name "${KEYPAIR_NAME}" \
        -passin pass:"${KEY_PASS}" \
        -out "${BASEFILE}/${KEYPAIR_NAME}.p12" \
        -passout pass:"${MDP_P12}"

    if [ "${BASEFILE}/${KEYPAIR_NAME}.p12" != "${TARGET_FILE}" ]; then
        mkdir -p $(dirname ${TARGET_FILE})
        mv -v "${BASEFILE}/${KEYPAIR_NAME}.p12" "${TARGET_FILE}"
    fi
}

# Import a p12 certificate into a keystore
function addP12InKeystore {
    local KEYSTORE="${1}"
    local KEYSTORE_PASSWORD="${2}"
    local P12_KEYSTORE="${3}"
    local P12_STORE_PASSWORD="${4}"

    mkdir -p "$(dirname ${KEYSTORE})"

    keytool -importkeystore \
        -srckeystore ${P12_KEYSTORE} -srcstorepass ${P12_STORE_PASSWORD} -srcstoretype PKCS12 \
        -destkeystore ${KEYSTORE} -storepass ${KEYSTORE_PASSWORD} \
        -keypass ${KEYSTORE_PASSWORD} -deststorepass ${KEYSTORE_PASSWORD} \
        -destkeypass ${KEYSTORE_PASSWORD} -deststoretype PKCS12
}

# Get the keystore passphrase for a given component
function getKeystorePassphrase {
    local KEY="${1}"
    local RETURN_CODE=0

    local EXISTS=$(hasComponentPassphrase "keystores" "${KEY}")
    if [ "${EXISTS}" == "false" ]; then
        # We generate a random key
        local PASSPHRASE=$(generatePassphrase)
        setComponentPassphrase keystores "${KEY}" "${PASSPHRASE}"
        echo "${PASSPHRASE}"
    else
        echo $(getComponentPassphrase "keystores" "${KEY}")
    fi
}

# Generate a truststore
function generateTrustStore {
    local TRUSTORE_TYPE=${1}
    local CLIENT_TYPE=${2}

    if [ "${TRUSTORE_TYPE}" != "vitamui-services" ] && [ ${TRUSTORE_TYPE} != "client" ]; then
        pki_logger "ERROR" "Invalid trustore type: ${TRUSTORE_TYPE}"
        return 1
    fi

    # Set truststore path and delete the store if already exists
    if [ "${TRUSTORE_TYPE}" == "client" ]; then
        TRUST_STORE=${REPERTOIRE_KEYSTORES}/client-${CLIENT_TYPE}/truststore_${CLIENT_TYPE}.p12
        TRUST_STORE_PASSWORD=$(getKeystorePassphrase "truststores_client_${CLIENT_TYPE}")
    elif [ "${TRUSTORE_TYPE}" == "vitamui-services" ]; then
        TRUST_STORE=${REPERTOIRE_KEYSTORES}/vitamui-services/truststore_vitamui.p12
        TRUST_STORE_PASSWORD=$(getKeystorePassphrase "truststores_vitamui")
    else
        pki_logger "ERROR" "Invalid trustore type: ${TRUSTORE_TYPE}"
        return 1
    fi

    if [ -f "${TRUST_STORE}" ]; then
        rm -vf "${TRUST_STORE}"
    fi

    # Add the public client ca certificates to the truststore
    pki_logger "Add client certificates to the truststore"
    if [ "${TRUSTORE_TYPE}" == "client" ]; then

        if [ "${CLIENT_TYPE}" == "vitamui-services" ]; then
             CLIENT_CA_DIR="${CERTIFICATE_DIR}/${CLIENT_TYPE}/ca"
        else
             CLIENT_CA_DIR="${CERTIFICATE_DIR}/client-${CLIENT_TYPE}/ca"
        fi

        for CRT_FILE in $(ls ${CLIENT_CA_DIR}/*.crt); do
            pki_logger "Add ${CRT_FILE} to the truststore ${CLIENT_TYPE}"
            ALIAS="client-${CLIENT_TYPE}-$(basename ${CRT_FILE})"
            addCrtInKeystore ${TRUST_STORE} \
                            ${TRUST_STORE_PASSWORD} \
                            ${CRT_FILE} \
                            ${ALIAS}
        done

    fi

    # Add the server certificates to the truststore
    pki_logger "Add server certificates to the truststore"
    for CRT_FILE in $(ls ${CERTIFICATE_DIR}/vitamui-services/ca/*.crt); do
        pki_logger "Add ${CRT_FILE} to the truststore ${CLIENT_TYPE}"
        ALIAS="server-$(basename ${CRT_FILE})"
        addCrtInKeystore ${TRUST_STORE} \
                        ${TRUST_STORE_PASSWORD} \
                        ${CRT_FILE} \
                        ${ALIAS}
    done

    # Add the client CA certificates to the server truststore (to trust incoming client certs)
    if [ "${TRUSTORE_TYPE}" == "vitamui-services" ]; then
        pki_logger "Add client CA certificates to the server truststore"
        for CLIENT_CA_TYPE in vitam vitamui-services external; do
             if [ "${CLIENT_CA_TYPE}" == "vitamui-services" ]; then
                CA_DIR="${CERTIFICATE_DIR}/${CLIENT_CA_TYPE}/ca"
             else
                CA_DIR="${CERTIFICATE_DIR}/client-${CLIENT_CA_TYPE}/ca"
             fi

             if [ -d "${CA_DIR}" ]; then
                for CRT_FILE in $(ls ${CA_DIR}/*.crt 2>/dev/null); do
                    pki_logger "Add ${CRT_FILE} to the server truststore"
                    ALIAS="client-${CLIENT_CA_TYPE}-$(basename ${CRT_FILE})"
                    addCrtInKeystore ${TRUST_STORE} \
                                    ${TRUST_STORE_PASSWORD} \
                                    ${CRT_FILE} \
                                    ${ALIAS}
                done
             fi
        done
    fi

    if [ "${DEV_MODE}" == "true" ]; then
        pki_logger "DEV_MODE is true"
        # Add the server certificates to the truststore
        for CRT_FILE in $(find ${CERTIFICATE_DIR}/vitamui-services/server -name "*.crt"); do
            pki_logger "Add ${CRT_FILE} to the truststore ${CLIENT_TYPE}"
            ALIAS="server-$(basename ${CRT_FILE})"
            addCrtInKeystore ${TRUST_STORE} \
                            ${TRUST_STORE_PASSWORD} \
                            ${CRT_FILE} \
                            ${ALIAS}
        done
    fi
}

function generateHostKeystore {
    local COMPONENT="${1}"
    local KEYSTORE="${2}"
    local P12_KEYSTORE="${3}"
    local CRT_KEY_PASSWORD="${4}"
    local KEYSTORE_PASSWORD="${5}"
    local TMP_P12_PASSWORD="${6}"

    if [ -f ${KEYSTORE} ]; then
        rm -f ${KEYSTORE}
    fi

    pki_logger "Generate p12"
    crtKeyToP12 $(dirname ${P12_KEYSTORE}) \
                ${CRT_KEY_PASSWORD} \
                ${COMPONENT} \
                ${TMP_P12_PASSWORD} \
                ${P12_KEYSTORE}

    pki_logger "Generate keystore"
    addP12InKeystore ${KEYSTORE} \
                ${KEYSTORE_PASSWORD} \
                ${P12_KEYSTORE} \
                ${TMP_P12_PASSWORD}

    if [ "${DEV_MODE}" != "true" ]; then
        if [ -f ${P12_KEYSTORE} ]; then
            pki_logger " /!\ Delete p12: ${P12_KEYSTORE}"
            rm -vf ${P12_KEYSTORE}
        fi
    fi
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

    TMP_P12_PASSWORD="$(generatePassphrase)"
    REPERTOIRE_KEYSTORES="${REPERTOIRE_ROOT}/environments/keystores"

    if [ ! -d ${REPERTOIRE_KEYSTORES} ]; then
        pki_logger "Directory ${REPERTOIRE_KEYSTORES} does not exist, creating it..."
        mkdir -p ${REPERTOIRE_KEYSTORES};
    fi

    # We create vault files if they don't exist.
    initVault   keystores   ${ERASE}

    # Remove old keystores & servers directories
    find ${REPERTOIRE_KEYSTORES} -mindepth 1 -maxdepth 1 -type d -exec rm -vrf {} \;

    # Generate the server keystores for vitamui-services except ui- components
    for COMPONENT in $(ls ${CERTIFICATE_DIR}/vitamui-services/server/ | grep -v -e "README" -e "^ui-" ); do

        pki_logger "-------------------------------------------"
        pki_logger "Create keystore for ${COMPONENT}"
        KEYSTORE=${REPERTOIRE_KEYSTORES}/vitamui-services/server/${COMPONENT}/keystore_${COMPONENT}.p12
        P12_KEYSTORE=${CERTIFICATE_DIR}/vitamui-services/server/${COMPONENT}/${COMPONENT}.p12
        CRT_KEY_PASSWORD=$(getComponentPassphrase certs "server_vitamui_services_${COMPONENT}_key")
        KEYSTORE_PASSWORD=$(getKeystorePassphrase "keystores_server_vitamui_services_${COMPONENT}")

        generateHostKeystore ${COMPONENT} \
                             ${KEYSTORE} \
                             ${P12_KEYSTORE} \
                             ${CRT_KEY_PASSWORD} \
                             ${KEYSTORE_PASSWORD} \
                             ${TMP_P12_PASSWORD}
    done

    # Generate client keystores foreach client type
    for CLIENT_TYPE in external vitam vitamui-services; do

        if [ "${CLIENT_TYPE}" == "vitamui-services" ]; then
            STORE_DIR="${REPERTOIRE_KEYSTORES}/${CLIENT_TYPE}/clients"
            CERT_SRC_DIR="${CERTIFICATE_DIR}/${CLIENT_TYPE}/clients"
            KEY_PREFIX="client_${CLIENT_TYPE}"
        else
            STORE_DIR="${REPERTOIRE_KEYSTORES}/client-${CLIENT_TYPE}"
            CERT_SRC_DIR="${CERTIFICATE_DIR}/client-${CLIENT_TYPE}/clients"
            KEY_PREFIX="client_client-${CLIENT_TYPE}"
        fi

        mkdir -p ${STORE_DIR}
        # Do not generate keystores for ui- components, we don't need them
        for COMPONENT in $( ls ${CERT_SRC_DIR} 2>/dev/null | grep -v -e "README" -e "external" -e "^ui-" ); do

            # Generate the p12 keystore
            pki_logger "-------------------------------------------"
            pki_logger "Generate client keystore for ${COMPONENT}"
            CERT_DIRECTORY=${CERT_SRC_DIR}/${COMPONENT}
            CRT_KEY_PASSWORD=$(getComponentPassphrase certs "${KEY_PREFIX}_${COMPONENT}_key")
            if [ "${CLIENT_TYPE}" == "vitamui-services" ]; then
                P12_KEYSTORE=${STORE_DIR}/${COMPONENT}/keystore_${COMPONENT}.p12
            else
                P12_KEYSTORE=${STORE_DIR}/keystore_${COMPONENT}.p12
            fi
            P12_PASSWORD=$(getKeystorePassphrase "keystores_client_${CLIENT_TYPE}_${COMPONENT}")

            if [ "${DEV_MODE}" != "true" ]; then
                if [ -f ${P12_KEYSTORE} ]; then
                    pki_logger " /!\ Delete p12: ${P12_KEYSTORE}"
                    rm -vf ${P12_KEYSTORE}
                fi
            fi

            pki_logger "Generate p12"
            crtKeyToP12 ${CERT_DIRECTORY} \
                        ${CRT_KEY_PASSWORD} \
                        ${COMPONENT} \
                        ${P12_PASSWORD} \
                        ${P12_KEYSTORE}

            pki_logger "Generate keystore for ${COMPONENT}"
            if [ "${CLIENT_TYPE}" == "vitamui-services" ]; then
                KEYSTORE=${STORE_DIR}/${COMPONENT}/keystore_${COMPONENT}.p12
            else
                KEYSTORE=${STORE_DIR}/keystore_${COMPONENT}.p12
            fi
            KEYSTORE_PASSWORD=$(getKeystorePassphrase "keystores_client_${CLIENT_TYPE}_${COMPONENT}")
            addP12InKeystore ${KEYSTORE} \
                        ${KEYSTORE_PASSWORD} \
                        ${P12_KEYSTORE} \
                        ${P12_PASSWORD}
        done

        # Generate the CLIENT_TYPE truststore
        if [ "${CLIENT_TYPE}" != "vitamui-services" ]; then
            pki_logger "-------------------------------------------"
            pki_logger "Génération du truststore client-${CLIENT_TYPE}"
            generateTrustStore "client" ${CLIENT_TYPE}
        fi

    done

    # Generate the vitamui-services trustore
    pki_logger "-------------------------------------------"
    pki_logger "Génération du truststore vitamui-services"
    generateTrustStore "vitamui-services" "vitamui-services"

    pki_logger "-------------------------------------------"
    pki_logger "Fin de la génération des stores"

}
