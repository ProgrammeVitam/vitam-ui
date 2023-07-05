#!/bin/sh

cd ../../..
mvn clean install -DskipTests -Pvitam

cd ./api/api-pastis/pastis-standalone
mvn clean install -DskipTests -Pstandalone
