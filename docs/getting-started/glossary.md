# Glossary

This section of the documentation will explain the core components of the Raptor architecture and a bit of terminology

Some terms will be used and explained later but, just to be prepared, we will introduce them here:

- **Device**, **Service object** or **Object** is any kind of connected device that can be connected to internet. We will use interchangiably *Device* and *Service Object*. The latter is intended to be the declared instance inside the platform. [Read more](data-model/#device)

- A **Stream** is a dataset connected to a Device. Devices will produce data and this data will be stored inside streams. [Read more](data-model/#streams)

- A **Channel** is the single data element inside the stream. Imagine it as a column inside a table. [Read more](data-model/#channels)

- A **Subscription** is a way to communicate asynchronously with the a client (like a device). The client will connect and receive updates when they become available. [Read more](data-model/#subscriptions)

- An **Actuation** allows to perform actions on a connected device and monitor the status of such operations. [Read more](data-model/#actuations)

- Am **API Key** or **Token** is the actual key used to interact with the platform. There may be many keys, connected to the same user. [Read more](authentication/#tokens)
