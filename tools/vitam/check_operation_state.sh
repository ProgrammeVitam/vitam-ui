#!/bin/bash
# Script permettant de récupérer le nombre d'opération en fonction de leur statut dans VITAM.
# Il tourne interroge ADMIN-ACCESS de manière continue toutes les 10s et affiche le résultat.
set -e

# PARAMETERS
if [ -z $1 ] || [ -z $2 ] || [ -z $3 ] || [ -z $4 ]; then
    echo "Usage: $0 <URL_ADMIN_ACCESS> <CERT_FILE> <KEY_FILE> <TENANT> <ACCESS_CONTRACT>"
    echo "Ex: $0 https://10.0.11.1:8444 mycert.pem 7 AC-000001"
    exit 1
fi

URL_ADMIN_ACCESS=$1
CERT_FILE=$2
KEY_FILE=$3
TENANT="X-Tenant-Id: $4"
echo $TENANT
ACCESS_CONTRACT="X-Access-Contract-Id: $5"
echo $ACCESS_CONTRACT

# CONFIGURATION
API_ADMIN_ACCESS="${URL_ADMIN_ACCESS}/admin-external/v1/operations"

CURL_OPTS="-s -XGET --cert ${CERT_FILE} --key ${KEY_FILE} --insecure"

# Query conditions
QUERY_TOTAL='{}'
QUERY_OK='{"statuses":["OK"] }'
QUERY_STARTED='{"statuses":["STARTED"]}'
QUERY_KO='{"statuses":["KO"]}'
QUERY_FATAL='{"statuses":["FATAL"]}'
QUERY_COMPLETED='{"states":["COMPLETED"]}'
QUERY_RUNNING='{"states":["RUNNING"]}'
QUERY_PAUSE='{"states":["PAUSE"]}'


##### MAIN ######
while true; do

    TOTAL=`curl $CURL_OPTS -H "$TENANT" -H "$ACCESS_CONTRACT" -H "Content-Type: application/json" -H "Accept: application/json" $API_ADMIN_ACCESS --data-binary "$QUERY_TOTAL" | jq  '.["$hits"].total'`

    OK=`curl $CURL_OPTS -H "$TENANT" -H "$ACCESS_CONTRACT" -H "Content-Type: application/json" -H "Accept: application/json" $API_ADMIN_ACCESS  --data-binary "$QUERY_OK" | jq  '.["$hits"].total'`
    STARTED=`curl $CURL_OPTS -H "$TENANT" -H "$ACCESS_CONTRACT" -H "Content-Type: application/json" -H "Accept: application/json" $API_ADMIN_ACCESS  --data-binary "$QUERY_STARTED" | jq  '.["$hits"].total'`
    KO=`curl $CURL_OPTS -H "$TENANT" -H "$ACCESS_CONTRACT" -H "Content-Type: application/json" -H "Accept: application/json" $API_ADMIN_ACCESS  --data-binary "$QUERY_KO" | jq  '.["$hits"].total'`
    FATAL=`curl $CURL_OPTS -H "$TENANT" -H "$ACCESS_CONTRACT" -H "Content-Type: application/json" -H "Accept: application/json" $API_ADMIN_ACCESS  --data-binary "$QUERY_FATAL" | jq  '.["$hits"].total'`

    COMPLETED=`curl $CURL_OPTS -H "$TENANT" -H "$ACCESS_CONTRACT" -H "Content-Type: application/json" -H "Accept: application/json" $API_ADMIN_ACCESS  --data-binary "$QUERY_COMPLETED" | jq  '.["$hits"].total'`
    RUNNING=`curl $CURL_OPTS -H "$TENANT" -H "$ACCESS_CONTRACT" -H "Content-Type: application/json" -H "Accept: application/json" $API_ADMIN_ACCESS  --data-binary "$QUERY_RUNNING" | jq  '.["$hits"].total'`
    PAUSE=`curl $CURL_OPTS -H "$TENANT" -H "$ACCESS_CONTRACT" -H "Content-Type: application/json" -H "Accept: application/json" $API_ADMIN_ACCESS  --data-binary "$QUERY_PAUSE" | jq  '.["$hits"].total'`

    echo `date` "TOTAL:$TOTAL COMPLETED:$COMPLETED RUNNING:$RUNNING PAUSE:$PAUSE --- STARTED:$STARTED OK:$OK KO:$KO FATAL:$FATAL"
    sleep 10
done


#curl -v -XGET --insecure --cert ./vitamui-vitam-external.pem 'https://10.0.11.1:8444/admin-external/v1/operations' -H @header --data-binary '{"statuses":["KO"],"states":["COMPLETED"]}' | python -c 'import sys, json; print json.load(sys.stdin)["$hits"]["total"]'





