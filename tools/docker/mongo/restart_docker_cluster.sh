#!/bin/bash

cd $(dirname $0)

./stop_docker_cluster.sh
./start_docker_cluster.sh
