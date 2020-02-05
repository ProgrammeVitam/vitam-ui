#!/bin/bash
set -e

docker-compose -f vitam-recette.yml up -d

docker exec -it vitam-all /sshd_start.sh
docker exec -it vitam-admin /sshd_start.sh

time docker exec -it -u vitamdev vitam-all vitam-start dockerrecette
echo "VITAM is started"
