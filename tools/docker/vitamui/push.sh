#!/usr/bin/env bash

cd `dirname $0`
VITAMUI_VERSION=$1
docker push docker.vitamui.com/vitamui:${VITAMUI_VERSION}
