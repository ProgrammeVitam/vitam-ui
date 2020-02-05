#!/usr/bin/env bash

#
# Builds vitamui-osbase docker container.
# Usage:
#   ./build.sh VITAMUI_VERSION [DOCKER BUILD EXTRA PARAMS]...
#
VITAMUI_VERSION=$1
shift
docker build . -t docker.vitamui.com/vitamui-osbase:${VITAMUI_VERSION}  $@
