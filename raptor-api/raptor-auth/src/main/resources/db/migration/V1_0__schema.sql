
CREATE DATABASE IF NOT EXISTS raptor;
USE raptor;

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

create table if not exists devices (
    id bigint not null auto_increment, 
    uuid varchar(255) not null, 
    owner_id bigint, 
    parent_id bigint, 
    primary key (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

create table if not exists roles (
    id bigint not null auto_increment, 
    name varchar(255) not null, 
    primary key (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

create table if not exists tokens (
    id bigint not null auto_increment, 
    parent_id bigint, 
    created datetime not null, 
    enabled bit not null, 
    expires bigint, 
    name varchar(255) not null, 
    secret varchar(255), 
    token varchar(255) not null, 
    token_type varchar(255) not null, 
    type varchar(255), 
    device_id bigint, 
    user_id bigint, 
    primary key (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

create table if not exists users (
    id bigint not null auto_increment, 
    created datetime not null, 
    email varchar(128) not null, 
    enabled bit not null, 
    firstname varchar(64), 
    last_password_reset datetime not null, 
    lastname varchar(64),
    password varchar(128) not null, 
    username varchar(128) not null, 
    uuid varchar(255) not null, 
    primary key (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

create table if not exists users_roles (
    user_id bigint not null, 
    role_id bigint not null
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

create table if not exists authorities (
    username varchar(128) not null, 
    authority varchar(255) not null
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE if not exists acl_sid (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    principal BOOLEAN NOT NULL,
    sid VARCHAR(100) NOT NULL,
    UNIQUE KEY unique_acl_sid (sid, principal)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE if not exists acl_class (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    class VARCHAR(100) NOT NULL,
    UNIQUE KEY uk_acl_class (class)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE if not exists  acl_object_identity (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    object_id_class BIGINT UNSIGNED NOT NULL,
    object_id_identity BIGINT NOT NULL,
    parent_object BIGINT UNSIGNED,
    owner_sid BIGINT UNSIGNED,
    entries_inheriting BOOLEAN NOT NULL,
    UNIQUE KEY uk_acl_object_identity (object_id_class, object_id_identity),
    CONSTRAINT fk_acl_object_identity_parent FOREIGN KEY (parent_object) REFERENCES acl_object_identity (id),
    CONSTRAINT fk_acl_object_identity_class FOREIGN KEY (object_id_class) REFERENCES acl_class (id),
    CONSTRAINT fk_acl_object_identity_owner FOREIGN KEY (owner_sid) REFERENCES acl_sid (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE if not exists acl_entry (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    acl_object_identity BIGINT UNSIGNED NOT NULL,
    ace_order INTEGER NOT NULL,
    sid BIGINT UNSIGNED NOT NULL,
    mask INTEGER UNSIGNED NOT NULL,
    granting BOOLEAN NOT NULL,
    audit_success BOOLEAN NOT NULL,
    audit_failure BOOLEAN NOT NULL,
    UNIQUE KEY unique_acl_entry (acl_object_identity, ace_order),
    CONSTRAINT fk_acl_entry_object FOREIGN KEY (acl_object_identity) REFERENCES acl_object_identity (id),
    CONSTRAINT fk_acl_entry_acl FOREIGN KEY (sid) REFERENCES acl_sid (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


alter table users add constraint idx_username unique (username);
alter table devices add constraint fk_devices_users_id foreign key (owner_id) references users (id);
alter table devices add constraint fk_devices_parent_id_devices_id  foreign key (parent_id) references devices (id);

alter table tokens add constraint idx_tokens_token  unique (token);
alter table tokens add constraint fk_tokens_device_id_id  foreign key (device_id) references devices (id);
alter table tokens add constraint fk_tokens_user_id_users_id foreign key (user_id) references users (id);
alter table tokens  add constraint fk_tkns_prnt_id_tkn_id  foreign key (parent_id) references tokens (id);

alter table users_roles add constraint fk_users_roles_role_id_roles_id foreign key (role_id) references roles (id);
alter table users_roles add constraint fk_users_roles_user_id_users_id foreign key (user_id) references users (id);

alter table authorities add constraint idx_auth_username unique (username, authority);
alter table authorities add constraint fk_auth_users foreign key (username) references users (username);
