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
    header_uuid UUID ,
    project_step_uuid UUID ,
    invest_program VARCHAR(10) ,
    r_scenario UUID ,
    construction_type VARCHAR(30) ,
    functional_group TEXT ,
    priority VARCHAR(30) ,
    complex_reconstruction_program TEXT ,
    project_dependency UUID,
    pir_start_year INTEGER,
    pir_end_year INTEGER,
    smr_start_year INTEGER,
    smr_end_year INTEGER,
    project_step_completion_year INTEGER,
    analytic_uuid UUID ,
    value NUMERIC(34, 17)
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

create table if not exists public.field (
    uuid uuid not null constraint field_pk primary key,
    name varchar(255),
    ba_uuid uuid,
    area_uuid uuid,
    type_uuid uuid,
    licenses varchar(255),
    esg boolean,
    int integer
);

INSERT INTO
    public.field (
        uuid,
        name,
        ba_uuid,
        area_uuid,
        type_uuid,
        licenses,
        esg,
        int
    )
VALUES
    (
        '3986ea1c-ac29-4cf7-8a82-5e12a1063b80',
        'Ямбургское',
        'f271eb01-0b47-45dd-843b-d05ee20dbe8c',
        '1a0b5abb-473d-42d3-8434-43ab5862da30',
        '20c668bf-e0de-4e41-897e-513db70be197',
        'ШОМ006641НР 19.08.2022',
        false,
        null
    );