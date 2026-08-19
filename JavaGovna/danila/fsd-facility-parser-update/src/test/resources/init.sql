create table if not exists public.field
(
    uuid      uuid not null
        constraint field_pk
            primary key,
    name      varchar(255),
    ba_uuid   uuid,
    area_uuid uuid,
    type_uuid uuid,
    licenses  varchar(255),
    esg       boolean,
    int       integer
);

create table if not exists public.horizon
(
    uuid       uuid not null
        constraint horizon_pk
            primary key,
    name       varchar(255)
        constraint idx_unique_horizon_name
            unique,
    field_uuid uuid
);

create table if not exists public.horizon_area
(
    uuid         uuid not null
        primary key,
    name         varchar(255)
        constraint idx_unique_horizon_area_name
            unique,
    horizon_uuid uuid
);

create table if not exists public.facility
(
    uuid              uuid         not null
        primary key,
    name              varchar(255) not null
        constraint idx_unique_facility_name
            unique,
    field_uuid        uuid
        constraint facility_fk_field
            references public.field,
    horizon_uuid      uuid
        constraint facility_fk_horizon
            references public.horizon,
    horizon_area_uuid uuid
        constraint facility_fk_horizon_area
            references public.horizon_area,
    short_name        varchar(255),
    dkc varchar(255)
);

INSERT INTO public.field (uuid, name, ba_uuid, area_uuid, type_uuid, licenses, esg, int)
VALUES ('3986ea1c-ac29-4cf7-8a82-5e12a1063b80', 'Уренгойское', 'f271eb01-0b47-45dd-843b-d05ee20dbe8c',
        '1a0b5abb-473d-42d3-8434-43ab5862da30', '20c668bf-e0de-4e41-897e-513db70be197', 'ШОМ006641НР 19.08.2022', false,
        null);

INSERT INTO public.horizon (uuid, name, field_uuid)
VALUES ('cc2ca3c1-eeeb-4767-8bc6-28ed5c874330', 'Уренгойское.Ачимовский', 'effa730b-37cf-4bc7-8e19-118027d22d7b');

INSERT INTO public.horizon_area (uuid, name, horizon_uuid)
VALUES ('fc262b0c-fc34-443f-9df4-acb1b3bb66f1', 'Уренгойское.Ачимовский.Участок 1А',
        'dbe3da4f-bc45-499c-a01f-cf78fcbc0fec');

INSERT INTO public.horizon_area (uuid, name, horizon_uuid)
VALUES ('fc262b0c-fc34-443f-9df4-acb1b3bb66f2', 'Уренгойское.Ачимовский',
        'dbe3da4f-bc45-499c-a01f-cf78fcbc0fec');
