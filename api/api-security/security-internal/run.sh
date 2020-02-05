#!/bin/bash

mkdir -p target/src/main
rm -rf target/src/main/config
cp -r src/main/config target/src/main/config

#retrieve version
version="$(grep -oP '(?<=>).*?(?=</version>)' pom.xml | grep -v 'version')"

./target/security-internal-$(echo $version).jar -Xms128m -Xmx512m --spring.config.additional-location=file:src/main/config/security-internal-application-dev.yml
