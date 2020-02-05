#!/bin/bash

PACKAGE_VERSION=$(cat dist/package.json \
  | grep version \
  | head -1 \
  | awk -F: '{ print $2 }' \
  | sed 's/[",]//g' \
  | tr -d '[[:space:]]')

echo -e "\n================="
echo -e "Installing ui-frontend-commons@$PACKAGE_VERSION for ui-frontend"
echo -e "=================\n"

(cd ../ui-frontend && npm install ../ui-frontend-common/ui-frontend-common-$PACKAGE_VERSION.tgz --save)

