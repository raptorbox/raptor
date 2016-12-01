
docker rm -f $(docker ps -aq)
docker rmi -f $(docker images -a | grep "raptor" | awk '{print $3}')
