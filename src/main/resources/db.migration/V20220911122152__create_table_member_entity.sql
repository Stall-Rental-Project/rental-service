set search_path to mhmarket;

create table member
(
    member_id       uuid primary key default gen_random_uuid(),
    application_id  uuid not null,
    name            varchar(255) not null,
    age             integer not null,
    created_at      timestamptz not null,
    updated_at      timestamptz ,
    unique(application_id, name)
);

alter table member
    add constraint member_application_fk foreign key (application_id) references application (application_id) on delete cascade on update cascade;

