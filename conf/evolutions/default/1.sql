# --- !Ups

create table user (
  id bigint(20) not null primary key,
  email varchar(255) not null unique,
  name varchar(255) not null,
  isNotMember boolean not null
);

create sequence user_seq;


create table identities (
  userId bigint(20) not null,
  adapter int not null,
  password varchar(255) not null,
  foreign key (userId) references user(id)
);

create sequence identities_seq;

# --- !Downs

drop table if exists user;
drop sequence if exists user_seq;
drop table if exists identities;
drop sequence if exists identities_seq;