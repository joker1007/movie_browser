-- For H2 Database
create table fileinfos (
  id bigserial not null primary key,
  md5 varchar(512) not null,
  fullpath varchar(512) not null,
  relative_path varchar(512) not null,
  basename varchar(255) not null,
  filesize bigint not null,
  target_Id bigint not null,
  created_at timestamp not null,
  updated_at timestamp
)
