create schema if not exists test;
set schema test;

-- drop table if exists id_table;
create table if not exists id_table
(
    entity_name varchar(256) not null primary key,
    prev_value  bigint       not null,
    step_size   integer      not null,
    fetch_size  integer      not null
);

insert into id_table
values ('for_lock', 1, 1, 1);



alter table if exists batch_rule drop constraint fk__batch_rule__batch;
alter table if exists task drop constraint fk__task__batch;
alter table if exists batch_rule_desc drop constraint fk__batch_rule_desc__batch_rule;

drop table if exists batch;
create table if not exists batch
(
    id         bigint not null primary key,
    company_id integer,
    batch_name varchar(256)
);


drop table if exists batch_rule;
create table if not exists batch_rule
(
    id        bigint not null primary key,
    batch_id  bigint constraint fk__batch_rule__batch references batch,
    rule_name varchar(256)
);


drop table if exists batch_rule_desc;
create table if not exists batch_rule_desc
(
--     id          bigint not null primary key,
    rule_id     bigint constraint fk__batch_rule_desc__batch_rule references batch_rule,
    description varchar(256)
);


drop table if exists task;
create table if not exists task
(
    id          bigint  not null primary key,
    batch_id    bigint constraint fk__task__batch references batch,
    company_id  integer not null,
    employee_id varchar(256)
);

