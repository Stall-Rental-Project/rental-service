set search_path to mhmarket;

alter table rate
    drop column initiator_id;
alter table rate
    drop column modifier_id;