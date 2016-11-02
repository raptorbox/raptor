Overview
---

This section of the documentation will explain the core components of the Raptor architecture and a bit of terminology



Glossary
---

Some terms will be used and explained later but, just to be prepared, we will introduce them here:


- **Device** is any kind of connected device that can be connected to internet. We will use interchangiably *Device* and *Virtual Device*. The latter is intended to be the declared instance inside the platform. [Read more](data-model/#device)

- A **Stream** is a dataset connected to a Device. Devices will produce data and this data will be stored inside streams. [Read more](data-model/#streams)

- A **Channel** is the single data element inside the stream. Imagine it as a column inside a table. [Read more](data-model/#channels)

- A **Subscription** is a way to communicate asynchronously with the a client (like a device). The client will connect and receive updates when they become available. [Read more](data-model/#subscriptions)

- An **Actuation** allows to perform actions on a connected device and monitor the status of such operations. [Read more](data-model/#actuations)

- The **API Key** is an item which allow to identify and give permissions to an user or a device in order to perform operation in the platform. [Read more](authentication/#api-keys)

- A **Token** is the actual key used to interact with the platform. There may be many keys, connected to different ways to authenticate. [Read more](authentication/#tokens)
