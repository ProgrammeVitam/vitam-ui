#!/bin/bash

#retrieve version
version="$(grep -oP '(?<=>).*?(?=</version>)' pom.xml | grep -v 'version')"

java -jar target/ui-identity-$(echo $version).jar -Xms128m -Xmx512m --spring.config.additional-location=file:src/main/config/ui-identity-application-recette.yml
