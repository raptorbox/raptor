create table users (
  id int,
  uuid varchar(256),
  username varchar(256),
  password varchar(256),
  firstname varchar(256),
  lastname varchar(256),
  email varchar(256),
  lastPasswordResetDate int,
  created int,
  enabled boolean
);

create table roles (
  id int,
  name varchar(256)
);

create table token (
  id int,
  name varchar(256),
  token varchar(2048),
  user_id int,
  enabled boolean
);
