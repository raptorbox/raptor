CBUSER=Administrator
CBPASS=password


echo "Init CB node"

/opt/couchbase/bin/couchbase-cli node-init \
    -c localhost --user=$CBUSER --password=$CBPASS \
    --node-init-data-path=/data/couchbase

sleep 5
echo "Init CB cluster"
/opt/couchbase/bin/couchbase-cli cluster-init \
    -c localhost --user=admin --password=$CBPASS \
    --cluster-init-username=$CBUSER \
    --cluster-init-password="" \
    --cluster-init-ramsize=1600
