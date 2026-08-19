CREATE TABLE IF NOT EXISTS public.r_analytic (uuid UUID NOT NULL PRIMARY KEY, name VARCHAR(255));

CREATE TABLE IF NOT EXISTS public.r_scenario (uuid UUID NOT NULL PRIMARY KEY, name VARCHAR(255));

CREATE TABLE IF NOT EXISTS public.business_associate (
    uuid UUID NOT NULL PRIMARY KEY,
    long_name VARCHAR(255) CONSTRAINT idx_unique_business_associate_long_name UNIQUE,
    short_name VARCHAR(255),
    ba_type VARCHAR(255),
    stockholders VARCHAR(1000)
);

CREATE TABLE IF NOT EXISTS public.header_econom (
    uuid UUID NOT NULL PRIMARY KEY,
    fsd_source TEXT NOT NULL,
    scenario UUID,
    ba_uuid UUID,
    year INTEGER,
    field_uuid UUID,
    created_date TIMESTAMP NOT NULL DEFAULT NOW ()
);

create table if not exists public.project_step (
    uuid uuid not null constraint project_step_pk primary key,
    name varchar(255) not null constraint uc_project_step_name unique,
    type varchar(255) not null,
    horizon_uuid uuid not null,
    code varchar(255) not null,
    ba_uuid uuid,
    equipment_name varchar(255),
    project_name varchar(255)
);

INSERT INTO
    public.project_step (uuid, name, type, horizon_uuid, code, ba_uuid)
VALUES
    (
        '1cca9c73-a973-42c0-98f9-51f7087434e8',
        '051-3001236.Чаядинское.Карбон.Скважины.Новые скважины',
        'Эксплуатационное бурение',
        '639601cc-2a47-4d1c-83b2-224a7bf8bb56',
        '051-2064',
        '99f91cef-ea38-4aa2-8956-98762c406d9a'
    );

create table if not exists public.r_scenario (uuid uuid not null primary key, name varchar(255));

CREATE TABLE IF NOT EXISTS public.project_step_econ (
    uuid UUID not null PRIMARY KEY,
    header_uuid UUID not null,
    project_step_uuid UUID NOT NULL,
    invest_program VARCHAR(10) NOT NULL,
    r_scenario UUID NOT NULL,
    construction_type VARCHAR(30) NOT NULL,
    functional_group TEXT NOT NULL,
    priority VARCHAR(30) NOT NULL,
    project_dependency UUID,
    pir_start_year INTEGER,
    pir_end_year INTEGER,
    smr_start_year INTEGER,
    smr_end_year INTEGER,
    analytic_uuid UUID NOT NULL,
    value NUMERIC(34, 17) NOT NULL
);

INSERT INTO
    public.r_scenario (uuid, name)
VALUES
    (
        '5e78bb8c-64ce-401b-bbd3-cbc18cab3226',
        'Сценарий 1'
    ),
    (
        '5e78bb8c-64ce-401b-bbd3-cbc18cab3626',
        'Сценарий 2'
    );

INSERT INTO
    public.business_associate (uuid, long_name, short_name, ba_type)
VALUES
    (
        '99f91cef-ea38-4aa2-8956-98762c406d9a',
        'ООО "Газпром добыча Ноябрьск"',
        'ООО "Газпром добыча Ноябрьск"',
        null
    );

INSERT INTO
    public.r_analytic (uuid, name)
VALUES
    ('b1c3f8a9-1c2d-4e6b-9f13-97f8d9d4b1a2', 'НДС %'),
    (
        'd8040181-156e-4956-a944-4841844004d1',
        'Капитальные вложения, млн руб.'
    );