#!/bin/bash

#retrieve version
version="$(grep -oP '(?<=>).*?(?=</version>)' pom.xml | grep -v 'version' | grep -oP 'SNAPSHOT')"

java -jar target/pastis-5.0-SNAPSHOT.jar -Xms128m -Xmx512m --spring.profiles.active=dev
