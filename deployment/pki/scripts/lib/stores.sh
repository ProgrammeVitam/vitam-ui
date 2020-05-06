#!/usr/bin/env bash
set -e

######################################################################
############################# Includes  ##############################
######################################################################

. "$(dirname $0)/pki/scripts/lib/commons.sh"

######################################################################
############################# Functions ##############################
######################################################################

# Pour incorporer un certificat dans un store
function addCrtInJks {
    local STORE="${1}"
    local MDP_STORE="${2}"
    local CERTIFICAT="${3}"
    local ALIAS="${4}"

    keytool -import -keystore ${STORE} \
        -file ${CERTIFICAT} \
        -storepass ${MDP_STORE} \
        -keypass ${MDP_STORE} \
        -noprompt \
        -alias ${ALIAS}
}

# Pour incorporer une CA dans un store
function addCaInJks {
    local STORE="${1}"
    local MDP_STORE="${2}"
    local CERTIFICAT="${3}"
    local ALIAS="${4}"

    keytool -import -trustcacerts -keystore ${STORE} \
        -file ${CERTIFICAT} \
        -storepass ${MDP_STORE} \
        -keypass ${MDP_STORE} \
        -noprompt \
        -alias ${ALIAS}
}

# Génération d'un p12 et d'un pem depuis un certificat
function crtKeyToP12 {
    local BASEFILE="${1}"
    local MDP_KEY="${2}"
    local KEYPAIR_NAME="${3}"
    local MDP_P12="${4}"
    local TARGET_FILE="${5}"

    openssl pkcs12 -export \
        -inkey "${BASEFILE}/${KEYPAIR_NAME}.key" \
        -in "${BASEFILE}/${KEYPAIR_NAME}.crt" \
        -name "${KEYPAIR_NAME}" \
        -passin pass:"${MDP_KEY}" \
        -out "${BASEFILE}/${KEYPAIR_NAME}.p12" \
        -passout pass:"${MDP_P12}"

    if [ "${BASEFILE}/${KEYPAIR_NAME}.p12" != "${TARGET_FILE}" ]; then
        mkdir -p $(dirname ${TARGET_FILE})
        mv "${BASEFILE}/${KEYPAIR_NAME}.p12" "${TARGET_FILE}"
    fi
}

# Pour incorporer un certificat p12 dans un keystore jks
function addP12InJks {
    local JKS_KEYSTORE="${1}"
    local JKS_KEYSTORE_PASSWORD="${2}"
    local P12_KEYSTORE="${3}"
    local P12_STORE_PASSWORD="${4}"

    mkdir -p "$(dirname ${JKS_KEYSTORE})"

    keytool -importkeystore \
        -srckeystore ${P12_KEYSTORE} -srcstorepass ${P12_STORE_PASSWORD} -srcstoretype PKCS12 \
        -destkeystore ${JKS_KEYSTORE} -storepass ${JKS_KEYSTORE_PASSWORD} \
        -keypass ${JKS_KEYSTORE_PASSWORD} -deststorepass ${JKS_KEYSTORE_PASSWORD} \
        -destkeypass ${JKS_KEYSTORE_PASSWORD} -deststoretype JKS
}

# Renvoie la clé du keystore pour un composant donné
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

# Generate a trustore
function generateTrustStore {
    local TRUSTORE_TYPE=${1}
    local CLIENT_TYPE=${2}

    if [ "${TRUSTORE_TYPE}" != "server" ] && [ ${TRUSTORE_TYPE} != "client" ]; then
        pki_logger "ERROR" "Invalid trustore type: ${TRUSTORE_TYPE}"
        return 1
    fi

    # Set truststore path and delete the store if already exists
    if [ "${TRUSTORE_TYPE}" == "client" ]; then
        JKS_TRUST_STORE=${REPERTOIRE_KEYSTORES}/client-${CLIENT_TYPE}/truststore_${CLIENT_TYPE}.jks
        TRUST_STORE_PASSWORD=$(getKeystorePassphrase "truststores_client_${CLIENT_TYPE}")
    elif [ "${TRUSTORE_TYPE}" == "server" ]; then
        JKS_TRUST_STORE=${REPERTOIRE_KEYSTORES}/server/truststore_server.jks
        TRUST_STORE_PASSWORD=$(getKeystorePassphrase "truststores_server")
    else
        pki_logger "ERROR" "Invalid trustore type: ${TRUSTORE_TYPE}"
        return 1
    fi

    if [ -f "${JKS_TRUST_STORE}" ]; then
        rm -f "${JKS_TRUST_STORE}"
    fi

    # Add the public client ca certificates to the truststore
    pki_logger "Ajout des certificats client dans le truststore"
    if [ "${TRUSTORE_TYPE}" == "client" ]; then

        for CRT_FILE in $(ls ${REPERTOIRE_CERTIFICAT}/client-${CLIENT_TYPE}/ca/*.crt); do
            pki_logger "Ajout de ${CRT_FILE} dans le truststore ${CLIENT_TYPE}"
            ALIAS="client-${CLIENT_TYPE}-$(basename ${CRT_FILE})"
            addCrtInJks ${JKS_TRUST_STORE} \
                        ${TRUST_STORE_PASSWORD} \
                        ${CRT_FILE} \
                        ${ALIAS}
        done

    fi

    # Add the server certificates to the truststore
    pki_logger "Ajout des certificats serveur dans le truststore"
    for CRT_FILE in  $(ls ${REPERTOIRE_CERTIFICAT}/server/ca/*.crt); do
        pki_logger "Ajout de ${CRT_FILE} dans le truststore ${CLIENT_TYPE}"
        ALIAS="server-$(basename ${CRT_FILE})"
        addCrtInJks ${JKS_TRUST_STORE} \
                    ${TRUST_STORE_PASSWORD} \
                    ${CRT_FILE} \
                    ${ALIAS}
    done
}

function generateHostKeystore {
    local COMPONENT="${1}"
    local JKS_KEYSTORE="${2}"
    local P12_KEYSTORE="${3}"
    local CRT_KEY_PASSWORD="${4}"
    local JKS_PASSWORD="${5}"
    local TMP_P12_PASSWORD="${6}"

    if [ -f ${JKS_KEYSTORE} ]; then
        rm -f ${JKS_KEYSTORE}
    fi

    pki_logger "Génération du p12"
    crtKeyToP12 $(dirname ${P12_KEYSTORE}) \
                ${CRT_KEY_PASSWORD} \
                ${COMPONENT} \
                ${TMP_P12_PASSWORD} \
                ${P12_KEYSTORE}

    pki_logger "Génération du jks"
    addP12InJks ${JKS_KEYSTORE} \
                ${JKS_PASSWORD} \
                ${P12_KEYSTORE} \
                ${TMP_P12_PASSWORD}

    pki_logger "Suppression du p12"
    if [ -f ${P12_KEYSTORE} ]; then
        rm -f ${P12_KEYSTORE}
    fi
}

######################################################################
#############################    Main    #############################
######################################################################

function main() {
    cd $(dirname $0)
    init
    ERASE="false"

    if [ "$#" -gt 0 ]; then
        if [ "${1,,}" == "true" ]; then
            ERASE="true"
        fi
    fi

    pki_logger "Paramètres d'entrée:"
    pki_logger "    -> Ecraser la configuration des keystores/PKI: ${ERASE}"

    TMP_P12_PASSWORD="$(generatePassphrase)"
    REPERTOIRE_KEYSTORES="${REPERTOIRE_ROOT}/environments/keystores"

    if [ ! -d ${REPERTOIRE_KEYSTORES} ]; then
        pki_logger "Création du répertoire des keystores ..."
        mkdir -p ${REPERTOIRE_KEYSTORES};
    fi

    # We create vault files if they don't exist.
    initVault   keystores   ${ERASE}

    # Remove old keystores & servers directories
    find ${REPERTOIRE_KEYSTORES} -mindepth 1 -maxdepth 1 -type d -exec rm -rf {} \;

    # Generate the server keystores
    for SERVER in $(ls ${REPERTOIRE_CERTIFICAT}/server/hosts/); do

        mkdir -p ${REPERTOIRE_KEYSTORES}/server/${SERVER}

        # awk : used to strip extension
        for COMPONENT in $( ls ${REPERTOIRE_CERTIFICAT}/server/hosts/${SERVER}/ 2>/dev/null | awk -F "." '{for (i=1;i<NF;i++) print $i}' | sort | uniq ); do

            pki_logger "-------------------------------------------"
            pki_logger "Creation du keystore de ${COMPONENT} pour le serveur ${SERVER}"
            JKS_KEYSTORE=${REPERTOIRE_KEYSTORES}/server/${SERVER}/keystore_${COMPONENT}.jks
            P12_KEYSTORE=${REPERTOIRE_CERTIFICAT}/server/hosts/${SERVER}/${COMPONENT}.p12
            CRT_KEY_PASSWORD=$(getComponentPassphrase certs "server_${COMPONENT}_key")
            JKS_PASSWORD=$(getKeystorePassphrase "keystores_server_${COMPONENT}")

            generateHostKeystore    ${COMPONENT} \
                                    ${JKS_KEYSTORE} \
                                    ${P12_KEYSTORE} \
                                    ${CRT_KEY_PASSWORD} \
                                    ${JKS_PASSWORD} \
                                    ${TMP_P12_PASSWORD}
        done

    done

    # Keystores generation foreach client type (storage, external)
    # for CLIENT_TYPE in external storage; do
    for CLIENT_TYPE in external vitam; do

        # # Set grantedstore path and delete the store if already exists
        # JKS_GRANTED_STORE=${REPERTOIRE_KEYSTORES}/client-${CLIENT_TYPE}/grantedstore_${CLIENT_TYPE}.jks
        # GRANTED_STORE_PASSWORD=$(getKeystorePassphrase "grantedstores_client_${CLIENT_TYPE}")

        # # Delete the old granted store if already exists
        # if [ -f ${JKS_GRANTED_STORE} ]; then
        #     rm -f ${JKS_GRANTED_STORE}
        # fi
        mkdir -p ${REPERTOIRE_KEYSTORES}/client-${CLIENT_TYPE}
        # # client-${CLIENT_TYPE} keystores generation
        for COMPONENT in $( ls ${REPERTOIRE_CERTIFICAT}/client-${CLIENT_TYPE}/clients 2>/dev/null | grep -v "^external$"); do

            # Generate the p12 keystore
            pki_logger "-------------------------------------------"
            pki_logger "Creation du keystore client de ${COMPONENT}"
            CERT_DIRECTORY=${REPERTOIRE_CERTIFICAT}/client-${CLIENT_TYPE}/clients/${COMPONENT}
            CRT_KEY_PASSWORD=$(getComponentPassphrase certs "client_client-${CLIENT_TYPE}_${COMPONENT}_key")
            P12_KEYSTORE=${REPERTOIRE_KEYSTORES}/client-${CLIENT_TYPE}/keystore_${COMPONENT}.p12
            P12_PASSWORD=$(getKeystorePassphrase "keystores_client_${CLIENT_TYPE}_${COMPONENT}")

            if [ -f ${P12_KEYSTORE} ]; then
                rm -f ${P12_KEYSTORE}
            fi

            pki_logger "Génération du p12"
            crtKeyToP12 ${CERT_DIRECTORY} \
                        ${CRT_KEY_PASSWORD} \
                        ${COMPONENT} \
                        ${P12_PASSWORD} \
                        ${P12_KEYSTORE}
        done

        # Generate the CLIENT_TYPE truststore
        pki_logger "-------------------------------------------"
        pki_logger "Génération du truststore client-${CLIENT_TYPE}"
        generateTrustStore "client" ${CLIENT_TYPE}

    done

    # Generate the server trustore
    pki_logger "-------------------------------------------"
    pki_logger "Génération du truststore server"
    generateTrustStore "server" "server"

    pki_logger "-------------------------------------------"
    pki_logger "Fin de la génération des stores"

}