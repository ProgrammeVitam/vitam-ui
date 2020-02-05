#!/bin/bash
set -e

docker-compose -f vitam-dev.yml up -d

time docker exec -u vitamdev vitam vitam-start dockerdev
echo "VITAM is started"
