#!/bin/sh

# echo "Rebuilding packages"
# ./scripts/mvn-build.sh >> /dev/null

for file in ./*/*/Dockerfile
do
    echo "- ${file}"
done
