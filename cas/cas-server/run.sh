#!/bin/bash
mvn clean package -DskipTests
java -Dspring.config.additional-location=src/main/config/ -Dspring.profiles.active=dev,gateway -jar -Xms128m -Xmx512m target/cas-server.war
