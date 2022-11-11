set search_path to mhmarket;
create sequence nsa_sequence increment 1 start 1;
create sequence rsa_sequence increment 1 start 1;

create or replace function generate_application_code(app_type integer) returns text
    language plpgsql
as
$$
declare
    zero       text := '0';
    code       text;
    next_value bigint;
    nsa_prefix text := 'NSA';
    rsa_prefix text := 'RSA';
begin
    if app_type = 0 then
        next_value = nextval('nsa_sequence');
    elsif app_type = 1 then
        next_value = nextval('rsa_sequence');
    end if;

    code := cast(next_value as text);
    while length(code) < 6
        loop
            code := concat(zero, code);
        end loop;

    if app_type = 0 then
        code = concat(nsa_prefix, code);
    elsif app_type = 1 then
        code = concat(rsa_prefix, code);
    end if;

    return code;
end;
$$;