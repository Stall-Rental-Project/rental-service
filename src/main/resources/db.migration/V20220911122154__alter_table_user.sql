set search_path to mhmarket;

alter table users
    drop column province;
alter table users
    add column province_info varchar(255) not null ;
alter table users
    add column province_address varchar(255) not null ;
