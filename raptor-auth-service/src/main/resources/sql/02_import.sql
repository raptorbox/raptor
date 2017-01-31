
CREATE DATABASE IF NOT EXISTS raptor;
USE raptor;


delete from roles where name in ('super_admin', 'admin', 'user', 'guest');
insert into roles (id, name) values (1, 'super_admin'), (2, 'admin'), (3, 'user'), (4, 'guest');
