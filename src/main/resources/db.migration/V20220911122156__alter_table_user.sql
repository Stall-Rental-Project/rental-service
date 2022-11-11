set search_path to mhmarket;

alter table users
    drop column province_info;
alter table users
    drop column province_address;
alter table users
    add column province varchar(255) not null ;
