create schema if not exists postgres;

drop table if exists batch;
create table if not exists batch
(
    id   bigint not null GENERATED always as identity primary key,
    name varchar(256)
);
