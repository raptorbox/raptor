Raptor Auth Service
===

***Documentation is coming soon!***

Authorization and authentication API which handles login, registration and permission management

Usage
---

Start with maven `mvn spring-boot:run`

Once running visit `http://localhost:8090/auth/swagger.yaml` to get an overview of the API docs

Development
---

To run the project at least `mariadb` and `broker` containers should be running.

The `schema.sql` and `import.sql` files can be used to initialize a SQL database, an in memory database should work as long the correct driver is set

To drop the default database schema

```
-- SET foreign_key_checks = 0;
--
-- drop table if exists authorities;
-- drop table if exists roles;
-- drop table if exists users;
-- drop table if exists users_roles;
-- drop table if exists tokens;
-- drop table if exists devices;
--
-- drop table if exists acl_sid;
-- drop table if exists acl_class;
-- drop table if exists acl_object_identity;
-- drop table if exists acl_entry;
--
-- SET foreign_key_checks = 1;
```

License
---

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
