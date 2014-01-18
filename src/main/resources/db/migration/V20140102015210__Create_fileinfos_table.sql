-- For H2 Database
create table fileinfos (
  id bigserial not null primary key,
  md5 varchar(512) not null unique,
  fullpath varchar(512) not null,
  relativePath varchar(512) not null,
  basename varchar(255) not null,
  filesize bigint not null,
  targetId bigint not null,
  created_at timestamp not null,
  updated_at timestamp
)
