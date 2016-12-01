# --- !Ups

create table notification (
  id bigserial not null primary key,
  userid uuid not null,
  objectid bigint not null,
  category integer not null);

# --- !Downs

drop table if exists notification;