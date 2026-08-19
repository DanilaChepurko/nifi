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
            unique
);
CREATE TABLE project_event (
    uuid UUID NOT NULL PRIMARY KEY,
    project_step_uuid UUID NOT NULL,
    name VARCHAR(255) NOT NULL
     constraint uc_project_event_name
                                          unique,
    number_event VARCHAR(255) NOT NULL,
    short_name VARCHAR(255) NOT NULL,
    type_event VARCHAR(255) NOT NULL,
    lvl_event VARCHAR(255) NOT NULL,
    prioritization_group VARCHAR(25) NOT NULL,

    CONSTRAINT fk_project_event_project_step FOREIGN KEY (project_step_uuid) REFERENCES project_step(uuid)
);

INSERT INTO public.project_step (uuid, name)
VALUES ('99f91cef-ea38-4aa2-8956-98762c406d9a', '051-2000005.Астраханское.Карбон.Скважины.НКТ');

INSERT INTO public.project_step (uuid, name)
VALUES ('cc2ca3c1-eeeb-4767-8bc6-28ed5c874330', '051-2000006.Астраханское.Карбон.ДКС.Привод ГПА');
