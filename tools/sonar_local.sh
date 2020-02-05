#!/usr/bin/env bash
mvn clean verify -Psonar -DskipTests \
        -Dsonar.host.url=http://localhost:19000 \
        -Dsonar.login=beddd2de7a6018d7f697abb54a4d86b3b08695b0
