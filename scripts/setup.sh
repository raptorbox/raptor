CBHOST=raptor.local
CBUSER=admin
CBPASS=password
CBDATAPATH=/data/couchbase

#curl -u $CBUSER:$CBPASS -d username=$CBUSER -d password=$CBPASS -d port=8091 http://$CBHOST:8091/settings/web

mkdir -p /var/log/raptor

echo "Init CB node"

/opt/couchbase/bin/couchbase-cli node-init \
    -c localhost --user=$CBUSER --password=$CBPASS \
    --node-init-data-path=$CBDATAPATH

sleep 2
echo "Init CB cluster"
/opt/couchbase/bin/couchbase-cli cluster-init \
    -c localhost --user=$CBUSER --password=$CBPASS \
    --cluster-init-username=$CBUSER \
    --cluster-init-password=$CBPASS \
    --cluster-init-ramsize=1600
