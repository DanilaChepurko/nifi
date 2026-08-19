
create table if not exists public.business_associate
(
    uuid       uuid not null
        primary key,
    long_name  varchar(255),
    short_name varchar(255),
    ba_type    varchar(255)
);

create table if not exists public.field
(
    uuid       uuid not null
            primary key,
    name       varchar(255)
      ,
    field_uuid uuid
);
create table if not exists public.r_version
(
    uuid       uuid not null
            primary key,
    name       varchar(255)
    );

create table if not exists public.header_econom
(
    uuid uuid not null,
    fsd_source text,
    year int,
    field_uuid uuid,
    ba_uuid uuid,
    horizon_uuid uuid,
    scenario uuid,
    version_uuid uuid,
    iteration int,
    name varchar(255),
    created_date TIMESTAMP
);


CREATE TABLE if not exists public.project_event (
    uuid UUID NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);
CREATE TABLE if not exists public.equipment (
    uuid UUID NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE if not exists public.equipment_in_use (
    uuid UUID NOT NULL PRIMARY KEY,
    header_uuid UUID NOT NULL,
    equipment_uuid UUID NOT NULL,
    project_event_uuid UUID NOT NULL,
    year INTEGER NOT NULL,
    measure_unit VARCHAR(55),
    physical_volume NUMERIC(34,17) NOT NULL,
    estimated_cost NUMERIC(34,17) NOT NULL,
    functional_group varchar(255)

);


-- Исправленный init.sql
-- ... существующий код ...

INSERT INTO public.business_associate (uuid, long_name)
VALUES ('99f92cef-ea38-4aa2-8956-98762c406d9a', 'ООО "Газпром добыча Астрахань"')
ON CONFLICT (uuid) DO NOTHING;

INSERT INTO public.field (uuid, name)
VALUES ('99f91cef-ea38-4aa2-8956-98762c406d9b', 'Астраханское')
ON CONFLICT (uuid) DO NOTHING;

INSERT INTO public.project_event (uuid, name)
VALUES ('99f91cef-ea38-4aa2-8956-98762c406d9a', '051-2000005.(1.1.0.1).Астраханское.Карбон.Р.С.Скважины.ФА')
ON CONFLICT (uuid) DO NOTHING;

INSERT INTO public.project_event (uuid, name)
VALUES ('cc2ca3c1-eeeb-4767-8bc6-28ed5c874330', '051-2000006.(1.1.0.1).Астраханское.Карбон.Р.П.ДКС.Привод ГПА')
ON CONFLICT (uuid) DO NOTHING;

INSERT INTO public.equipment (uuid, name)
VALUES ('cc2ca3c1-eeeb-4767-8bc7-28ed5c874330', 'ГПА')
ON CONFLICT (uuid) DO NOTHING;

INSERT INTO public.equipment (uuid, name)
VALUES ('cc2ca3c1-eeeb-4767-8bc8-28ed5c874330', 'ФА')
ON CONFLICT (uuid) DO NOTHING;

INSERT INTO public.r_version (uuid, name)
VALUES ('cc2ca3c1-eeeb-4767-8bc8-28ed4c874330', 'Вариант №1')
ON CONFLICT (uuid) DO NOTHING;