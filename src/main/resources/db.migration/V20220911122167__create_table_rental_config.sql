set search_path to mhmarket;

create table rental_config
(
    id    bigserial primary key,
    key   varchar(128) not null unique,
    value varchar(512) not null
);
insert into rental_config (key, value)
values ('lease.terminate_cron_expression', '0 0 0 * * *');
update rental_config
set value = '0 2 16 * * *'
where key = 'lease.inactivate_cron_expression';