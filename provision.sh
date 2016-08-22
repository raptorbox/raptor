#!/bin/sh

cd ~

sudo add-apt-repository ppa:webupd8team/java -y
sudo apt-get update -yqq
sudo apt-get install oracle-java8-installer oracle-java8-set-default -y

echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | sudo /usr/bin/debconf-set-selections

wget https://download.elastic.co/elasticsearch/release/org/elasticsearch/distribution/deb/elasticsearch/2.3.5/elasticsearch-2.3.5.deb
sudo dpkg -i elasticsearch-2.3.5.deb

wget http://mirrors.muzzy.it/apache/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.zip

cd /etc
sudo ln -s /vagrant/config raptor

sudo mkdir -p /var/log/raptor
sudo chown `whoami`.`whoami` /var/log/raptor

sudo ln -s /vagrant/bin/raptor.sh /usr/bin
ln -s /vagrant/bin ~/bin

cd /vagrant
mvn clean install -DskipTest=true
