#!/usr/bin/env bash

if [ -z $5 ]; then
    echo "Usage: $0 <ELASTIC_URL> <ELASTIC_REPO> <BACKUP_DIR>"
    exit 1
fi


#http://eltasicsearch-data.service.${consul_domain}:9200

ELASTIC_URL=
ELASTIC_REPO=vitam_backup
BACKUP_DIR=

SNAPSHOT_NAME="backup-"`date +%Y-%m-%d`


curl -X PUT "${ELASTIC_URL}/_snapshot/${ELASTIC_REPO}" -d '{ "type": "fs", "settings": { "location": "${BACKUP_DIR}" } }'

curl -X PUT "${ELASTIC_URL}/_snapshot/${ELASTIC_REPO}/${SNAPSHOT_NAME}?wait_for_completion=true"
