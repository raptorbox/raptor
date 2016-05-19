
# Absolute path to this script. /home/user/bin/foo.sh
SCRIPT=$(readlink -f $0)
# Absolute path this script is in. /home/user/bin
SCRIPTPATH=`dirname $SCRIPT`

RPATH="$SCRIPTPATH/../raptor-cli"

cd $RPATH

java -jar target/raptor-cli-1.0-jar-with-dependencies.jar
