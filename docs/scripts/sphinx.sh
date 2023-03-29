#!/bin/bash

set -vx

apt-get -y install python3-sphinx

#Docker solution

docker run -it --rm -v ./../fr:/docs sphinxdoc/sphinx sphinx-quickstart