use dev;

drop table if exists test.id_table;
create table test.id_table
(
    entity_name varchar(256) not null primary key,
    prev_value  bigint       not null,
    step_size   integer      not null,
    fetch_size  integer      not null
);

alter table test.batch_rule drop constraint fk__batch_rule__batch;
alter table test.task drop constraint fk__task__batch;
alter table test.batch_rule_desc drop constraint fk__batch_rule_desc__batch_rule;

drop table if exists test.batch;
create table test.batch
(
    id         bigint not null primary key,
    company_id integer,
    batch_name varchar(256)
);


drop table if exists test.batch_rule;
create table test.batch_rule
(
    id        bigint not null primary key,
    batch_id  bigint constraint fk__batch_rule__batch references test.batch,
    rule_name varchar(256)
);


drop table if exists test.batch_rule_desc;
create table test.batch_rule_desc
(
--     id          bigint not null primary key,
    rule_id     bigint constraint fk__batch_rule_desc__batch_rule references test.batch_rule,
    description varchar(256)
);


drop table if exists test.task;
create table test.task
(
    id          bigint  not null primary key,
    batch_id    bigint constraint fk__task__batch references test.batch,
    company_id  integer not null,
    employee_id varchar(256)
);

