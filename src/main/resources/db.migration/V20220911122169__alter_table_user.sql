set search_path to mhmarket;

alter table users
    add column telephone varchar(255) not null default '';

alter table users
    drop column date_of_birth;

alter table users
    add column date_of_birth timestamptz not null default now();
