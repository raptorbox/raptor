#!/bin/sh

tag=$(git describe --tag)
currdir=$(pwd)

for file in ./*/*/Dockerfile
do

    prjpath=$(dirname $file)
    prj=`echo $(basename $prjpath) | awk -F- '{print $2}'`
    imagename="raptorbox/${prj}:${tag}"

    echo "Publishing $imagename"
    cd "$currdir/$prjpath"
    docker push "raptorbox/${prj}:${tag}"
    
done
