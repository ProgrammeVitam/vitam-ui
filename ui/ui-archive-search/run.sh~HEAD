#!/bin/bash

mkdir -p target/src/main
rm -rf target/src/main/config
cp -r src/main/config target/src/main/config

#retrieve version
version="$(grep -oP '(?<=>).*?(?=</version>)' pom.xml | grep -v 'version')"

./target/ui-archive-search$(echo $version).jar -Xms128m -Xmx512m --spring.config.additional-location=file:src/main/config/ui-archive-search-application-recette.yml
