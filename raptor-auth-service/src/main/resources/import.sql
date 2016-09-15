delete from roles where name = 'super_admin' limit 1;
delete from roles where name = 'admin'  limit 1;
delete from roles where name = 'user'  limit 1;
delete from roles where name = 'guest'  limit 1;

insert into roles (id, name) values (1, 'super_admin'), (2, 'admin'), (3, 'user'), (4, 'guest');
