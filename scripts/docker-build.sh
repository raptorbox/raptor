#!/bin/sh

currdir=$(pwd)

gittag=$(git describe --tag)
tag=$(echo $gittag | awk -F- '{print $1 "-" $2}' | sed 's/\-$//')
basetag=$(echo $tag | awk -F. '{print $1}')

echo "Tag release is ${tag} for branch release ${basetag}"

# echo "Rebuilding packages"
# ./scripts/mvn-build.sh >> /dev/null

echo "Building containers"

for file in ./*/*/Dockerfile
do

    prjpath=$(dirname $file)
    prj=`echo $(basename $prjpath) | awk -F- '{print $2}'`
    imagename="raptorbox/${prj}:${tag}"

    echo "Building $imagename"
    cd "$currdir/$prjpath"
    docker build . -t "raptorbox/${prj}:${tag}" >> /dev/null

    echo "Tag branch release raptorbox/${prj}:${basetag}"
    docker tag "raptorbox/${prj}:${tag}" "raptorbox/${prj}:${basetag}"

done
