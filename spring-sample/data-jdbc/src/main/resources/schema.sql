create schema if not exists test;

set search_path to test;

drop table if exists batch_desc;
drop table if exists batch;

create table if not exists batch
(
    id   bigint not null GENERATED always as identity primary key,
    name varchar(256)
);


create table if not exists batch_desc
(
    batch_ids bigint primary key references batch (id),
    content  varchar(256)
);

