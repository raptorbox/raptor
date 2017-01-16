#!/bin/sh

# ./scripts/docker-setup.sh

cd raptor-auth-service
docker build . -t raptorbox/auth
docker push raptorbox/auth

cd ..
cd raptor-http-api
docker build . -t raptorbox/api
docker push raptorbox/api

cd ..
cd raptor-broker
docker build . -t raptorbox/broker
docker push raptorbox/broker
