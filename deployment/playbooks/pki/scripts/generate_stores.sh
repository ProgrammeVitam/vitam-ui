#!/usr/bin/env bash
set -e

######################################################################
############################# Includes  ##############################
######################################################################

. "$(dirname $0)/lib/functions.sh"


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
    local YAML_PATH="${1}"
    local RETURN_CODE=0

    if [ ! -f "${VAULT_KEYSTORES}" ]; then
        return 1
    fi

    # Decrypt vault file
    ansible-vault decrypt ${VAULT_KEYSTORES} ${ANSIBLE_VAULT_PASSWD}
    # Try/catch/finally stuff with bash (to make sure the vault stay encrypted)
    {
        # Try
        # Generate bash vars with the yml file:
        #       $certKey_blah
        #       $certKey_blahblah
        #       $certKey_........
        eval $(parse_yaml ${VAULT_KEYSTORES} "storeKey_") && \
        # Get the value of the variable we are interested in
        # And store it into another var: $CERT_KEY
        eval $(echo "STORE_KEY=\$storeKey_$(echo ${YAML_PATH} |sed 's/[\.-]/_/g')") && \
        # Print the $CERT_KEY var
        echo "${STORE_KEY}"
    } || {
        # Catch
        RETURN_CODE=1
        pki_logger "ERROR" "Error while reading keystore passphrase for ${YAML_PATH} in keystores vault: ${VAULT_KEYSTORES}"
    } && {
        # Finally
        if [ "${STORE_KEY}" == "" ]; then
            pki_logger "ERROR" "Error while retrieving the store key: ${YAML_PATH}"
            RETURN_CODE=1
        fi
        ansible-vault encrypt ${VAULT_KEYSTORES} ${ANSIBLE_VAULT_PASSWD}
        return ${RETURN_CODE}
    }
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

cd $(dirname $0)

#TMP_P12_PASSWORD="$(generatePassphrase)"
REPERTOIRE_TEMPLATE_CONF="${REPERTOIRE_ROOT}/playbooks/templates/vitamui/conf"

# Remove old keystores & servers directories
find ${REPERTOIRE_CERTIFICAT} -type f -name *.jks -exec rm -f {} \;
find ${REPERTOIRE_CERTIFICAT} -type f -name *.p12 -exec rm -f {} \;

# Generate keystores for each COMPONENT
for COMPONENT in $(ls ${REPERTOIRE_CERTIFICAT}/); do

     # Generate the p12 keystore
    pki_logger "-------------------------------------------"
    pki_logger "Creation du keystore de ${COMPONENT}"
    CERT_DIRECTORY=${REPERTOIRE_CERTIFICAT}/${COMPONENT}
    CRT_KEY_PASSWORD=jkspasswd
    P12_KEYSTORE=${REPERTOIRE_CERTIFICAT}/${COMPONENT}/${COMPONENT}.p12
    P12_PASSWORD=jkspasswd
    JKS_KEYSTORE=${REPERTOIRE_CERTIFICAT}/${COMPONENT}/${COMPONENT}.jks
    JKS_PASSWORD=jkspasswd
    JKS_TRUST_STORE=${REPERTOIRE_CERTIFICAT}/${COMPONENT}/truststore.jks
    TRUST_STORE_PASSWORD=jkspasswd

    if [ -f ${P12_KEYSTORE} ]; then
        rm -f ${P12_KEYSTORE}
    fi

    pki_logger "Génération du p12"
    crtKeyToP12 ${CERT_DIRECTORY} \
                ${CRT_KEY_PASSWORD} \
                ${COMPONENT} \
                ${P12_PASSWORD} \
                ${P12_KEYSTORE}

    pki_logger "Génération du jks"
    addP12InJks ${JKS_KEYSTORE} \
                ${JKS_PASSWORD} \
                ${P12_KEYSTORE} \
                ${P12_PASSWORD}

    pki_logger "Suppression du p12"
    if [ -f ${P12_KEYSTORE} ]; then
        rm -f ${P12_KEYSTORE}
    fi

    pki_logger "Génération du trustore"
    for CRT_FILE in $(ls ${REPERTOIRE_CA}/*.crt); do
    pki_logger "Ajout de ${CRT_FILE} dans le truststore"
    ALIAS="$(basename ${CRT_FILE})"
    addCrtInJks ${JKS_TRUST_STORE} \
                ${TRUST_STORE_PASSWORD} \
                ${CRT_FILE} \
                ${ALIAS}
    done

    pki_logger "Copying keystores in template configuration folder"
    cp -f ${JKS_KEYSTORE} ${REPERTOIRE_TEMPLATE_CONF}/${COMPONENT}/

    pki_logger "Copying truststore in template configuration folder"
    cp -f ${JKS_TRUST_STORE} ${REPERTOIRE_TEMPLATE_CONF}/${COMPONENT}/

done

## CAS PARTICULIER POUR LES CERTIFICATS VITAM EXTERNAL
pki_logger "Generate keystore and truststore for VITAM client external"
CERT_DIRECTORY=${REPERTOIRE_VITAM_CERTIFICAT}
CERT_NAME="vitamui-vitam-external"
CRT_KEY_PASSWORD=jkspasswd
P12_KEYSTORE=${REPERTOIRE_VITAM_CERTIFICAT}/${CERT_NAME}.p12
P12_PASSWORD=jkspasswd
JKS_PASSWORD=jkspasswd
JKS_TRUST_STORE=${REPERTOIRE_VITAM_CERTIFICAT}/truststore_${CERT_NAME}.jks
TRUST_STORE_PASSWORD=jkspasswd

if [ -f ${P12_KEYSTORE} ]; then
    rm -f ${P12_KEYSTORE}
fi

pki_logger "Génération du p12"
crtKeyToP12 ${REPERTOIRE_VITAM_CERTIFICAT} \
            ${CRT_KEY_PASSWORD} \
            ${CERT_NAME} \
            ${P12_PASSWORD} \
            ${P12_KEYSTORE}

if [ -f ${JKS_TRUST_STORE} ]; then
    rm -f ${JKS_TRUST_STORE}
fi
pki_logger "Génération du trustore"
for CRT_FILE in $(ls ${REPERTOIRE_VITAM_CA}/*.crt); do
    pki_logger "Ajout de ${CRT_FILE} dans le truststore"
    ALIAS="$(basename ${CRT_FILE})"
    addCrtInJks ${JKS_TRUST_STORE} \
                ${TRUST_STORE_PASSWORD} \
                ${CRT_FILE} \
                ${ALIAS}
done

pki_logger "Copying VITAM truststore and keystore in configuration folder iam_internal"
cp -f ${P12_KEYSTORE} ${REPERTOIRE_TEMPLATE_CONF}/iam-internal/
cp -f ${JKS_TRUST_STORE} ${REPERTOIRE_TEMPLATE_CONF}/iam-internal/

## FIN CAS PARTICULIER POUR LES CERTIFICATS VITAM EXTERNAL

pki_logger "Fin de la génération des stores"
