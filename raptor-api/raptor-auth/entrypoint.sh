#!/bin/sh

# wait for mariadb first boot
if [ ! -e "/firstboot" ]
then
    sleep 10
fi

touch "/firstboot"

java -jar /raptor.jar
