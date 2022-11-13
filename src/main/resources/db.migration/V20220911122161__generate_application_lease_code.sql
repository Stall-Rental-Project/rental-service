set search_path to mhmarket;


create sequence application_lease_sequence increment 1 start 1;
create or replace function generate_application_lease_code(market_type int)
    returns text
    language plpgsql
as
$$
declare
    zero                   text := '0';
    public_market_prefix   text := 'COM';
    code                   text;
    next_value             bigint;
begin
    next_value := nextval('application_lease_sequence');
    code := cast(next_value as text);

    while length(code) < 6
        loop
            code := concat(zero, code);
        end loop;

    code := concat(public_market_prefix, '-', code);

    raise notice 'The generated lease code is %', code;

    return code;
end;
$$;

