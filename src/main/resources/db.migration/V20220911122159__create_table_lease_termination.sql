set search_path to mhmarket;

create table lease_termination
(
    termination_id uuid primary key      default gen_random_uuid(),
    application_id uuid         not null,
    reason         text,
    end_date       timestamptz  not null,
    created_by     varchar(256) not null,
    accepted       bool         not null default false,
    created_at     timestamptz  not null default now(),
    updated_at     timestamptz,
    status         integer      not null
);