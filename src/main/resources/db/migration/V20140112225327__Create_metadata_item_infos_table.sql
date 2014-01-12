create table metadata_item_infos(
  file_metadata_id bigint not null,
  item_info_id bigint not null
);

create unique index file_metadata_id_and_item_info_id_on_metadata_item_infos on metadata_item_infos(file_metadata_id, item_info_id);
create unique index item_info_id_and_file_metadata_id_on_metadata_item_infos on metadata_item_infos(item_info_id, file_metadata_id);