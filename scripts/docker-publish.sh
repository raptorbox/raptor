#!/bin/sh

currdir=$(pwd)

gittag=$(git describe --tag)
tag=$(echo $gittag | awk -F- '{print $1 "-" $2}' | sed 's/\-$//')
basetag=$(echo $tag | awk -F. '{print $1}')

echo "Tag release is ${tag} for branch release ${basetag}"

echo "Pushing containers"

for file in ./*/*/Dockerfile
do

    prjpath=$(dirname $file)
    prj=`echo $(basename $prjpath) | awk -F- '{print $2}'`
    imagename="raptorbox/${prj}:${tag}"

    echo "Pushing $imagename"
    docker push "$imagename"

    echo "Pushing raptorbox/${prj}:${basetag}"
    docker push "raptorbox/${prj}:${basetag}"

done
