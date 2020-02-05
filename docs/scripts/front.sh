#!/bin/bash
set -vx

apt-get install build-essential
curl -sL https://deb.nodesource.com/setup_12.x | bash -
apt-get install -y nodejs
npm install -g @angular/cli
npm install -g webpack bower grunt-cli ungit karma yo
