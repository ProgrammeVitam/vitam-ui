#!/bin/bash

#retrieve version
version="$(grep -oP '(?<=>).*?(?=</version>)' pom.xml | grep -v 'version')"

./target/security-$(echo $version).jar -Xms128m -Xmx512m --spring.profiles.active=dev
