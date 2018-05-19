#!/bin/sh

DOCKER_V=17.06.0
COMPOSE_V=1.11.0

# check root / ask sudo
USRID=$(id -u)
if [ $USRID -ne 0 ]; then
  echo "Please run as root/sudo. Exiting"
  exit 1
fi

# check git is avail
if hash git 2>/dev/null; then
  echo "GIT found"
else
  echo "GIT is missing, exiting. Install with eg. `sudo apt-get install git -y`"
  exit 1
fi

# check docker is avail
if hash docker 2>/dev/null; then
  echo "Docker found"
else
  echo "Docker is missing, exiting."
  echo "You can install docker running this script `curl -sSL https://get.docker.com/ | sh`"
  exit 1
fi

# check docker version is ok
DOCKER_CV=`docker -v | awk  '{ print $3 }' | sed 's/,//' | awk -F  "-"  '{ print $1 }' `
DOCKER_V_val=`echo $DOCKER_V | sed -e 's/\.//g'`
DOCKER_CV_val=`echo $DOCKER_CV | sed -e 's/\.//g'`

echo $DOCKER_CV_val

if [ "$DOCKER_CV_val" -ge "$DOCKER_V_val" ]; then
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
  echo "You can install it with `sudo apt-get install python-pip -y && sudo  pip install docker-compose`"
  exit 1
fi

# check docker-compose version is ok
COMPOSE_CV=`docker-compose -v | awk  '{ print $3 }' | sed 's/,//'`
COMPOSE_V_val=`echo $COMPOSE_V | sed -e 's/\.//g'`
COMPOSE_CV_val=`echo $COMPOSE_CV | sed -e 's/\.//g'`

if [ "$COMPOSE_CV_val" -ge "$COMPOSE_V_val" ]; then
  echo "Compose version is $COMPOSE_CV"
else
  echo "Compose version must be $COMPOSE_V or greater, but version $COMPOSE_CV found. Exiting"
  exit 1
fi

if [ ! -e "/opt/raptor" ]; then
  git clone https://github.com/raptorbox/raptor /opt/raptor
fi

echo "127.0.0.1  raptor.local" | sudo tee -a /etc/hosts

cd /opt/raptor
git pull origin master

# fetch and install raptor-cli
if [ -L "/usr/bin/raptor" ]; then
  rm /usr/bin/raptor
fi

chmod +x scripts/raptor-cli.sh
ln -s `pwd`/scripts/raptor-cli.sh /usr/bin/raptor

echo "Install completed!"
echo "run `sudo raptor up -d` to start Raptor"

# x-www-browser http://raptor.local

exit 0
