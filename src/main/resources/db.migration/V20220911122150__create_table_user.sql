set search_path to mhmarket;

create table users
(
    user_id         uuid primary key default gen_random_uuid(),
    first_name      varchar(255) not null,
    middle_name     varchar(255),
    last_name       varchar(255) not null,
    sex             integer not null default 0, -- 0 Female, 1 Male
    marital_status  integer not null,
    date_of_birth   timestamptz not null,
    place_of_birth  varchar(255) not null,
    farther_name    varchar(255) not null,
    mother_name     varchar(255) not null,
    house_number    varchar(255),
    street          varchar(255),
    province        varchar(255) not null,
    city            varchar(255) not null,
    ward        varchar(255) not null,
    zipcode         varchar(255) not null,
    district    uuid not null,
    created_at      timestamptz not null,
    updated_at      timestamptz
);

