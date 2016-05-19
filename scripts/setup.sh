CBUSER=Administrator
CBPASS=password

/opt/couchbase/bin/couchbase-cli node-init \
    -c localhost --user=$CBUSER --password=$CBPASS \
    --node-init-data-path=/data/couchbase

sleep 5
echo "Instance initialization"
echo "--------------------------------------------------------"
/opt/couchbase/bin/couchbase-cli cluster-init \
    -c localhost --user=admin --password=$CBPASS \
    --cluster-init-username=$CBUSER \
    --cluster-init-password="" \
    --cluster-init-ramsize=1400
sleep 5

echo "Create buckets"
echo "--------------------------------------------------------"
/opt/couchbase/bin/couchbase-cli bucket-create \
    --bucket-type=couchbase \
    --bucket-ramsize=400 \
    --bucket-replica=1 \
    --bucket=serviceobjects \
    -c localhost --user=$CBUSER --password=$CBPASS

/opt/couchbase/bin/couchbase-cli bucket-create \
    --bucket-type=couchbase \
    --bucket-ramsize=200 \
    --bucket-replica=1 \
    --bucket=actuations \
     -c localhost --user=$CBUSER --password=$CBPASS

/opt/couchbase/bin/couchbase-cli bucket-create \
    --bucket-type=couchbase \
    --bucket-ramsize=600 \
    --bucket-replica=1 \
    --bucket=soupdates \
     -c localhost --user=$CBUSER --password=$CBPASS

/opt/couchbase/bin/couchbase-cli bucket-create \
    --bucket-type=couchbase \
    --bucket-ramsize=200 \
    --bucket-replica=1 \
    --bucket=subscriptions \
     -c localhost --user=$CBUSER --password=$CBPASS 
