#!/usr/bin/env bash

docker build . -t docker.vitamui.com/vitamui-base:${VITAMUI_VERSION} $@
