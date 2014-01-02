-- For H2 Database
create table fileinfos (
  id bigserial not null primary key,
  md5 varchar(512) not null unique,
  fullpath varchar(512) not null,
  filesize bigint not null,
  created_at timestamp not null,
  updated_at timestamp
)
