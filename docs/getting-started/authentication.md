
Introduction
---

Raptor supports the [OAuth 2.0](http://oauth.net/2/) specification in order to handle authentication and authorization to access and use Raptor APIs.

Users will be able to get credentials for the various entities needing to access specific platform capabilities via Raptor API: credentials are in the form of access tokens, called API Keys, that can be generated and obtained though Raptor user interface.

The following picture explains relationships among users, devices and API keys handled by Raptor.

![API Keys](img/API_Keys.png)

API Keys
---

A User can have many API Keys with different permissions. API Keys can be generated from the frontend and used in the code.

The API Keys can be of two type

- **User API Keys** this kind of API key can have more permissions attached and interact with multiple devices at once

- **Device API Key** this key is related to only one device and is commonly used to interact in a more controllable way. Imagine the Device API Keys as a one-to-one relation with the endpoint

API Keys can be generated, disabled and deleted. Permission can be easily added or remove affecting immediatly the device or code using that key.


Permissions
---

Permissions allow to have a fine-grained control over what an API key used in client code can do with the device and it's data

For example a `web application` may have a `READ`-only API key on a device. The `device` itself instead will have `READ` and `WRITE` permissions

Available permissions can be divided in two main groups

**Data Level** available to Device and User keys

- `READ` allow to *read* data for a device
- `WRITE` allow to *write* data for a device
- `SUBSCRIBE` allow to manage *subsciption* and receive data updates on a device
- `EXECUTE` allow to manage *actuations* and invoke or update the status for a device

**Device Level**  available to *User keys only*

- `ALL` a generic *wildcard* permission which override any other permission
- `CREATE` allow to *create new devices*
- `LOAD` allow to *load the definition* of a device
- `UPDATE` allow to *update the definition* of a device
- `DELETE` allow to *drop* a device and all of its data

Tokens
---

Tokens are the keys used to identify the acting user during the interaction with the platform.

*HTTP requests*: During an HTTP request the token must be prefixed with the `Bearer` keyword and inserted in the `Authorization` header.

A request should look like this

```
GET / HTTP/1.1
Host: api.raptorbox.eu
Accept: application/json
Authorization: Bearer 84adf610db60654a564654a8f75
```
