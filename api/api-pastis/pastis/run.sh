#!/bin/bash

#retrieve version
version="$(grep -oP '(?<=>).*?(?=</version>)' pom.xml | grep -v 'version' | grep 'SNAPSHOT')"

java -jar target/pastis-$(echo $version).jar -Xms128m -Xmx512m --spring.profiles.active=dev