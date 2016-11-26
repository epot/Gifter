# --- !Ups

ALTER TABLE password_info ADD "loginInfoId" BIGINT;
ALTER TABLE oauth1_info ADD "loginInfoId" BIGINT;
ALTER TABLE oauth2_info ADD "loginInfoId" BIGINT;
ALTER TABLE openid_info ADD "loginInfoId" BIGINT;

create table "login_info" (id bigint not null primary key,"providerID" VARCHAR NOT NULL,"providerKey" VARCHAR NOT NULL);
create table "user_login_info" ("userID" uuid NOT NULL,"loginInfoId" BIGINT NOT NULL);
create table "openid_attributes" ("id" VARCHAR NOT NULL,"key" VARCHAR NOT NULL,"value" VARCHAR NOT NULL);

INSERT INTO login_info (id, providerID, providerKey)
SELECT default, 'provider', 'key'
FROM oauth2_info;
INSERT INTO login_info (id, providerID, providerKey)
SELECT default, 'provider', 'key'
FROM password_info;
INSERT INTO login_info (id, providerID, providerKey)
SELECT default, 'provider', 'key'
FROM oauth1_info;
INSERT INTO login_info (id, providerID, providerKey)
SELECT default, 'provider', 'key'
FROM openid_info;
alter table user_profiles add column user_id uuid;

alter table user_profiles drop constraint user_profiles_provider_key_idx;
ALTER TABLE user_profiles DROP "provider";
ALTER TABLE user_profiles DROP "text";



# --- !Downs

drop table if exists openid_attributes;
drop table if exists user_login_info;
drop table if exists login_info;

ALTER TABLE password_info DROP "loginInfoId";
ALTER TABLE oauth1_info DROP "loginInfoId";
ALTER TABLE oauth2_info DROP "loginInfoId";
ALTER TABLE openid_info DROP "loginInfoId";
ALTER TABLE user_profiles DROP "user_id";
alter table user_profiles add column provider character varying(64) not null;
alter table user_profiles add column key text not null;
alter table user_profiles add constraint user_profiles_provider_key_idx unique (provider, key);

