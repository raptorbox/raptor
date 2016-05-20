CBUSER=admin
CBPASS=password
CBDATAPATH=/data/couchbase

echo "Init CB node"

/opt/couchbase/bin/couchbase-cli node-init \
    -c localhost --user=$CBUSER --password=$CBPASS \
    --node-init-data-path=$CBDATAPATH

sleep 5
echo "Init CB cluster"
/opt/couchbase/bin/couchbase-cli cluster-init \
    -c localhost --user=$CBUSER --password=$CBPASS \
    --cluster-init-username=$CBUSER \
    --cluster-init-password=$CBPASS \
    --cluster-init-ramsize=1600
