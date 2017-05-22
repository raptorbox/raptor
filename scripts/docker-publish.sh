#!/bin/sh

./scripts/docker-build.sh

cd raptor-broker
docker build . -t raptorbox/broker
docker push raptorbox/broker
cd ..

cd docker/proxy
docker build . -t raptorbox/proxy
docker push raptorbox/proxy
cd ../..

cd raptor-api

cd raptor-action
docker build . -t raptorbox/action
docker push raptorbox/action
cd ..

cd raptor-auth
docker build . -t raptorbox/auth
docker push raptorbox/auth
cd ..

cd raptor-inventory
docker build . -t raptorbox/inventory
docker push raptorbox/inventory
cd ..

cd raptor-stream
docker build . -t raptorbox/stream
docker push raptorbox/stream
cd ..

cd raptor-profile
docker build . -t raptorbox/profile
docker push raptorbox/profile
cd ..

cd ..
