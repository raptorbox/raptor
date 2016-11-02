# Data Model

Raptor uses a well-defined data model to define a device and all of its possible interactions

## Device

Imagine we want to control a Drone within Raptor.

To start we need to identify some key fields to describe our device to the platform

```
{
    "name": "Drone",
    "description": "My drone",
    "customFields": {
        "model": "aero-202111"
    }
}
```

###Fields


- `name` is _required_ and is used to identify the device

- `description` is a textual description of the device to help users to identify the device and will be used in searches too

- `customFields` is an object of key / values properties which may be useful to define further details of the device. In the example we put the model but may be any other data

## Streams

Streams are datasets, you can imagine them as tables. A channel stay in a stream and is the minimum information to give sense to the data. Imagine to a channel as a column of our table.

Let's have an example with a **location table** which store the movement of our Drone

Position     | Altitude | Heading
------------ | -------- | -------
11.25, 52.11 | 114.2    | 241.9

Our Drone is at a geo-referenced `Position` and at a certain `Altitude` with a degree of `Heading`.

We can now easily migrate to the Raptor json data model

The _streams_ field contains a list of stream definitions indexed by name

In our example the stream name is `location`

```
{
    "streams": {
        "location": {

        },          
        "another stream": {}
    }
}
```

## Channels

Channels are the single unit of data for a stream. An example can be the GPS `position` and `heading` or `temperature` or `pressure` for a weather device.

A sample extended definition is

```
{
  "position": {
      "type": "geo_point",
      "unit": "degrees"
  }
}
```

The channel name is specified as key in the channels object, in this case `position`

###Fields

- `type` is a predefined value and can be one of

  - `number` any number like `-1`, `1.5`, `1.23e-7`
  - `string` any UTF-8 string like `µ€llò ←→ wørld ™`
  - `boolean` one of `true` or `false`
  - `geo_point` a point coordinate in a format of

    - a string like `lon, lat`
    - a json array like `[lon, lat]`
    - a json object like `{ "lon": n.n, "lat": n.n }`
    - a geohash `drm3btev3e86`

- `unit` is a descriptive string value for the type of the data, eg `degrees`, `miles`, `celsius`, `meters`

A minimum channel definition can be composed of just the `channel name` and the `type`

```
{
  "position": "geo_point"
}
```

## Actuations

Let's add an action to invoke and then track the status on a connected device

```
{
    "actions": [
        {
            "name": "fly-home",
            "description": "Force the drone to fly back to its deck"
        }
    ]
}
```

The `actions` fields is a list as array of objects with just two fields

###Fields


- `name` a unique name of the action, it will be used in the API call

- `description` for the human presentation of the action

--------------------------------------------------------------------------------

## A complete example definition

The common form is the "shrinked" one, where all the details hidden

Follows a definition of a Drone

```
{
    "name": "Drone",
    "streams": {
        "location": {
          "position": "geo_point",
          "altitude": "number",
          "heading": "number",
        },
    },
    "actions": [
      "fly-home"
    ]
}
```

The same can be expressed in an expanded way

```
{
    "name": "Drone",
    "description": "My drone",
    "customFields": {
        "model": "aero/202111"
    },
    "streams": {
        "description": "GPS outdoor location",
        "type": "sensor",
        "location": {
            "channels": {
                "position": {
                    "type": "geo_point"
                },
                "altitude": {
                    "type": "number",
                    "unit": "m"
                },
                "heading": {
                    "type": "number",
                    "unit": "degrees"
                }
            }
        }
    },
    "actions": [
        {
            "name": "fly-home",
            "description": "Force the drone to fly back to its deck"
        }
    ]
}
```
