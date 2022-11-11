set search_path to mhmarket;

alter table users
    add column email varchar(255) not null;
alter table application
    add column payment_status integer;
alter table application
    add column stall_type integer;
alter table application
    add column stall_class integer;
alter table application
    add column stall_area double precision;