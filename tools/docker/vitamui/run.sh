#!/usr/bin/env bash

cd `dirname $0`

if [ ! -z $1 ]; then
    export VITAMUI_VERSION=$1
fi

if [ -z $VITAMUI_VERSION ]; then
    echo "ERROR: env variable VITAMUI_VERSION not define."
    exit 1
fi

docker stop vitamui
docker rm -v vitamui
docker run -it -d \
    --name vitamui \
    -e "container=docker" \
    --privileged
    docker.vitamui.com/vitamui:${VITAMUI_VERSION} \
    $@

docker exec -it vitamui bash
