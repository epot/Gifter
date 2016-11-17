# --- !Ups
create table auth_token (
  id bigint not null primary key,
  userid bigint not null,
  expiry timestamp not null,
  foreign key (userid) references user_table(id) on delete cascade
);

# --- !Downs


drop table if exists auth_token;

