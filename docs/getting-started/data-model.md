
Raptor uses a well-defined data model to define a device and all of its possible interactions

Device
---

Imagine we want to control a Drone within Raptor.

To start we need to identify some key fields to describe our device to the platform

```json
{
    "name": "Drone",
    "description": "My drone",
    "customFields": {
        "model": "aero/202111"
    }
}
```

**Fields**

- **name** is *required* and is used to identify the device

- **description** is a textual description of the device to help users to identify the device and will be used in searches too

- **customFields** is an object of key / values properties which may be useful to define further details of the device. In the example we put the model but may be any other data

---
Streams
---

Streams are datasets, you can imagine them as tables where defined channels are columns

Let's have an example with a **location table** which store the movement of our Drone

Position     | Altitude      | Heading
------------ | ------------- | ------------
11.25, 52.11 | 114.2         | 241.9

Our Drone is at a geo-referenced `Position` and at a certain `Altitude` with a degree of `Heading`.

We can now easily migrate to the Raptor json data model

The *streams* field contains a list of stream definitions indexed by name

In our example the stream name is `location`

```json
{
    "streams": {
        "location": {
            "channels": {},
            "description": "GPS outdoor location",
            "type": "sensor"
        }
    }
}
```

**Fields**

- **description** is the human readable presentation of the stream

- **type** the type of stream. It can be any value (eg `open-data`), most commonly `sensor`

- **channels** is a *required* field and contains the list of data fields definitions for this stream

---
Channels
---

Channels are the single unit of data for a stream. An example can be the GPS `position` and `heading` or `temperature` or `pressure` for a weather device.

A sample definition of a channel is


```json
{
    "channels": {
        "position": {
            "type": "geo_point",
            "unit": "degrees"
        }
    }
}
```

The channel name is specified as key in the channels object, in this case `position`

**Fields**

- **type** is a predefined value and can be one of
    - `number` any number like `-1`, `1.5`, `1.23e-7`
    - `string` any UTF-8 string like `µ€llò ←→ wørld ™`
    - `boolean` one of `true` or `false`
    - `geo_point` a point coordinate in a format of
        - a string like `lon, lat`
        - a json array like `[lon, lat]`
        - a json object like `{ "lon": n.n, "lat": n.n }`
        - a geohash `drm3btev3e86`

- **unit** is a descriptive string value for the type of the data, eg `degrees`, `miles`, `celsius`,  `meters`

---
Actuations
---

Let's add an action to invoke and then track the status on a connected device

```json
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

**Fields**

- **name**  a code-name of the action, it will be used in further api call

- **description**  the human presentation of the action

---
The complete example model
---

Your model should look like this at the end. Now `POST /`-ing it to the api will result in a new device created and ready to use

```json
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

the model can be shortened to

```json
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
