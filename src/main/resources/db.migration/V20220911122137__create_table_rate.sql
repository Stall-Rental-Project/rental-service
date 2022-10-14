set search_path to mhmarket;

create table rate
(
    rate_id          uuid primary key      default gen_random_uuid(),
    rate_code        varchar(128) not null,
    status           int          not null default 0, -- 0 = Inactive, 1 = Active
    type             int          not null,
    created_at       timestamptz  not null default now(),
    updated_at       timestamptz,
    initiator_id     uuid         not null,
    modifier_id      uuid,
    content          text         not null,           -- structured-string based on type
    other_rate_type  int                              -- only set if type = OTHER_RATES_VALUE
);

create unique index rate_code_unique_idx on rate using btree (rate_code);

