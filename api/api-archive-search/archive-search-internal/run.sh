#!/bin/bash

mkdir -p target/src/main
rm -rf target/src/main/config
cp -r src/main/config target/src/main/config

#retrieve version
version="$(grep -oP '(?<=>).*?(?=</version>)' pom.xml | grep -v 'version')"

java -Dvitam.config.folder=src/main/resources/dev/vitam -jar target/archive-search-internal-$(echo $version).jar -Xms128m -Xmx512m --spring.profiles.active=dev
