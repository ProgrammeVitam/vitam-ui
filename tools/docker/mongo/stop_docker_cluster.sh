#!/bin/bash
# Emmanuel Deviller

cd $(dirname $0)

docker compose -f ./mongo_cluster.yml down --remove-orphans

echo "vitamui-mongo is stopped"


