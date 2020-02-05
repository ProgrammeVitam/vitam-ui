#!/bin/bash
set -e

docker-compose up -d
docker-compose exec vitamui /workspace/deployment/deployment-0.1.0-jenkins-pipelines/config.sh

echo "VitamUI-API is started"
