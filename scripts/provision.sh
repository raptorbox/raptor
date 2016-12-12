#/bin/sh


# Install Java & Maven
sudo add-apt-repository ppa:webupd8team/java -y
sudo apt-get update 2>&1 /dev/null
sudo apt-get install oracle-java8-set-default -yqq 2>&1 /dev/null
sudo apt-get install maven -yqq

#Install Docker
sudo apt-get install apt-transport-https ca-certificates -yqq
sudo apt-key adv --keyserver hkp://ha.pool.sks-keyservers.net:80 --recv-keys 58118E89F3A912897C070ADBF76221572C52609D
echo "deb https://apt.dockerproject.org/repo ubuntu-xenial main" | sudo tee /etc/apt/sources.list.d/docker.list
sudo apt-get update > /dev/null
sudo apt-get install docker-engine -yqq

#Install Docker Compose
curl -L "https://github.com/docker/compose/releases/download/1.9.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

#Elasticsearch 5 fix
sysctl -w vm.max_map_count=262144

sudo ./scripts/docker-setup.sh
