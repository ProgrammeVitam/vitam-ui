#!/usr/bin/env bash

if [ -z $5 ]; then
    echo "Usage: $0 <MONGO_HOST> <MONGO_PORT> <MONGO_USER> <MONGO_PASSWORD> <BACKUP_DIR>"
    exit 1
fi

MONGO_HOST=$1
MONGO_PORT=$2
MONGO_USER=$3
MONGO_PASSWORD=$4
BACKUP_DIR=$5

mongodump --host $MONGO_HOST --port $MONGO_PORT --username $MONGO_USER --password $MONGO_PASSWORD --out $BACKUP_DIR


