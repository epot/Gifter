# --- !Ups


create table oauth1_info
(
   provider character varying(64) not null,
   key text not null,
   token text not null,
   secret text not null,
   created timestamp without time zone not null,
   constraint pk_oauth1_info primary key (provider, key)
) with (oids = false);
    
create table openid_info
(
   provider character varying(64) not null,
   key text not null,
   id text not null,
   attributes text not null,
   created timestamp without time zone not null,
   constraint pk_openid_info primary key (provider, key)
) with (oids = false);
    

create table oauth2_info
(
   provider character varying(64) not null,
   key text not null,
   access_token text not null,
   token_type character varying(64),
   expires_in integer,
   refresh_token character varying(64),
   params text,
   created timestamp without time zone,
   constraint pk_oauth2_info primary key (provider, key)
) with (oids = false);
    
create table users (
  id uuid primary key,
  username character varying(256),
  profiles text[] not null,
  roles character varying(64)[] not null,
  created timestamp not null
) with (oids=false);

create index users_profiles_idx on users using gin (profiles);
create unique index users_username_idx on users using btree (username collate pg_catalog."default");
create index users_roles_idx on users using gin (roles);
    
create table password_info
(
   provider character varying(64) not null,
   key text not null,
   hasher character varying(64) not null,
   password character varying(256) not null,
   salt character varying(256),
   created timestamp without time zone not null,
   constraint pk_password_info primary key (provider, key)
) with (oids = false);

create table requests (
  id uuid primary key not null,
  user_id uuid not null,
  auth_provider character varying(64) not null,
  auth_key text not null,
  remote_address character varying(64) not null,

  method character varying(10) not null,
  host text not null,
  secure boolean not null,
  path text not null,
  query_string text,

  lang text,
  cookie text,
  referrer text,
  user_agent text,
  started timestamp not null,
  duration integer not null,
  status integer not null
) with (oids=false);

create index requests_account_idx on requests using btree (user_id);

alter table requests add constraint requests_users_fk foreign key (user_id) references users (id) on update no action on delete no action;

create table session_info
(
  id text not null,
  provider character varying(64) not null,
  key text not null,
  last_used timestamp without time zone not null,
  expiration timestamp without time zone not null,
  fingerprint text,
  created timestamp without time zone not null,
  constraint pk_session_info primary key (id)
) with (oids = false);

create index idx_session_info_provider_key on session_info (provider, key);

create table user_profiles (
  provider character varying(64) not null,
  key text not null,
  email character varying(256),
  first_name character varying(512),
  last_name character varying(512),
  full_name character varying(512),
  avatar_url character varying(512),
  created timestamp not null
) with (oids=false);

create index user_profiles_email_idx on user_profiles using btree (email collate pg_catalog."default");

alter table user_profiles add constraint user_profiles_provider_key_idx unique (provider, key);

alter table event add column creator_id uuid;
alter table event drop constraint event_creatorid_fkey;
alter table participant add column user_id uuid;
alter table participant drop constraint participant_userid_fkey;
alter table gift add column creator_id uuid;
alter table gift drop constraint gift_creatorid_fkey;
alter table comment add column creator_id uuid;
alter table comment drop constraint comment_creatorid_fkey;
alter table history add column user_id uuid;
alter table history drop constraint history_userid_fkey;

# --- !Downs


drop table if exists openid_info;
drop table if exists oauth1_info;
drop table if exists oauth2_info;
drop index if exists users_profiles_idx;
drop index if exists oauth2_info;
drop index if exists users_username_idx;
drop table if exists password_info;
drop table if exists requests;
drop table if exists requests_account_idx;
drop table if exists session_info;
drop table if exists idx_session_info_provider_key;
drop table if exists user_profiles;
drop table if exists user_profiles_email_idx;
drop table if exists users;

