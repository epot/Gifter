# --- !Ups

create table user (
  id bigint(20) not null primary key,
  name varchar(255) not null,
  isNotMember boolean not null
);

create sequence user_seq;


create table identity (
  userid bigint(20) not null,
  email varchar(255) not null unique,
  adapter int not null,
  hash varchar(255) not null,
  foreign key (userid) references user(id) on delete cascade
);


create table event (
  id bigint(20) not null primary key,
  creatorid bigint(20) not null,
  name varchar(255) not null,
  date timestamp not null,
  type int not null,
  foreign key (creatorid) references user(id) on delete cascade
);

create sequence event_seq;

create table participant(
  id bigint(20) not null primary key,
  userid bigint(20) not null,
  eventid bigint(20) not null,
  role int not null,
  foreign key (userid) references user(id) on delete cascade,
  foreign key (eventid) references event(id) on delete cascade
);
create sequence participant_seq;

create table gift (
  id bigint(20) not null primary key,
  creatorid bigint(20) not null,
  eventid bigint(20) not null,
  creationDate timestamp not null,
  content nvarchar(max) not null,
  foreign key (creatorid) references user(id) on delete cascade,
  foreign key (eventid) references event(id) on delete cascade
);
create sequence gift_seq;

create table comment (
  id bigint(20) not null primary key,
  creatorid bigint(20) not null,
  giftid bigint(20) not null,
  content varchar(255),
  foreign key (creatorid) references user(id) on delete cascade,
  foreign key (giftid) references gift(id) on delete cascade
);
create sequence comment_seq;

create table history (
  id bigint(20) not null primary key,
  objectid bigint(20) not null,
  userid bigint(20) not null,
  creationdate timestamp not null,
  category varchar(255) not null,
  content varchar(4000) not null,
  foreign key (userid) references user(id) on delete cascade
);

create sequence history_seq;


# --- !Downs

drop table if exists user;
drop sequence if exists user_seq;
drop table if exists identity;
drop table if exists event;
drop sequence if exists event_seq;
drop table if exists participant;
drop sequence if exists participant_seq;
drop table if exists gift;
drop sequence if exists gift_seq;
drop table if exists comment;
drop sequence if exists comment_seq;
drop table if exists history;
drop sequence if exists history_seq;