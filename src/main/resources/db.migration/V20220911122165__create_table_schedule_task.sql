create schema scheduled;

set search_path to scheduled;

create table scheduled_task
(
    name       varchar(255) primary key,
    type       int         not null,
    started_at timestamptz not null,
    ended_at   timestamptz,
    completed  boolean     not null default false,
    success    boolean     not null default false,
    stacktrace text
);