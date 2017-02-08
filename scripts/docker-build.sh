#!/bin/sh

mvn clean install -DskipTests=true

cd raptor-http-api
mvn package -DskipTests=true
cd ..

cd raptor-broker
mvn package -DskipTests=true
cd ..

cd raptor-auth-service
mvn package -DskipTests=true
cd ..

docker-compose build
