set search_path to mhmarket;

alter table application
    add column reminded_payment_date timestamptz;