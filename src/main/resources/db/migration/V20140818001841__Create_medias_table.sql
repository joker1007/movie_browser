-- For H2 Database
create table medias (
  id bigserial not null primary key,
  md5 varchar(128) not null,
  fullpath varchar(512) not null,
  relative_path varchar(512) not null,
  basename varchar(512) not null,
  filesize bigint not null,
  target_id bigint not null,
  created_at timestamp not null,
  updated_at timestamp not null
)
