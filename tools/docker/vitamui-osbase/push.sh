#!/usr/bin/env bash

#
# Publish docker container in docker registry
#
VITAMUI_VERSION=$1
shift
docker push docker.vitamui.com/vitamui-osbase:${VITAMUI_VERSION}

