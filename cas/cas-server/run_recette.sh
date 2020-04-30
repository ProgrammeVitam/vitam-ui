#!/bin/bash
mvn clean package -DskipTests
java -Dspring.config.additional-location=src/main/config/cas-server-application-recette.yml -jar -Xms128m -Xmx512m target/cas-server.war
