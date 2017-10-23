
create table if not exists apps (
    id bigint not null auto_increment, 
    uuid varchar(255) not null, 
    owner_id bigint, 
    primary key (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

alter table apps add constraint fk_apps_users_id foreign key (owner_id) references users (id);
