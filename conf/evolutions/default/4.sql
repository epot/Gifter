# --- !Ups

drop table if exists comment;
drop sequence if exists comment_seq;

create table comment (
  id bigserial not null primary key,
  userid uuid not null,
  objectid bigint not null,
  creationdate timestamp not null,
  category integer not null,
  content varchar(255));

# --- !Downs
