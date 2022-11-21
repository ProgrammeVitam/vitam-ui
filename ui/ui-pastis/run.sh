#!/bin/bash

#retrieve version
version="$(grep -oP '(?<=>).*?(?=</version>)' pom.xml | grep -v 'version' | sort --unique)"

java -jar target/ui-pastis-$(echo $version).jar -Xms128m -Xmx512m --spring.profiles.active=dev,recette
