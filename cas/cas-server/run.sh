#!/bin/bash
mvn clean package -DskipTests
mkdir -p target/src/main
rm -rf target/src/main/config
cp -r src/main/config target/src/main/config
java -Dspring.config.location=src/main/config/cas-server-application-dev.yml -jar -Xms128m -Xmx512m target/cas-server.war
