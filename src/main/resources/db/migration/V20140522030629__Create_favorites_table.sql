create table favorites(id bigserial not null primary key, fileinfo_id bigint not null);

create unique index fileinfo_id_on_favorites on favorites(fileinfo_id);