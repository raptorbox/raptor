#!/bin/sh

DOCKER_V=1.12.0
COMPOSE_V=1.9.0

# check root / ask sudo
USRID=$(id -u)
if [ $USRID -ne 0 ]; then
  echo "Please run as root/sudo. Exiting"
  exit 1
fi

# check docker is avail
if hash docker 2>/dev/null; then
  echo "Docker found"
else
  echo "Docker is missing, exiting"
  exit 1
fi

# check docker version is ok
DOCKER_CV=`docker -v | awk  '{ print $3 }' | sed 's/,//'`
DOCKER_V_val=`echo $DOCKER_V | sed -e 's/\.//g'`
DOCKER_CV_val=`echo $DOCKER_CV | sed -e 's/\.//g'`

if [ $DOCKER_CV_val -ge $DOCKER_V_val ]; then
  echo "Docker version is $DOCKER_CV"
else
  echo "Docker version must be $DOCKER_V or greater, but version $DOCKER_CV found. Exiting"
  exit 1
fi

# check docker-compose is avail
if hash docker-compose 2>/dev/null; then
  echo "Docker Compose found"
else
  echo "Docker Compose is missing, exiting"
  exit 1
fi

# check docker-compose version is ok
COMPOSE_CV=`docker-compose -v | awk  '{ print $3 }' | sed 's/,//'`
COMPOSE_V_val=`echo $COMPOSE_V | sed -e 's/\.//g'`
COMPOSE_CV_val=`echo $COMPOSE_CV | sed -e 's/\.//g'`

if [ $COMPOSE_CV_val -ge $COMPOSE_V_val ]; then
  echo "Compose version is $COMPOSE_CV"
else
  echo "Compose version must be $COMPOSE_V or greater, but version $COMPOSE_CV found. Exiting"
  exit 1
fi

mkdir /opt/raptor -p
cd /opt/raptor

# set vm.max_map_count for elasticsearch
sysctl -w vm.max_map_count=262144
echo "vm.max_map_count=262144" | tee /etc/sysctl.d/90-raptor.conf

# fetch https://raw.githubusercontent.com/raptorbox/raptor/master/docker-compose.yml
wget https://raw.githubusercontent.com/raptorbox/raptor/master/docker-compose.yml

# create sql import directory
mkdir -p raptor-auth-service/src/main/resources/sql

# fetch sql files
wget -O raptor-auth-service/src/main/resources/sql/01_schema.sql https://raw.githubusercontent.com/raptorbox/raptor/master/raptor-auth-service/src/main/resources/sql/01_schema.sql
wget -O raptor-auth-service/src/main/resources/sql/02_import.sql https://raw.githubusercontent.com/raptorbox/raptor/master/raptor-auth-service/src/main/resources/sql/02_import.sql

# fetch and install raptor-cli
wget https://raw.githubusercontent.com/raptorbox/raptor/master/scripts/raptor-cli.sh
chmod u+x raptor-cli.sh
ln -s `pwd`/raptor-cli.sh /usr/bin/raptor

echo "Install completed!"
echo "run 'raptor up' to start Raptor"

exit 0
