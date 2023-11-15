#!/bin/bash
mvn clean package -DskipTests
java -Dspring.config.additional-location=src/main/config/cas-server-application-dev.yml -Dspring.profiles.active=gateway -jar -Xms128m -Xmx512m target/cas-server.war
