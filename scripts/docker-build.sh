#!/bin/sh

./scripts/mvn-build.sh

docker-compose build
