
# MQTT broker

## The protocol

MQTT is a machine-to-machine (M2M) connectivity protocol. It was designed as an extremely lightweight publish/subscribe messaging transport. For more details see [mqtt.org](http://mqtt.org)

## Connection

MQTT and Websocket-over-MQTT both accept connections at port `:1883`.

A username/password pair is required and can be one of those operations

- Username and password of an real user. For example the default user is `admin:admin`
- Empty username and a valid `apiKey` provided as password. This allow to keep control over permission and eventually to retire the key.

## Data updates

Every time a stream receive a data update it will be notified over this subscription.

There is topic built over a pattern that can be used

`<device id>/streams/<stream name>/updates`

The payload is structured in with those fields

- `channels`: a map of channel names and their values
- `timestamp`: timestamp of when the data has been created
- `userId`: ID of the user that authored the data (or one of its apiKey)
- `streamId`: ID of the stream of the object
- `objectId`: ID of the object the record is referred to

### Example:


```
{
  "channels": {
    "location": { "lat": 41, "lon": 11 },
    "altitude": 42,
  },
  "timestamp": 0123456789,
  "userId": "8cc42953-13fd-4019-b9f5-1ea372382d01",
  "streamId": "position",  
  "objectId": "bc09e014-2d7d-4e66-9702-b31e54b2de26",
}
```

## Actuations


When sending commands to an action (via the HTTP API) a connected device can receive the body of request via MQTT and react accordingly

The topic to register for is `<device id>/actions/<action name>` the payload sent is exactly the same as the one sent by the action invocation via HTTP

##System events


It is possible to receive information on object life cycle (like creation, update or deletion), data updates and actuation operations.

The topic to use is `<device id>/events`

The payload may vary based on the source of the event.

There are common fields shared by all the payload types

- `type`:     Type of event, may be one of `object`, `stream`, `actuation`
- `op`:       Operation requested; for `object` type can be one of `create`, `update`, `delete`, `push`. For an `action` type it may be `execute` or `delete`
- `userId`:   ID of the user that authored the data (or one of its apiKey)
- `streamId`: ID of the stream of the object
- `objectId`: ID of the object the record is referred to
- `object`:   Full JSON object definition
- `path`:     Full path of the object, if it has a hierarchy

Additionally based on the event type

- `actionId`: name of the action triggered

## Hierarchy based events


Objects can be used as group for other objects as it may be for gateways devices.

A parent object can receive all the children notifications by registering with a wildcard on its id

For example `<parent id>/#` will catch all of its children related notifications (events, actions, streams)

## Disable events


*Note* To disable events for an object set `eventsEnabled` (under `settings`) to `false` in the definition

Example:
```
{
  "name": "Device",
  "settings": {
    "eventsEnabled": true
  }
}
```
