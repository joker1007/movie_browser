create table thumbnails (
  id bigserial not null primary key,
  md5 varchar(512) not null unique,
  data blob not null,
  created_at timestamp not null,
  updated_at timestamp
)