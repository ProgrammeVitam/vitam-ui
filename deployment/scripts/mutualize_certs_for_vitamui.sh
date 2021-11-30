#/bin/bash

# Decription: Mutualize script for pki vitamui and vitam

usage() {
  echo "Usage: $0 -v <path_to_vitam_certs_dir> -u <path_to_vitamui_certs_dir> [-h]"
  echo
  echo "Description: This script allows you to mutualize PKI between Vitam-UI & Vitam."
  echo
  echo "Parameters:"
  echo "  -v <path_to_vitam_certs_dir>   : Path to Vitam certs directory."
  echo "  -u <path_to_vitamui_certs_dir> : Path to Vitam-UI certs directory."
  echo "  -h : Show the usage."
}

################################################################################

while getopts v:u:h flag
do
  case "${flag}" in
    v) VITAM_CERTS_DIR=${OPTARG};;
    u) VITAMUI_CERTS_DIR=${OPTARG};;
    *) usage; exit 1;;
  esac
done

if [ ! -d "$VITAM_CERTS_DIR" ]
then
  usage
  echo "[ERROR]: Could not find certs directory for Vitam."
  exit 1
elif [ ! -d "$VITAMUI_CERTS_DIR" ]
then
  usage
  echo "[ERROR]: Could not find certs directory for Vitam-UI."
  exit 1
fi

set -e

echo "################## Vitam-UI -> Vitam ##################"
VITAM_CERTS_EXTERNAL_DIR="${VITAM_CERTS_DIR}/client-external"

echo "Importing Vitam-UI CA (allow Vitam to identify Vitam-UI services)"
mkdir -p "${VITAM_CERTS_EXTERNAL_DIR}/ca"
for CA in $(ls ${VITAMUI_CERTS_DIR}/client-vitam/ca/ca*.crt); do
  cp -f "${CA}" "${VITAM_CERTS_EXTERNAL_DIR}/ca/vitamui_$(basename ${CA})"
done

echo "Importing Vitam-UI certs (allow Vitam to link Vitam-UI's requests to it's security context)"
mkdir -p "${VITAM_CERTS_EXTERNAL_DIR}/clients/external"
for CA in $(ls ${VITAMUI_CERTS_DIR}/client-vitam/clients/vitamui/*.crt); do
  cp -f "${CA}" "${VITAM_CERTS_EXTERNAL_DIR}/clients/external/$(basename ${CA})"
done
echo "#######################################################"

echo "################## Vitam -> Vitam-UI ##################"
VITAMUI_CERTS_VITAM_DIR="${VITAMUI_CERTS_DIR}/client-vitam"

echo "Importing Vitam CA (allowing Vitam-UI to identify Vitam services)"
mkdir -p "${VITAMUI_CERTS_VITAM_DIR}/ca"
for CA in $(ls ${VITAM_CERTS_DIR}/server/ca/ca*.crt); do
  cp -f "${CA}" "${VITAMUI_CERTS_VITAM_DIR}/ca/vitam_$(basename ${CA})"
done
echo "#######################################################"
