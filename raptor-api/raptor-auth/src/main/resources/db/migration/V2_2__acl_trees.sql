
create table if not exists trees (
    id bigint not null auto_increment, 
    uuid varchar(255) not null, 
    owner_id bigint, 
    primary key (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

alter table trees add constraint fk_trees_users_id foreign key (owner_id) references users (id);
