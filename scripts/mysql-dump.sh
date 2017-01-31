
docker-compose exec mariadb  sh -c 'mysqldump raptor -uroot --password=$MYSQL_ROOT_PASSWORD'
