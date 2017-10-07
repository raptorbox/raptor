# raptor-sdk

The Raptor Java SDK is part of the raptor platform and used extensively in the codebase.

It can be reused as a standalone library in external application for a direct integration to the exposed API

- [Requirements](#requirements)
- [Setup](#Setup)
- [Authentication](#authentication)
- [Inventory](#inventory)
  - [List devices](#list-devices)
  - [Create a device](#create-a-device)
  - [Update a device](#update-a-device)
  - [Load a device](#load-a-device)
  - [Search for devices](#search-for-devices)
  - [Events notifications](#events-notifications)
    - [Watch device events](#watch-device-events)
    - [Watch data events](#watch-data-events)
    - [Watch action events](#watch-device-action-events)
- [Stream](#stream)
  - [Push data](#push-data)
  - [Pull data](#pull-data)
  - [Last update](#last-update)
  - [Drop data](#drop-data)
  - [Search for data](#search-for-data)
    - [Search by time](#search-by-time)
    - [Search by numeric range](#search-by-numeric-range)
    - [Search by distance](#search-by-distance)
    - [Search by bounding box](#search-by-bounding-box)
- [Action](#action)
    - [Set status](#set-status)
    - [Get status](#get-status)
    - [Invoke an action](#invoke-an-action)
- [Profile](#profile)
  - [Set a value](#set-a-value)
  - [Get a value](#get-a-value)
  - [Get all values](#get-all-values)
- [Tree](#tree)
  - [Create a node](#create-a-node)
  - [Create a device node](#create-a-device-node)
  - [List trees](#list-trees)
  - [Delete a node](#delete-a-node)
- [Admin](#admin)

## Requirements

- Java 8 or higher
- Maven

## Setup

Import in your project the `raptor-sdk` maven package.

## Authentication

Let's start by initializing a raptor client instance

```java

// login with username and password
Raptor raptor = new Raptor("http://raptor.local", "admin", "admin")

// alternatively, login with a token
// Raptor raptor = new Raptor("http://raptor.local", "..token..")


// login and retrieve a token
AuthClient.LoginState loginInfo = raptor.Auth().login();

log.debug("Welcome {} (token: {})", loginInfo.user.getUsername(), loginInfo.token);

// close the session and drop the login token
raptor.Auth().logout();

```

## Inventory

The inventory API store device definitions

### List devices

List devices owned by a user

```java
List<Device> list = raptor.Inventory().list();
log.debug("found {} devices", list.size());
```

### Create a device

Create a new device definition

```java
Device dev = new Device();

dev.name("test device")
    .description("info about");
dev.properties().put("active", true);
dev.properties().put("cost", 15000L);
dev.properties().put("version", "4.0.0");

dev.validate();

raptor.Inventory().create(dev);

log.debug("Device created {}", dev.id());
```

### Update a device

Update a device definition

```java

// Create a data stream named ambient with a channel temperature of type number
Stream s = dev.addStream("ambient", "temperature", "number");

//Add further channels of different types
s.addChannel("info", "text")
s.addChannel("alarm", "boolean")

// add an action
Action a = dev.addAction("light-control");

raptor.Inventory().update(dev);

log.debug("Device updated: \n {}", dev.toJSON());
```

### Load a device

Load a device definition

```java

Device dev1 = raptor.Inventory().load(dev.id());

log.debug("Device loaded: \n {}", dev.toJSON());
```

### Search for devices

Search for device definitions

```java

DeviceQuery q = new DeviceQuery();

// all devices which name contains `test`
q.name.contains("test");
// and properties.version  equals to 4.0.0
q.properties.has("version", "4.0.0");

log.debug("Searching for {}", q.toJSON().toString());
List<Device> results = raptor.Inventory().search(q);

log.debug("Results found {}", results.stream().map(d -> d.name()).collect(Collectors.toList()));
```

### Event notifications

When a device receive data, an action is triggered or the definition changes events are emitted over an asynchronous MQTT channel.

#### Watch device events

Device events are notified when a device definition changes

```java
raptor.Inventory().subscribe(dev, new DeviceCallback() {
    @Override
    public void callback(Device obj, DevicePayload message) {
        log.debug("Device event received {}", message.toString());
    }
});
```

#### Watch data events

Data events are generated when a stream is updated

```java
raptor.Inventory().subscribe(dev, new DataCallback() {
    @Override
    public void callback(Stream stream, RecordSet record) {
        log.debug("dev: Data received {}", record.toJson());
    }
});
```

#### Watch action events

Action events are generated when an action is triggered or the status changes

```java
raptor.Inventory().subscribe(dev, new ActionCallback() {
    @Override
    public void callback(Action action, ActionPayload payload) {
        log.debug("dev: Data received  for {}: {}",
            payload.actionId,
            payload.data
        );
    }
});
```

## Stream

The Stream API handles data push and retrieval

### Push data

Send data based on a stream definition

```java

Stream stream = dev.getStream("ambient")

RecordSet record = new RecordSet(stream)
    .channel("temperature", 5)
    .channel("info", "cold")
    .channel("alarm", true)
    .location(new GeoJsonPoint(11, 45))
    .timestamp(Instant.now())
    ;

raptor.Stream().push(record)
```

### Pull data

Retrieve data

```java

// return 100 records from 10
int from = 10,
    size = 100;

ResultSet results = raptor.Stream().pull(stream, from, size);
```

### Last update

Retrieve the last record sent based on the timestamp

```java
ResultSet results = raptor.Stream().lastUpdate(stream);
```

### Drop data

Removed the data stored in a stream

```java
raptor.Stream().delete(stream);
```

### Search for data

#### Search by time

Search for a range in the data timestamp

```java
Instant i = Instant.now()

DataQuery q = new DataQuery()
    .timeRange(
        i.plus(500, ChronoUnit.MILLIS),
        i.plus(2500, ChronoUnit.MILLIS)
    );

log.debug("Searching {}", q.toJSON().toString());
ResultSet results = raptor.Stream().search(stream, q);
```

#### Search by numeric range

Search for a range in a numeric field

```java
DataQuery q = new DataQuery()
    .range("temperature", -10, 10);

log.debug("Searching {}", q.toJSON().toString());
ResultSet results = raptor.Stream().search(stream, q);
```

#### Search by distance

Search data by distance using the `location` field

```java
DataQuery q = new DataQuery()
    .distance(new GeoJsonPoint(11.45, 45.11), 10000, Metrics.KILOMETERS);

log.debug("Searching {}", q.toJSON().toString());
ResultSet results = raptor.Stream().search(stream, q);
```

#### Search by bounding box

Search data within an area using the `location` field

```java
DataQuery q = new DataQuery()
    .boundingBox(new GeoJsonPoint(12, 45), new GeoJsonPoint(10, 44)));

log.debug("Searching {}", q.toJSON().toString());
ResultSet results = raptor.Stream().search(stream, q);
```

## Action

The Action API handles status and triggering of device defined actions

### Set status

Store the current status of an action

```java
Action a = dev.action("light-control");
ActionStatus status = raptor.Action()
    .setStatus(a, a.getStatus().status("on"));
```

### Get status

Get the current stored status of an action

```java
Action a = dev.action("light-control");
ActionStatus status = raptor.Action()
    .getStatus(a);
```

### Invoke an action

Trigger an action on the remote device

```java
Action a = dev.action("light-control");
ActionStatus status = raptor.Action()
    .invoke(a);
```

Set the status of an action

## Profile

The Profile API handles a per-user key-value local store

### Set a value

Set a value by key

```java
ObjectNode json = r.Profile().newObjectNode();
json.put("test", "foo");
json.put("size", 1000L);
json.put("valid", true);

r.Profile().set("test1", json);
```

### Get a value

Get a value by key

```java

JsonNode response = r.Profile().get("test1");
```

### Get all values


Get all values

```java

JsonNode response = r.Profile().get();
```

## Tree

The Tree API handles hierarchical data structures

### Create a node

Create a generic (type `group`) node tree

```java
TreeNode node1 = TreeNode.create("Root1");
raptor.Tree().create(node1);

TreeNode child1 = TreeNode.create("child1");
TreeNode child2 = TreeNode.create("child2");

raptor.Tree().add(node1, Arrays.asList(child1));
raptor.Tree().add(child1, Arrays.asList(child2));
```

### Create a device node

Create a device references inside the tree. Events from that device will be propagated to the parent nodes up to the root

```java

raptor.Tree().add(child2, dev);
```

### List trees

List all the trees available

```java
List<TreeNode> list = raptor.Tree().list();
```

### Delete a node

Delete a node, causing all the leaves to be point to the parent. In case of `device` node references, this will not remove the device

```java
// drop child1 from previous example, child2 will be now direct children of Root1
raptor.Tree().remove(
    node1 //Root1
        .children().get(0) // child1
);
```

## Admin

Admin APIs allow the management of users, tokens and permissions

For an up to date reference see the [tests](https://github.com/raptorbox/raptor/tree/master/raptor-sdk/src/test/java/org/createnet/raptor/sdk/admin)

## License

Apache2

```
Copyright FBK/CREATE-NET

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
