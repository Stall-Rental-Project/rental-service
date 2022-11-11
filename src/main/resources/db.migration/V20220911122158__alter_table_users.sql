set search_path to mhmarket;

alter table users
    drop column district;
alter table users
    add column district varchar(255);