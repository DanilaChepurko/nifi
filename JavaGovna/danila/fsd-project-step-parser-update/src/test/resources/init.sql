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

create table if not exists public.business_associate
(
    uuid       uuid not null
        primary key,
    long_name  varchar(255),
    short_name varchar(255),
    ba_type    varchar(255)
);

create table if not exists public.project_step
(
    uuid           uuid         not null
        constraint project_step_pk
            primary key,
    name           varchar(255) not null
        constraint uc_project_step_name
            unique,
    type           varchar(255) not null,
    horizon_uuid   uuid         not null
        constraint project_step_fk_horizon
            references public.horizon,
    code           varchar(255) not null,
    ba_uuid        uuid
        constraint project_step_fk_ba
            references public.business_associate,
    equipment_name varchar(255),
    project_name   varchar(255)
);

INSERT INTO public.business_associate (uuid, long_name, short_name, ba_type)
VALUES ('99f91cef-ea38-4aa2-8956-98762c406d9a', 'ООО "Газпром добыча Надым"', 'ООО "Газпром добыча Надым"', null);

INSERT INTO public.horizon (uuid, name, field_uuid)
VALUES ('cc2ca3c1-eeeb-4767-8bc6-28ed5c874330', 'Бованенковское.Сеноман-апт', 'effa730b-37cf-4bc7-8e19-118027d22d7b');
