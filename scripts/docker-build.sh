#!/bin/sh

echo "Rebuilding packages"
./scripts/mvn-build.sh >> /dev/null

tag=$(git describe --tag)

currdir=$(pwd)

for file in ./*/*/Dockerfile
do

    prjpath=$(dirname $file)
    prj=`echo $(basename $prjpath) | awk -F- '{print $2}'`
    imagename="raptorbox/${prj}:${tag}"

    echo "Building $imagename"
    cd "$currdir/$prjpath"
    docker build . -t "raptorbox/${prj}:${tag}" >> /dev/null
    # docker tag "raptorbox/${prj}:${tag}" "raptorbox/${prj}:latest"
done
