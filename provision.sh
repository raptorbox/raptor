#!/bin/sh

cd ~

BASEDIR=/vagrant

echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | sudo /usr/bin/debconf-set-selections
sudo add-apt-repository ppa:webupd8team/java -y
sudo apt-get update -qq
sudo apt-get install -yq oracle-java8-installer oracle-java8-set-default unzip 

wget https://download.elastic.co/elasticsearch/release/org/elasticsearch/distribution/deb/elasticsearch/2.3.5/elasticsearch-2.3.5.deb
sudo dpkg -i elasticsearch-2.3.5.deb

wget http://mirrors.muzzy.it/apache/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.zip
unzip apache-maven-3.3.9-bin.zip
ln -s ../apache-maven-3.3.9/bin/mvn ~/bin

cd /etc
sudo ln -s $BASEDIR/config raptor

sudo mkdir -p /var/log/raptor
sudo chown `whoami`.`whoami` /var/log/raptor
sudo chmod 775 /var/log/raptor

sudo ln -s $BASEDIR/bin/raptor /usr/bin/raptor
ln -s $BASEDIR/bin/raptor-err-logs /usr/bin
ln -s $BASEDIR/bin/raptor-launch /usr/bin
ln -s $BASEDIR/bin/raptor-logs /usr/bin
ln -s $BASEDIR/bin/raptor-rebuild /usr/bin

sudo ln -s $BASEDIR/scripts/raptor.service  /usr/lib/systemd/system/
sudo systemctl daemon-reload
sudo systemctl start raptor

cd $BASEDIR
mvn clean install -DskipTest=true
