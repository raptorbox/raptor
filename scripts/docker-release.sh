#!/bin/sh

currdir=$(pwd)

echo "Rebuilding mvn packages"
./scripts/mvn-build.sh >> /dev/null

echo "Build images"
./scripts/docker-build.sh

echo "Push images"
./scripts/docker-push.sh
