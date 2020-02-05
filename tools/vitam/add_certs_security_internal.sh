#!/usr/bin/env bash

if [ -z $1 ] || [ -z $2 ]; then
    echo "Usage: $0 <certificat> <url_security_internal>"
    echo "Ex: $0 mycert.crt http://10.0.0.30:28005"
    exit 1
fi
CERT_FILE=$1
URL_SECURITY_INTERNAL=$2

echo "Adding VITAMUI Certs into security internal"
CERT_BASE64=$(cat ${CERT_FILE} | base64)
API_SECURITY_INTERNAL="${URL_SECURITY_INTERNAL}/v1/api/identity"

curl -v -H "Content-Type: application/json" -X POST ${API_SECURITY_INTERNAL} -d '
  {
    "contextId" : "CT-000001",
    "certificate" : "'"${CERT_BASE64}"'"
  }"'
