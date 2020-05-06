#!/usr/bin/env bash
set -e

REPERTOIRE_ROOT="$( cd "$( readlink -f $(dirname ${BASH_SOURCE[0]}) )/../../.." ; pwd )"

function init () {
    
    REPERTOIRE_CERTIFICAT="${REPERTOIRE_ROOT}/environments/certs"
    REPERTOIRE_CA="${REPERTOIRE_ROOT}/pki/ca"
    CA_ROOT_TYPE="all"
    REPERTOIRE_CONFIG="${REPERTOIRE_ROOT}/pki/config"
    TEMP_CERTS="${REPERTOIRE_ROOT}/pki/tempcerts"
    PARAM_KEY_CHIFFREMENT="rsa:4096"
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

    find "${DIR_TO_PURGE}" -type f -name "*.attr" -exec rm -f {} \;
    find "${DIR_TO_PURGE}" -type f -name "*.old"  -exec rm -f {} \;
    find "${DIR_TO_PURGE}" -type f -name "*.req"  -exec rm -f {} \;
}

function generatePassphrase {
    cat /dev/urandom | tr -dc 'a-zA-Z0-9' | head -c 48
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
        ansible-vault create ${VAULT_FILE} ${VAULT_PASS}
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
            echo -n "${REPERTOIRE_CERTIFICAT}/vault-${TYPE}.yml"
            ;;
        "keystores")
            echo -n "${ENVIRONMENT_VARIABLES}/vault-${TYPE}.yml"
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
        "keystores")
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
            echo -n "stores_"
            ;;
        *) 
            pki_logger "ERROR" "Unable to determinate the template of the key for the type: ${TYPE}"
            return 1;
            ;;
    esac
}

# Method allowing to retrieve a key in a vault file (ONLY a single level of tree structure).
# @param TYPE Type of vault.
# @param KEY Key linked to the data to retrieve.
# @return The value linked to the provided key, if it exists.
function getComponentPassphrase {
    local TYPE="${1}"
    local KEY="${2}"

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
        # Try
        # Generate bash vars with the yml file:
        #       $certKey_blah
        #       $certKey_blahblah
        #       $certKey_........
        eval $(parse_yaml ${VAULT_FILE} "$KEY_PREFIX") && \
        # Get the value of the variable we are interested in
        # And store it into another var: $CERT_KEY
        eval $(echo "CERT_KEY=\$$KEY_PREFIX$(normalize_key ${KEY})") && \
        # Print the $CERT_KEY var
        echo "${CERT_KEY}"
    } || {
        # Catch
        RETURN_CODE=1
        pki_logger "ERROR" "Error while reading certificate passphrase for ${KEY} in certificates vault: ${VAULT_FILE}"
    } && {
        # Finally
        if [ "${CERT_KEY}" == "" ]; then
            pki_logger "ERROR" "Error while retrieving the key: ${KEY}"
            RETURN_CODE=1
        fi
        ansible-vault encrypt ${VAULT_FILE} ${VAULT_PASS}
        return ${RETURN_CODE}
    }
}


# Method allowing to check if a key is declared in a vault file (ONLY a single level of tree structure).
# @param TYPE Type of vault.
# @param KEY Key linked to the data to retrieve.
# @return True if the value exists, false otherwise.
function hasComponentPassphrase {
    local TYPE="${1}"
    local KEY="${2}"

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
        # Try
        # Generate bash vars with the yml file:
        #       $certKey_blah
        #       $certKey_blahblah
        #       $certKey_........
        eval $(parse_yaml ${VAULT_FILE} "$KEY_PREFIX") && \
        # Get the value of the variable we are interested in
        # And store it into another var: $CERT_KEY
        eval $(echo "CERT_KEY=\$$KEY_PREFIX$(normalize_key ${KEY})")

        if [ "${CERT_KEY}" == "" ]; then
            echo "false"
        else
            echo "true"
        fi
    } || {
        # Catch
        RETURN_CODE=1
        pki_logger "ERROR" "Error while reading certificate passphrase for ${KEY} in certificates vault: ${VAULT_FILE}"
    } && {
        # Finally
        ansible-vault encrypt ${VAULT_FILE} ${VAULT_PASS}
        return 0
    }
}


# Method allowing to save a key/value in a vault file (ONLY a single level of tree structure).
# @param TYPE Type of vault.
# @param KEY Key of the data.
# @param VALUE Value of the data.
function setComponentPassphrase {
    local TYPE="${1}"
    local KEY="${2}"
    local VALUE="${3}"

    # KWA TODO: explain & comonize the sed usage ;
    # KWA TODO: change replacement string in sed : /_/ ==> /__/
    local RETURN_CODE=0
    local VAULT_FILE=$(getVaultFile "$TYPE")
    local VAULT_PASS=$(getVaultPass "$TYPE")

    if [ ! -f "${VAULT_FILE}" ]; then
        pki_logger "ERROR" "The vault file is not found. Please, initialize it before call me ! Vault file: ${VAULT_FILE}"
        return 1
    fi

    # Decrypt vault file
    ansible-vault decrypt ${VAULT_FILE} ${VAULT_PASS}

    # Try/catch/finally stuff with bash (to make sure the vault stay encrypted)
    {
        local NORMALIZED_KEY=$(normalize_key "${KEY}")
        # If the key is already present, we remove it (i.e all line beginning with $NORMALIZED_KEY will be removed)
        sed -i "/^${NORMALIZED_KEY}/d" "${VAULT_FILE}"
        # Add key to vault
        echo "${NORMALIZED_KEY}: ${VALUE}" >> "${VAULT_FILE}"
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
        return ${RETURN_CODE}
    }
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
