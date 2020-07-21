#!/bin/bash

#retrieve version
version="$(grep -oP '(?<=>).*?(?=</version>)' pom.xml | grep -v 'version')"

java -jar target/ui-portal-$(echo $version).jar -Xms128m -Xmx512m --spring.config.additional-location=file:src/main/config/ui-portal-application-recette.yml
