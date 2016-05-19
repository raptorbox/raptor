
RPATH="../raptor-cli"

cd $RPATH
mvn exec:java -Dexec.args="$1 $2 $3"
