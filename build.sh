#!/bin/sh

set -xe

if [ "sonar" = "$1" ]; then
	./gradlew clean test sonarqube -Dsonar.host.url=http://127.0.0.1:9000 -Dsonar.verbose=true
elif [ "docker" = "$1" ]; then
./gradlew clean jib --no-build-cache -x test -x sonarqube -DsendCredentialsOverHttp=true
else
	./gradlew clean build --no-build-cache -x test -x sonarqube
fi