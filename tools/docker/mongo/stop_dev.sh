#!/bin/bash
# Emmanuel Deviller

cd $(dirname $0)

#docker stop vitamui-mongo
docker compose -f ./mongo_dev.yml down --remove-orphans
#docker stop vitamui-mongo vitamui-mongo2 vitamui-mongo3; docker rm vitamui-mongo vitamui-mongo2 vitamui-mongo3

echo "vitamui-mongo is stopped"


