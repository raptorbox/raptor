# Raptor

[![Build Status](https://travis-ci.org/raptorbox/raptor.svg?branch=master)](https://travis-ci.org/raptorbox/raptor) [![Coverage Status](https://coveralls.io/repos/github/raptorbox/raptor/badge.svg?branch=master)](https://coveralls.io/github/raptorbox/raptor?branch=master) [![Project Stats](https://www.openhub.net/p/raptorbox/widgets/project_thin_badge.gif)](https://www.openhub.net/p/raptorbox) [![Join the chat at https://gitter.im/raptorbox-iot/Lobby](https://badges.gitter.im/raptorbox-iot/Lobby.svg)](https://gitter.im/raptorbox-iot/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Welcome to the Raptor IoT platform repository!

Were you looking for the [API documentation](http://raptorbox.github.io/)

## What is this?

Raptor is a complete open-source solution for Rapid Prototyping of application for the Internet of Things or IoT

It is composed of an HTTP API and a MQTT broker (+AMQP and others) to create reactive stream of data of your devices

## Setup

To setup an Up&Running Raptorbox environment, please follow this [guide](https://raptorbox.github.io/documentation/)  


### Vagrant setup

If you have vagrant installed on your PC just run `vagrant up` to get the services up and running for you

## Getting started

***Work in progress!***

We are actively working to structure and expand the documentation!

To have a start we have the [documentation site](http://raptorbox.github.io)

### Querying the HTTP API

To get started there is a swagger based API documentation available at those addresses (once the appliance is up and running)

The default access credentials are username: `admin` and password: `admin`

- [Data and Object management API](http://localhost:8080/swagger.yaml)
- [User and Permission API](http://localhost:8090/auth/swagger.yaml)

The first operation is to login with an user and get an apiToken

```
curl -XPOST -H "Content-Type: application/json" \
-d '{ "username": "admin", "password": "admin" }' \
http://localhost:8090/auth/login
```

You will get back a session token and your user details

```
{
  "token":"eyJhbGciOiJIUzUxMiJ9.eyJjcmVhdGVkIjoxNDc3NTU1MTIwMTM5LCJleHAiOjE0Nzc1NTY5MjAsInV1aWQiOiI1OTRkYjY4MS1kMDVjLTQ1OWQtYjg4MS1kMzJlNjQwY2E5MzcifQ.4khnaaAVyoMm_QWjES5NQ6uNbUaaCyrTfrGx47p2qUdFh6ZWtvafGrZvf7iiarIj50FeXrqoSc9N0XTOYaVnig",
  "user":{
    "uuid":"594db681-d05c-459d-b881-d32e640ca937",
    // ... other details
  }
```

With the token it is now possible to query the Data and Object Management API and start sending your data streams!

For example to list all the registered objects

```
curl -XGET -H "Content-Type: application/json" \
-H "Authorization: Bearer <the whole token>" \
http://localhost:8080/
```

### Accessing the MQTT broker

To access the broker open an MQTT (or Websocket) connection to `http://localhost:1883` using your username or password.

API Key based access is supported by setting an empty username and the API Key as password. Currently this feature is not completely working due to [this issue](https://issues.apache.org/jira/browse/ARTEMIS-826)

You can use any library supporting MQTTv3.1.1 like [mqtt.js](https://github.com/mqttjs/MQTT.js) or [Eclipse PAHO](https://eclipse.org/paho/)

## Libraries and tools

We are working to expand the support of SDK and libraries in different languages.

Here the list of available ones:

- [javascript sdk](https://github.com/raptorbox/raptorjs)
- [java sdk](https://github.com/raptorbox/raptor/raptor-client) (work in progess)

## Credits

Raptor is a project by the [OpenIoT area](http://perfectiot.eu/) of [FBK/CREATE-NET](http://create-net.fbk.eu) in the wonderful Trentino, Italy.

Get in touch if you want to know more about Raptor or our other IoT projects.

We have an [IoT training program](http://perfectiot.eu/iot-training/) too!

## License

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
