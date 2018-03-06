# Raptor

[![Build Status](https://travis-ci.org/raptorbox/raptor.svg?branch=master)](https://travis-ci.org/raptorbox/raptor) [![Coverage Status](https://coveralls.io/repos/github/raptorbox/raptor/badge.svg?branch=master)](https://coveralls.io/github/raptorbox/raptor?branch=master) [![Project Stats](https://www.openhub.net/p/raptorbox/widgets/project_thin_badge.gif)](https://www.openhub.net/p/raptorbox) [![Join the chat at https://gitter.im/raptorbox-iot/Lobby](https://badges.gitter.im/raptorbox-iot/Lobby.svg)](https://gitter.im/raptorbox-iot/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

## About 
Raptorbox is a complete open-source solution for Rapid Prototyping of application for the Internet of Things (IoT) with a beautiful graphical user interface that enables the user to have a concrete view of the platform.

It is composed of a set of Restful HTTP APIs and an MQTT broker to create a reactive stream of data and action triggers for your devices. Its GUI enables users with better management and complete view of your devices with the data.

Raptorbox is equipped with `node-red` to enable rapid decision logic based on data and effective actions on devices.

# Pre-requisite

### Requirements
Raptor requires a minimum of 4GB of RAM to run slightly.

### Required software
For a quick installation, you only need both Docker and Docker Compose installed on your pc.
1. [Docker](https://www.docker.com/) 
2. [Docker-compose](https://docs.docker.com/compose/)
See the official Docker guide to install them by clicking on them.

For example on an Ubuntu or Debian box as root you can install with:
```
curl -sSL https://get.docker.com/ | sh
adduser `whoami` docker
sudo apt-get install python-pip -y && sudo  pip install docker-compose
```

# Getting Started
1. Clone the [repository](https://github.com/raptorbox/raptor) 
2. run *docker-compose up* in the root folder
3. open **http://localhost:80/** on your browser.

Some useful commands to see more about the platform docker instance
```
docker-compose ps to          //view the process status
docker-compose logs -f        //to view the logs
```


### Suggested configurations

For a development setup just use *docker-compose* to start the services with the default settings

For public-facing services, ensure to update the default passwords in config/raptor.yml in the user's section

## 5 minute tutorial

A quick tutorial can be downloaded from [here](https://github.com/raptorbox/raptor-tutorial) to control and stream data from your devices.

# Documentation
You can have a look to the [documentation hub](https://docs.raptorbox.eu/)

# Credits
Raptor is a project by the OpenIoT area of FBK/CREATE-NET in the wonderful Trentino, Italy.

Get in touch if you want to know more about Raptor or our other IoT projects.

We have launched an IoT training program as well. If you are interested in learning more about IoT, this is a best chance to learn about new IoT tools and technologies.

# License

Copyright FBK/CREATE-NET <http://create-net.fbk.eu>

```
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
```
