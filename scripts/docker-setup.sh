#!/bin/sh

cd raptor-http-api
mvn clean package -DskipTests=true
cd ..

cd raptor-broker
mvn clean package -DskipTests=true
cd ..

cd raptor-auth-service
mvn clean package -DskipTests=true
cd ..

docker-compose build
