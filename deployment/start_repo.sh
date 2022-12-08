#!/bin/bash
set -e

BASEDIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
docker run -dit --restart unless-stopped --name httpd_repo -p 8000:80 --volume "$BASEDIR/repo:/usr/local/apache2/htdocs/" httpd:2.4