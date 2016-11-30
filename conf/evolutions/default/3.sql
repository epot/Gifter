# --- !Ups

ALTER TABLE password_info ADD "loginInfoId" BIGINT;
ALTER TABLE oauth1_info ADD "loginInfoId" BIGINT;
ALTER TABLE oauth2_info ADD "loginInfoId" BIGINT;
ALTER TABLE openid_info ADD "loginInfoId" BIGINT;
ALTER TABLE password_info ADD "id" BIGSERIAL;
ALTER TABLE oauth1_info ADD "id" BIGSERIAL;
ALTER TABLE oauth2_info ADD "id" BIGSERIAL;
ALTER TABLE openid_info DROP "id";
ALTER TABLE openid_info ADD "id" BIGSERIAL;
ALTER TABLE password_info DROP CONSTRAINT pk_password_info;
ALTER TABLE oauth1_info DROP CONSTRAINT pk_oauth1_info;
ALTER TABLE oauth2_info DROP CONSTRAINT pk_oauth2_info;
ALTER TABLE openid_info DROP CONSTRAINT pk_openid_info;
ALTER TABLE password_info ALTER COLUMN key DROP NOT NULL;
ALTER TABLE password_info ALTER COLUMN provider DROP NOT NULL;
ALTER TABLE oauth1_info ALTER COLUMN key DROP NOT NULL;
ALTER TABLE oauth1_info ALTER COLUMN provider DROP NOT NULL;
ALTER TABLE oauth2_info ALTER COLUMN key DROP NOT NULL;
ALTER TABLE oauth2_info ALTER COLUMN provider DROP NOT NULL;
ALTER TABLE openid_info ALTER COLUMN key DROP NOT NULL;
ALTER TABLE openid_info ALTER COLUMN provider DROP NOT NULL;
ALTER TABLE password_info ALTER COLUMN created DROP NOT NULL;
ALTER TABLE oauth1_info ALTER COLUMN created DROP NOT NULL;
ALTER TABLE oauth2_info ALTER COLUMN created DROP NOT NULL;
ALTER TABLE openid_info ALTER COLUMN created DROP NOT NULL;

create table "login_info" (id BIGSERIAL not null primary key,"providerID" VARCHAR NOT NULL,"providerKey" VARCHAR NOT NULL);
create table "user_login_info" ("userID" uuid NOT NULL,"loginInfoId" BIGINT NOT NULL);
create table "openid_attributes" ("id" VARCHAR NOT NULL,"key" VARCHAR NOT NULL,"value" VARCHAR NOT NULL);

alter table user_profiles add column user_id uuid;

alter table user_profiles drop constraint user_profiles_provider_key_idx;
ALTER TABLE user_profiles DROP "provider";
ALTER TABLE user_profiles DROP "key";

ALTER TABLE event ALTER COLUMN id SET DEFAULT nextval('event_seq');
ALTER SEQUENCE event_seq OWNED BY event.id;
ALTER TABLE comment ALTER COLUMN id SET DEFAULT nextval('comment_seq');
ALTER SEQUENCE comment_seq OWNED BY comment.id;
ALTER TABLE gift ALTER COLUMN id SET DEFAULT nextval('gift_seq');
ALTER SEQUENCE gift_seq OWNED BY gift.id;
ALTER TABLE participant ALTER COLUMN id SET DEFAULT nextval('participant_seq');
ALTER SEQUENCE participant_seq OWNED BY participant.id;
ALTER TABLE history ALTER COLUMN id SET DEFAULT nextval('history_seq');
ALTER SEQUENCE history_seq OWNED BY history.id;

ALTER TABLE gift ALTER COLUMN creatorid DROP NOT NULL;
ALTER TABLE participant ALTER COLUMN userid DROP NOT NULL;
ALTER TABLE history ALTER COLUMN userid DROP NOT NULL;
ALTER TABLE comment ALTER COLUMN creatorid DROP NOT NULL;

INSERT INTO login_info ("providerID", "providerKey") (SELECT provider, key FROM oauth2_info);
INSERT INTO login_info ("providerID", "providerKey") (SELECT provider, key FROM password_info);
INSERT INTO user_profiles (user_id, full_name, created) (SELECT id, username, created FROM users);
insert into user_login_info ("userID", "loginInfoId") (SELECT distinct users.id, login_info.id from users, login_info where login_info."providerID" || ':' || login_info."providerKey" = ANY(users.profiles));
update oauth2_info set "loginInfoId"=login_info.id from login_info where login_info."providerID" =oauth2_info.provider and login_info."providerKey" = oauth2_info.key;
update password_info set "loginInfoId"=login_info.id from login_info where login_info."providerID" =password_info.provider and login_info."providerKey" = password_info.key;

# --- !Downs

drop table if exists openid_attributes;
drop table if exists user_login_info;
drop table if exists login_info;

delete from user_profiles;

drop SEQUENCE if exists event_id_seq;
ALTER TABLE event ALTER COLUMN id drop DEFAULT;

ALTER TABLE password_info DROP "loginInfoId";
ALTER TABLE oauth1_info DROP "loginInfoId";
ALTER TABLE oauth2_info DROP "loginInfoId";
ALTER TABLE openid_info DROP "loginInfoId";
ALTER TABLE password_info DROP "id";
ALTER TABLE oauth1_info DROP "id";
ALTER TABLE oauth2_info DROP "id";
ALTER TABLE openid_info DROP "id";
alter table openid_info add column id text not null;
ALTER TABLE user_profiles DROP "user_id";
alter table user_profiles add column provider character varying(64) not null;
alter table user_profiles add column key text not null;
alter table user_profiles add constraint user_profiles_provider_key_idx unique (provider, key);
ALTER TABLE user_profiles ALTER COLUMN key SET NOT NULL;

alter table password_info add constraint pk_password_info primary key (provider, key);
alter table oauth1_info add constraint pk_oauth1_info primary key (provider, key);
alter table oauth2_info add constraint pk_oauth2_info primary key (provider, key);
alter table openid_info add constraint pk_openid_info primary key (provider, key);


ALTER TABLE password_info ALTER COLUMN key SET NOT NULL;
ALTER TABLE password_info ALTER COLUMN provider SET NOT NULL;
ALTER TABLE oauth1_info ALTER COLUMN key SET NOT NULL;
ALTER TABLE oauth1_info ALTER COLUMN provider SET NOT NULL;
ALTER TABLE oauth2_info ALTER COLUMN key SET NOT NULL;
ALTER TABLE oauth2_info ALTER COLUMN provider SET NOT NULL;
ALTER TABLE openid_info ALTER COLUMN provider SET NOT NULL;


