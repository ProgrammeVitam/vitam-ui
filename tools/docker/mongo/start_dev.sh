#!/bin/bash
# Emmanuel Deviller

#########################
# Copy mongo scripts and template them
CUR_DIR=`pwd`
TEMPLATER_EXTRA_VARS="--extra-vars \"@$CUR_DIR/mongo_vars_dev.yml\""

if [ ! -z "$ADDITIONNAL_VITAMUI_CONFIG_FILE" ]; then
    TEMPLATER_EXTRA_VARS="${TEMPLATER_EXTRA_VARS} --extra-vars \"@${ADDITIONNAL_VITAMUI_CONFIG_FILE}\""
fi

# Make sure mongo-entrypoint is created before being mounted by docker to prevent it to be owned by root (which would fail running ansible).
mkdir -p mongo-entrypoint

docker-compose -f ./mongo_dev.yml up -d

sleep 2

# Create replica set and wait a few before execution other init scripts
docker exec -it vitamui-mongo /bin/bash -c "mongosh --port=27018 -f /scripts/mongo/replica-set/000_replicaset_dev.js;sleep 5;mongosh --port=27018 -f /scripts/mongo/replica-set/00_check_replicaset.js"

#########################

echo "Execute $CUR_DIR/database_scripts_templater with custom variables."
eval "ansible-playbook -i $CUR_DIR/hosts $CUR_DIR/database_scripts_templater.yml $TEMPLATER_EXTRA_VARS"
echo "vitamui-mongo is started"
