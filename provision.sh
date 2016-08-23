#!/bin/sh

cd ~
mkdir ~/bin -p

BASEDIR=/vagrant

echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | sudo /usr/bin/debconf-set-selections
sudo add-apt-repository ppa:webupd8team/java -y
sudo apt-get update -qq
sudo apt-get install -yq oracle-java8-installer oracle-java8-set-default unzip

ES=2.3.5
wget https://download.elastic.co/elasticsearch/release/org/elasticsearch/distribution/deb/elasticsearch/$ES/elasticsearch-$ES.deb
sudo dpkg -i elasticsearch-$ES.deb
rm elasticsearch-$ES.deb

MVN=3.3.9
wget http://mirrors.muzzy.it/apache/maven/maven-3/$MVN/binaries/apache-maven-$MVN-bin.zip
unzip apache-maven-$MVN-bin.zip
ln -s ../apache-maven-$MVN/bin/mvn ~/bin

cd ~
. .profile

sudo ln -s $BASEDIR/config /etc/raptor

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

cd ~
git clone -b master --single-branch https://github.com/apache/activemq-artemis.git
cd activemq-artemis
mvn clean install -DskipTests=true

cd $BASEDIR
mvn clean install -DskipTests=true
