#!/bin/sh

cd ../../..
mvn clean install -Dmaven.test.skip=true -Pvitam

cd ./api/api-pastis/pastis-standalone
mvn clean install -Dmaven.test.skip=true -Pstandalone
