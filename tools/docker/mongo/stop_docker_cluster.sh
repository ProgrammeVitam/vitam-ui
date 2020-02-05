#!/bin/bash
# Emmanuel Deviller

docker-compose -f ./mongo_cluster.yml down --remove-orphans

echo "vitamui-mongo is stopped"


