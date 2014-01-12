create table item_infos(
  id bigserial not null primary key,
  dmm_id varchar(32) not null unique,
  kind varchar(32) not null,
  name varchar(255) not null,
  created_at timestamp not null,
  updated_at timestamp
)