create table if not exists public.header_project
(
    uuid                    uuid not null
        constraint header_project_pk
            primary key,
    scenario                uuid,
    year                    integer,
    version_uuid            uuid,
    name                    varchar(255),
    created_date            timestamp,
    updated_date            timestamp,
    development_method_uuid uuid,
    pool_uuid               uuid,
    facility_uuid           uuid,
    horizon_uuid            uuid,
    model_date              timestamp,
    ba_uuid                 uuid
);

create table if not exists public.pden_vol_summary_project
(
    header_uuid      uuid not null
        constraint pden_vol_summary_project_fk
            references public.header_project,
    facility_uuid    uuid,
    pool_uuid        uuid,
    horizon_uuid     uuid,
    analytics_uuid   uuid,
    period_type_uuid uuid,
    start_date       timestamp,
    value            numeric(34, 17)
);

create table if not exists public.pden_option
(
    header_uuid       uuid not null
        constraint pden_option_fk
            references public.header_project,
    project_step_uuid uuid,
    equipment_uuid uuid,
    period_type_uuid  uuid,
    start_date        timestamp,
    volume            numeric(34, 17),
    measure_unit        varchar(100),
    coment text
);

create table if not exists public.r_analytic
(
    uuid uuid not null
        primary key,
    name varchar(255)
);

create table if not exists public.pool
(
    uuid         uuid not null
        constraint pool_pk
            primary key,
    name         varchar(255),
    ba_uuid      uuid,
    field_uuid   uuid,
    horizon_uuid uuid,
    esg          boolean,
    license      boolean,
    drod         integer,
    licenses     varchar(500)
);

create table if not exists public.facility
(
    uuid              uuid not null
        primary key,
    name              varchar(255)
        constraint idx_unique_facility_name
            unique,
    field_uuid        uuid,
    horizon_uuid      uuid,
    horizon_area_uuid uuid
);
create table if not exists public.horizon
(
    uuid              uuid not null
        primary key,
    name  varchar(255)
);
create table if not exists public.r_scenario
(
    uuid uuid not null
        primary key,
    name varchar(255)
);

create table if not exists public.r_version
(
    uuid uuid not null
        primary key,
    name varchar(255)
);

create table if not exists public.project_step
(
    uuid         uuid,
    name         varchar(255),
    type         varchar(255),
    horizon_uuid uuid,
    project_uuid uuid,
    status       varchar(255),
    ba_uuid      uuid,
    f1           integer,
    l2           integer,
    jhgjhg       integer,
    kind         varchar(255),
    address      varchar(255),
    constraint idx_unique_project_step_name_project_uuid
        unique (name, project_uuid)
);
CREATE TABLE if not exists public.equipment (
    uuid UUID NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

create table public.r_development_method
(
    uuid uuid not null
        constraint r_development_method_pk
            primary key,
    name varchar(255)
);

create table if not exists public.r_period_type
(
    uuid uuid not null
        constraint r_period_type_pk
            primary key,
    name varchar(255)
);

create table if not exists public.business_associate
(
    uuid         uuid not null
        primary key,
    long_name    varchar(255)
        constraint idx_unique_business_associate_long_name
            unique,
    short_name   varchar(255),
    ba_type      varchar(255),
    stockholders varchar(1000)
);

create table if not exists public.recom
(
    header_uuid         uuid not null
            constraint index_header_project
                unique,
    obosnov varchar(500)
);



INSERT INTO public.r_analytic (uuid, name)
VALUES ('759238ae-d6d9-49f4-805f-f674aefdf600', 'Запасы.Нефти.млн. т');

INSERT INTO public.r_analytic (uuid, name)
VALUES ('759238ae-d6d9-49f4-805f-f674aefdf5fd', 'Начальная добыча.Нефти.млн. т');


INSERT INTO public.r_analytic (uuid, name)
VALUES ('759238ae-d6d9-49f4-805f-f674aefdf5fe', 'Добыча газа. сепарации.млрд. м3');


INSERT INTO public.r_analytic (uuid, name)
VALUES ('759238ae-d6d9-49f4-805f-f674aefdf5fb', 'Добыча газа.сухой.млрд. м3');

INSERT INTO public.r_analytic (uuid, name)
VALUES ('759238ae-d6d9-49f4-805f-f674aefdf5fa', 'Запасы.Сухого газа.млрд. м3');
INSERT INTO public.r_analytic (uuid, name)
VALUES ('759238ae-d6d9-49f4-805f-f674aefdf5fc', 'Начальная добыча.Стабильного конденсата.млн. т');

INSERT INTO public.facility (uuid, name, field_uuid, horizon_uuid)
VALUES ('284adb90-7728-43ef-b672-c61a4a5183c6', 'Астраханское.УППГ', null, null);


INSERT INTO public.pool (uuid, name)
VALUES ('d0367fcb-3a33-406a-b35d-3efb936bccd1', 'Астраханское.Карбон.залежь башкирского яруса C2b');

insert into public.horizon(uuid,name)
values('d0367fcb-3a33-406a-b35d-3efb936bcad1','Астраханское.Карбон');

INSERT INTO public.r_scenario (uuid, name)
VALUES ('5e78bb8c-64ce-401b-bbd3-cbc18cab3226', 'Уровни добычи из АН');
INSERT INTO public.r_version (uuid, name)
VALUES ('8354ea0c-3fc9-4cf2-8e7d-56db9f8b9ff9', 'Утвержденный');


INSERT INTO public.r_period_type (uuid, name)
VALUES ('2123fe0a-33bd-4324-b312-696df40b0a7c', 'year');
INSERT INTO public.r_period_type (uuid, name)
VALUES ('d6bc16a4-391c-44e5-bf3d-571ebc82a22d', 'day');
INSERT INTO public.r_period_type (uuid, name)
VALUES ('157badbb-c88f-45bb-8eb7-1275b47970d5', 'month');
INSERT INTO public.r_period_type (uuid, name)
VALUES ('157badbb-c88f-45bb-8eb7-1275b47970d4', 'quarter');


INSERT INTO public.equipment (uuid, name)
VALUES ('1cca9c73-a973-42c0-98f9-51f4587434e8', 'Новые скважины');


INSERT INTO public.project_step (uuid, name, type, horizon_uuid, project_uuid, status, ba_uuid, f1, l2, jhgjhg, kind,
                                 address)
VALUES ('91c60f1b-61be-4153-8861-686a9160756f', '051-2001317.Астраханское.Карбон.Скважины', 'Скважины',
        '6505cdd5-cf7f-413b-b969-e56dc2cb6305', '639601cc-2a47-4d1c-83b2-224a7bf8bb56', 'Прогнозное',
        '99f91cef-ea38-4aa2-8956-98762c406d9a', null, null, null, null, null);

INSERT INTO public.r_development_method (uuid, name) VALUES ('1ee86845-4db4-4758-9c70-02f36d88a21f', 'Потенциал');

INSERT INTO public.business_associate (uuid, long_name, short_name, ba_type)
VALUES ('99f91cef-ea38-4aa2-8956-98762c406d9a', 'ООО "Газпром добыча Астрахань"', 'ООО "Газпром добыча Астрахань"', null);
