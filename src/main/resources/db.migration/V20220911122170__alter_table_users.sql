set search_path to mhmarket;

alter table users
    drop column telephone;

alter table users
    add column telephone varchar(255);
