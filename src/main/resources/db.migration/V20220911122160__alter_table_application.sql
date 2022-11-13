set search_path to mhmarket;

alter table application
    add column cancel_reason text;
alter table application
    add column paid_security_fee double precision;
alter table application
    add column paid_total_amount_due double precision;