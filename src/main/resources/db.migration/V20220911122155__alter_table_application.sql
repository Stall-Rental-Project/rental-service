set search_path to mhmarket;

alter table application
    drop column created_by;
alter table application
    add column created_by uuid;