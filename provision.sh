#!/bin/sh

CBHOST=raptor.local
CBUSER=admin
CBPASS=password
CBDATAPATH=/data/couchbase

IP="192.168.100.10"
EScluster=raptor

SRCDIR=`pwd`

BASEDIR=/opt
RAPTORDIR=/opt/raptor

sudo adduser --system --no-create-home --group --disabled-login raptor
adduser `whoami` raptor

echo "$IP raptor.local" | sudo tee -a /etc/hosts

if [ -e /vagrant ]; then
  sudo adduser vagrant raptor
  sudo ln -s $BASEDIR/m2 /home/vagrant/.m2
  sudo ln -s /vagrant /opt/raptor
else
  sudo ln -s $SRCDIR /opt/raptor
fi

echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | sudo /usr/bin/debconf-set-selections
sudo add-apt-repository ppa:webupd8team/java -y
sudo apt-get update -qq
sudo apt-get install -yq oracle-java8-installer oracle-java8-set-default unzip nginx

cd $BASEDIR
ES=2.3.5
sudo wget https://download.elastic.co/elasticsearch/release/org/elasticsearch/distribution/deb/elasticsearch/$ES/elasticsearch-$ES.deb
sudo dpkg -i elasticsearch-$ES.deb
sudo rm elasticsearch-$ES.deb

EStoken='#/raptor-conf'
ESconf=`tail -n 1 /etc/elasticsearch/elasticsearch.yml`;
if [ "$ESconf" != "$EStoken" ]; then
  cd /etc/elasticsearch
  echo "network.host: $IP" | sudo tee -a elasticsearch.yml
  echo "cluster.name: $EScluster" | sudo tee -a elasticsearch.yml
  echo "$EStoken" | sudo tee -a elasticsearch.yml
fi

sudo service elasticsearch restart

cd $BASEDIR
MVN=3.3.9
sudo wget http://mirrors.muzzy.it/apache/maven/maven-3/$MVN/binaries/apache-maven-$MVN-bin.zip
sudo unzip apache-maven-$MVN-bin.zip
sudo ln -s $BASEDIR/apache-maven-$MVN/bin/mvn /usr/bin/mvn
sudo rm apache-maven-$MVN-bin.zip

sudo mkdir -p $BASEDIR/m2
sudo ln -s $BASEDIR/m2 ~/.m2
sudo chown -R raptor.raptor $BASEDIR/m2
sudo chmod -R 775 $BASEDIR/m2

sudo ln -s $RAPTORDIR/config /etc/raptor

sudo mkdir -p /var/log/raptor
sudo chown raptor.raptor /var/log/raptor
sudo chmod 755 /var/log/raptor

sudo ln -s $RAPTORDIR/bin/raptor /usr/bin/raptor
sudo ln -s $RAPTORDIR/bin/raptor-err-logs /usr/bin
sudo ln -s $RAPTORDIR/bin/raptor-launch /usr/bin
sudo ln -s $RAPTORDIR/bin/raptor-logs /usr/bin
sudo ln -s $RAPTORDIR/bin/raptor-rebuild /usr/bin

sudo ln -s $RAPTORDIR/scripts/raptor.service  /usr/lib/systemd/system/
sudo systemctl daemon-reload

sudo rm /etc/nginx/sites-enabled/default
sudo ln -s $RAPTORDIR/config/raptor-nginx.conf /etc/nginx/sites-enabled/raptor.conf

sudo service nginx restart

sudo wget http://packages.couchbase.com/releases/4.1.0/couchbase-server-community_4.1.0-ubuntu14.04_amd64.deb
sudo dpkg -i couchbase-server-community_4.1.0-ubuntu14.04_amd64.deb
sudo rm couchbase-server-community_4.1.0-ubuntu14.04_amd64.deb

sudo service couchbase start

sudo mkdir -p /data/couchbase
sudo chown couchbase.couchbase /data/couchbase

/opt/couchbase/bin/couchbase-cli node-init \
    -c localhost --user=$CBUSER --password=$CBPASS \
    --node-init-data-path=$CBDATAPATH
sleep 2

/opt/couchbase/bin/couchbase-cli cluster-init \
    -c localhost --user=$CBUSER --password=$CBPASS \
    --cluster-init-username=$CBUSER \
    --cluster-init-password=$CBPASS \
    --cluster-init-ramsize=1600

cd $BASEDIR
git clone -b master --single-branch https://github.com/apache/activemq-artemis.git
cd activemq-artemis
mvn -q clean install -DskipTests=true

cd $RAPTORDIR
mvn -q clean install -DskipTests=true

sudo service raptor start
