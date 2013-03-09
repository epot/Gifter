# --- !Ups

create table user (
  id bigint(20) not null primary key,
  name varchar(255) not null,
  isNotMember boolean not null
);

create sequence user_seq;


create table identity (
  userId bigint(20) not null,
  email varchar(255) not null unique,
  adapter int not null,
  hash varchar(255) not null,
  foreign key (userId) references user(id)
);

# --- !Downs

drop table if exists user;
drop sequence if exists user_seq;
drop table if exists identity;