#!/bin/bash

PACKAGE_VERSION=$1

echo -e "\n================="
echo -e "Installing ui-frontend-common@$PACKAGE_VERSION for ui-frontend"
echo -e "=================\n"

(cd ../ui-frontend && npm install ui-frontend-common@$PACKAGE_VERSION --save) &&

echo -e "\nui-frontend-common@$PACKAGE_VERSION installation SUCCESS\n"

