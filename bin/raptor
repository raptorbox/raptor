#!/bin/sh -

# Absolute path to this script. /home/user/bin/foo.sh
SCRIPT=$(readlink -f $0)
# Absolute path this script is in. /home/user/bin
SCRIPTPATH=`dirname $SCRIPT`

RPATH="$SCRIPTPATH/../raptor-cli"

cd $RPATH
mvn exec:java -Dmaven.repo.local=/opt/maven -Dexec.args="$1 $2 $3"
