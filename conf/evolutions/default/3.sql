# --- !Ups

ALTER TABLE password_info ADD "loginInfoId" BIGINT;
ALTER TABLE oauth1_info ADD "loginInfoId" BIGINT;
ALTER TABLE oauth2_info ADD "loginInfoId" BIGINT;
ALTER TABLE openid_info ADD "loginInfoId" BIGINT;
ALTER TABLE password_info ADD "id" BIGSERIAL;
ALTER TABLE oauth1_info ADD "id" BIGINT;
ALTER TABLE oauth2_info ADD "id" BIGINT;
ALTER TABLE openid_info ADD "id" BIGINT;
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

INSERT INTO login_info (id, providerID, providerKey) SELECT default, 'provider', 'key' FROM oauth2_info;
INSERT INTO login_info (id, providerID, providerKey) SELECT default, 'provider', 'key' FROM password_info;
INSERT INTO login_info (id, providerID, providerKey) SELECT default, 'provider', 'key' FROM oauth1_info;
INSERT INTO login_info (id, providerID, providerKey) SELECT default, 'provider', 'key' FROM openid_info;
alter table user_profiles add column user_id uuid;

alter table user_profiles drop constraint user_profiles_provider_key_idx;
ALTER TABLE user_profiles DROP "provider";
ALTER TABLE user_profiles DROP "text";
ALTER TABLE user_profiles ALTER COLUMN key DROP NOT NULL;

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



# --- !Downs

drop table if exists openid_attributes;
drop table if exists user_login_info;
drop table if exists login_info;

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
ALTER TABLE user_profiles DROP "user_id";
alter table user_profiles add column provider character varying(64) not null;
alter table user_profiles add column key text not null;
alter table user_profiles add constraint user_profiles_provider_key_idx unique (provider, key);
ALTER TABLE user_profiles ALTER COLUMN key SET NOT NULL;


ALTER TABLE password_info ALTER COLUMN key SET NOT NULL;
ALTER TABLE password_info ALTER COLUMN provider SET NOT NULL;
ALTER TABLE oauth1_info ALTER COLUMN key SET NOT NULL;
ALTER TABLE oauth1_info ALTER COLUMN provider SET NOT NULL;
ALTER TABLE oauth2_info ALTER COLUMN key SET NOT NULL;
ALTER TABLE oauth2_info ALTER COLUMN provider SET NOT NULL;
ALTER TABLE openid_info ALTER COLUMN provider SET NOT NULL;


