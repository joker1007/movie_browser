-- For H2 Database
create table thumbnails (
  id bigserial not null primary key,
  md5 varchar(512) not null unique,
  data varchar(512) not null,
  created_at timestamp not null,
  updated_at timestamp not null
)
