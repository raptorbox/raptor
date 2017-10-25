
ALTER TABLE users_roles DROP FOREIGN KEY fk_users_roles_role_id_roles_id;
ALTER TABLE users_roles DROP FOREIGN KEY fk_users_roles_user_id_users_id;

drop table if exists roles;
drop table if exists users_roles;

create table if not exists groups (
    id bigint not null auto_increment, 
    name varchar(255) not null, 
    app_id bigint not null, 
    primary key (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

create table if not exists permissions (
    id bigint not null auto_increment, 
    name varchar(255) not null, 
    primary key (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

create table if not exists groups_permissions (
    group_id bigint not null, 
    permission_id bigint not null
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

create table if not exists users_groups(
    group_id bigint not null, 
    user_id bigint not null
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

alter table groups_permissions add constraint fk_group_perm_gid foreign key (group_id) references groups (id);
alter table groups_permissions add constraint fk_group_perm_pid foreign key (permission_id) references permissions (id);

alter table users_groups add constraint fk_usr_grp_gid foreign key (group_id) references groups (id);
alter table users_groups add constraint fk_usr_grp_uid foreign key (user_id) references users (id);
