-- For H2 Database
create table targets (
  id bigserial not null primary key,
  fullpath varchar(512) not null,
  as_path varchar(512),
  last_updated_at timestamp,
  created_at timestamp not null,
  updated_at timestamp
)
