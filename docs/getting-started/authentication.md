
Introduction
---

Raptor supports user login and token based authentication (leveraging on [JWT](https://jwt.io/) (RFC 7519) specification) in order to handle authentication and authorization to access and use Raptor APIs.

Users will be able to get credentials for the various entities needing to access specific platform capabilities via Raptor API: credentials are in the form of access tokens, called API Keys, that can be generated and obtained though Raptor user interface.

The following picture explains relationships among users, devices and API keys handled by Raptor.

![API Keys](img/API_Keys.png)

API Keys
---

A User can have many API Keys which can be generated from the frontend and used in the code.

API Keys can be generated, disabled and deleted, affecting immediately the device or code using that key.

Permissions
---

Permissions allow to have a fine-grained control over what an API key used in client code can do with the device and it's data

For example a `web application` may have a `READ`-only API key to access the data of a device. The `device` itself instead will have `WRITE` permission to the data stream API in order to store the data from sensors.

Available permissions follow

- `ADMINISTRATION` allow full access to the API, overriding other permissions

- `CREATE` allow to *create new devices*
- `LOAD` allow to *load the definition* of a device
- `UPDATE` allow to *update the definition* of a device
- `DELETE` allow to *drop* a device and all of its data

- `PULL` allow to *read* data for a device
- `PUSH` allow to *write* data for a device
- `SUBSCRIBE` allow to manage *subsciption* and receive data updates on a device
- `EXECUTE` allow to manage *actuations* and invoke or update the status for a device

Permissions can be delegated to a device from an owner (its creator) to another user

Refer to the permission API on how to change permissions and delegate access to users

Tokens
---

Tokens are used in place of username/password to identify the acting user during the interaction with the platform.

*HTTP requests*: During an HTTP request the token may be prefixed with the `Bearer` keyword and inserted in the `Authorization` header.

A request look like this

```
GET / HTTP/1.1
Host: api.raptor.local
Accept: application/json
Authorization: Bearer <token>
```
