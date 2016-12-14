# --- !Ups


ALTER TABLE event ALTER COLUMN creatorid DROP NOT NULL;
create table auth_token (
  id uuid primary key,
  userid uuid not null,
  expiry timestamp not null
);

# --- !Downs

ALTER TABLE event ALTER COLUMN creatorid SET NOT NULL;
drop table if exists auth_token;