#!/usr/bin/env bash
set -e

REPERTOIRE_ROOT="$( cd "$( readlink -f $(dirname ${BASH_SOURCE[0]}) )/../../.." ; pwd )"

function init () {
    CERTIFICATE_DIR="${REPERTOIRE_ROOT}/environments/certs"
    CA_DIR="${REPERTOIRE_ROOT}/pki/ca"
    CA_ROOT_TYPE="all"
    CONFIG_DIR="${REPERTOIRE_ROOT}/pki/config"
    TEMP_CERTS="${REPERTOIRE_ROOT}/pki/tempcerts"
    CRYPTO_SPEC="rsa:4096"
    ENVIRONMENT_VARIABLES="${REPERTOIRE_ROOT}/environments/group_vars/all"

    if [ -f "${REPERTOIRE_ROOT}/vault_pass.txt" ]; then
        ANSIBLE_VAULT_PASSWD="--vault-password-file ${REPERTOIRE_ROOT}/vault_pass.txt"
    else
        ANSIBLE_VAULT_PASSWD="--ask-vault-pass"
    fi
    if [ -f "${REPERTOIRE_ROOT}/vault_pki.pass" ]; then
        ANSIBLE_VAULT_PKI_PASSWD="--vault-password-file ${REPERTOIRE_ROOT}/vault_pki.pass"
    else
        ANSIBLE_VAULT_PKI_PASSWD="--ask-vault-pass"
    fi

    # Check if gawk is present
    hash gawk
}

function read_ansible_var {
    local ANSIBLE_VAR="${1}"
    local ANSIBLE_HOST="${2}"

    ANSIBLE_CONFIG="${REPERTOIRE_ROOT}/pki/scripts/lib/ansible.cfg" \
    ansible ${ANSIBLE_HOST} -i ${ENVIRONNEMENT_FILE} ${ANSIBLE_VAULT_PASSWD} -m debug -a "var=${ANSIBLE_VAR}" \
    | grep "${ANSIBLE_VAR}" | gawk -F ":" '{gsub("\\s","",$2); print $2}'
}

# Delete useless files
function purge_directory {
    local DIR_TO_PURGE="${1}"

    if [ ! -d "${DIR_TO_PURGE}" ]; then
        pki_logger "ERROR" "Directory ${DIR_TO_PURGE} does not exists"
        return 1
    fi

    find "${DIR_TO_PURGE:?}" -type f -name "*.attr" -exec rm -vf {} \;
    find "${DIR_TO_PURGE:?}" -type f -name "*.old"  -exec rm -vf {} \;
    find "${DIR_TO_PURGE:?}" -type f -name "*.req"  -exec rm -vf {} \;
}

function generatePassphrase {
    if [ "${DEV_MODE}" == "true" ]; then
        echo "changeme"
    else
        cat /dev/urandom | tr -dc 'a-zA-Z0-9' | head -c 48
    fi
}

function normalize_key {
    local KEY="${1}"

    echo "${KEY}" | sed 's/[\\/\.-]/_/g'
}

# Method allowing to initialize a vault file.
# @param TYPE Type of vault
# @param ERASE_VAULT Boolean indicating if the vault file must be reset if it exists.
function initVault {
    local TYPE="${1}"
    local ERASE_VAULT="${2:=true}"

    local VAULT_FILE=$(getVaultFile "$TYPE")
    local VAULT_PASS=$(getVaultPass "$TYPE")

    if [ ! -f "${VAULT_FILE}" ]; then
        pki_logger "Création du fichier ${VAULT_FILE}"
        mkdir -p "${VAULT_FILE%/*}"
        echo '---' > ${VAULT_FILE}
        ansible-vault encrypt ${VAULT_FILE} ${VAULT_PASS}
        echo '---' > "${VAULT_FILE}.example"
    elif [ "$ERASE_VAULT" == "true" ]; then
        pki_logger "Réinitialisation du fichier ${VAULT_FILE}"
        ansible-vault decrypt ${VAULT_FILE} ${VAULT_PASS}
        echo '---' > ${VAULT_FILE}
        ansible-vault encrypt ${VAULT_FILE} ${VAULT_PASS}
        echo '---' > "${VAULT_FILE}.example"
    fi
}

# Method allowing to determinate the path of a vault file accoring its type
# @param TYPE Type of vault
# @return The path of the vault file.
function getVaultFile() {
    local TYPE="${1}"

    case $TYPE in
        "ca" | "certs")
            echo -n "${CERTIFICATE_DIR}/vault-${TYPE}.yml"
            ;;
        "keystores" | "truststores")
            echo -n "${ENVIRONMENT_VARIABLES}/vault-keystores.yml"
            ;;
        *)
            pki_logger "ERROR" "Unable to determinate vault file for the type: ${TYPE}"
            return 1;
            ;;
    esac
}

# Method allowing to determinate the password of a vault file accoring its type
# @param TYPE Type of vault
# @return The password of the vault file.
function getVaultPass() {
    local TYPE="${1}"

    case $TYPE in
        "ca" | "certs")
            echo -n "${ANSIBLE_VAULT_PKI_PASSWD}"
            ;;
        "keystores" | "truststores")
            echo -n "${ANSIBLE_VAULT_PASSWD}"
            ;;
        *)
            pki_logger "ERROR" "Unable to determinate vault password for the type: ${TYPE}"
            return 1;
            ;;
    esac
}

# Method allowing to determinate the prefix of a key in a vault file.
# @param TYPE Type of vault.
# @return The prefix of the key.
function getKeyPrefix() {
    local TYPE="${1}"

    case $TYPE in
        "ca" | "certs")
            echo -n "certKey_"
            ;;
        "keystores")
            echo -n "keystore_"
            ;;
        "truststores")
            echo -n "truststore_"
            ;;
        *)
            pki_logger "ERROR" "Unable to determinate the prefix for the type: ${TYPE}"
            return 1;
            ;;
    esac
}

# Method allowing to retrieve a key in a vault file (ONLY a single level of tree structure).
# @param TYPE Type of vault.
# @param KEY Key linked to the data to retrieve.
# @return The value linked to the provided key, if it exists.
function getPassphrase {
    local TYPE="${1}"
    local KEY="${2}"

    local RETURN_CODE=0
    local VAULT_FILE=$(getVaultFile "$TYPE")
    local VAULT_PASS=$(getVaultPass "$TYPE")
    local KEY_PREFIX=$(getKeyPrefix "$TYPE")

    if [ ! -f "${VAULT_FILE}" ]; then
        pki_logger "ERROR" "The vault file is not found. Please, initialize it before calling me ! Vault file: ${VAULT_FILE}"
        return 1
    fi

    local KEY_TO_SEARCH="${KEY_PREFIX}$(normalize_key ${KEY})"

    local VAULT_CONTENT=$(ansible-vault view ${VAULT_FILE} ${VAULT_PASS})
    if [ $? -ne 0 ]; then
        pki_logger "ERROR" "Error while reading the vault file ${VAULT_FILE}"
        return 1
    fi
    if echo "$VAULT_CONTENT" | grep -q "^${KEY_TO_SEARCH}:"; then
        local VALUE=$(echo "$VAULT_CONTENT" | grep "^${KEY_TO_SEARCH}:" | awk '{print $2}')
        echo "${VALUE}"
    else
        pki_logger "ERROR" "Error while retrieving the key KEY_TO_SEARCH=${KEY_TO_SEARCH} of TYPE=${TYPE} in ${VAULT_FILE}"
        return 1
    fi
}

# Method allowing to check if a key is declared in a vault file (ONLY a single level of tree structure).
# @param TYPE Type of vault.
# @param KEY Key linked to the data to retrieve.
# @return True if the value exists, false otherwise.
function hasPassphrase {
    local TYPE="${1}"
    local KEY="${2}"

    local VAULT_FILE=$(getVaultFile "$TYPE")
    local VAULT_PASS=$(getVaultPass "$TYPE")
    local KEY_PREFIX=$(getKeyPrefix "$TYPE")

    if [ ! -f "${VAULT_FILE}" ]; then
        pki_logger "ERROR" "The vault file is not found. Please, initialize it before calling me ! Vault file: ${VAULT_FILE}"
        return 1
    fi

    local KEY_TO_SEARCH="${KEY_PREFIX}$(normalize_key ${KEY})"

    local VAULT_CONTENT=$(ansible-vault view ${VAULT_FILE} ${VAULT_PASS})
    if [ $? -ne 0 ]; then
        pki_logger "ERROR" "Error while reading the vault file ${VAULT_FILE}"
        return 1
    fi
    if echo "$VAULT_CONTENT" | grep -q "^${KEY_TO_SEARCH}:"; then
        echo "true"
    else
        echo "false"
    fi
}

# Method allowing to save a key/value in a vault file (ONLY a single level of tree structure).
# @param TYPE Type of vault.
# @param KEY Key of the data.
# @param VALUE Value of the data. If not provided, a random value will be generated.
# @return The value of the key.
function setPassphrase {
    local TYPE="${1}"
    local KEY="${2}"
    local VALUE="${3}"

    if [ -z "${VALUE}" ]; then
        # We generate a random key if no value is provided
        local PASSPHRASE=$(generatePassphrase)
    else
        local PASSPHRASE="${VALUE}"
    fi

    local RETURN_CODE=0
    local VAULT_FILE=$(getVaultFile "$TYPE")
    local VAULT_PASS=$(getVaultPass "$TYPE")
    local KEY_PREFIX=$(getKeyPrefix "$TYPE")

    if [ ! -f "${VAULT_FILE}" ]; then
        pki_logger "ERROR" "The vault file is not found. Please, initialize it before call me ! Vault file: ${VAULT_FILE}"
        return 1
    fi

    # Decrypt vault file
    ansible-vault decrypt ${VAULT_FILE} ${VAULT_PASS}

    # Try/catch/finally stuff with bash (to make sure the vault stay encrypted)
    {
        local NORMALIZED_KEY=${KEY_PREFIX}$(normalize_key "${KEY}")
        # If the key is already present, we remove it (i.e all line beginning with $NORMALIZED_KEY will be removed)
        sed -i "/^${NORMALIZED_KEY}/d" "${VAULT_FILE}"
        # Add key to vault
        echo "${NORMALIZED_KEY}: ${PASSPHRASE}" >> "${VAULT_FILE}"
        # The same for the example file
        sed -i "/^${NORMALIZED_KEY}/d" "${VAULT_FILE}.example"
        echo "${NORMALIZED_KEY}: changeme" >> "${VAULT_FILE}.example"
    } || {
        # Catch
        RETURN_CODE=1
        pki_logger "ERROR" "Error while writing to vault file: ${VAULT_FILE}"
    } && {
        # Finally
        ansible-vault encrypt ${VAULT_FILE} ${VAULT_PASS}
        echo "${PASSPHRASE}"
        return ${RETURN_CODE}
    }
}

# Method allowing to retrieve a key in a vault file (ONLY a single level of tree structure) or to set it if it does not exist.
# @param TYPE Type of vault (ca, certs, keystores or truststores).
# @param KEY Key linked to the data to retrieve or set.
# @return The value linked or set for the provided key
function getOrSetPassphrase {
    local TYPE="${1}"
    local KEY="${2}"

    local EXISTS=$(hasPassphrase "${TYPE}" "${KEY}")
    if [ "${EXISTS}" == "false" ]; then
        echo $(setPassphrase "${TYPE}" "${KEY}")
    else
        echo $(getPassphrase "${TYPE}" "${KEY}")
    fi
}

function pki_logger {
    if (( ${#} >= 2 )); then
        local ERR_LEVEL="${1}"
        local MESSAGE="${2}"
    else
        local ERR_LEVEL="INFO"
        local MESSAGE="${1}"
    fi
    echo "[${ERR_LEVEL}] [$(basename ${0}): ${FUNCNAME[ 1 ]}] ${MESSAGE}" 1>&2
}

# https://gist.github.com/pkuczynski/8665367
function parse_yaml {
    local prefix=$2
    local s='[[:space:]]*' w='[a-zA-Z0-9_]*' fs=$(echo @|tr @ '\034')
    sed -ne "s|^\($s\)\($w\)$s:$s\"\(.*\)\"$s\$|\1$fs\2$fs\3|p" \
        -e "s|^\($s\)\($w\)$s:$s\(.*\)$s\$|\1$fs\2$fs\3|p"  $1 |
    gawk -F$fs '{
        indent = length($1)/2;
        vname[indent] = $2;
        for (i in vname) {if (i > indent) {delete vname[i]}}
        if (length($3) > 0) {
            vn=""; for (i=0; i<indent; i++) {vn=(vn)(vname[i])("_")}
            printf("%s%s%s=\"%s\"\n", "'$prefix'",vn, $2, $3);
        }
    }'
}
