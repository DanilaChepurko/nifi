create table if not exists public.field (
    uuid uuid not null constraint field_pk primary key,
    name varchar(255) not null constraint uc_field_name unique,
    ba_uuid uuid,
    area_uuid uuid,
    type_uuid uuid not null,
    licenses varchar(255),
    esg boolean
);

create table if not exists public.r_period_type (
    uuid uuid not null constraint r_period_type_pk primary key,
    name varchar(255)
);

CREATE TABLE IF NOT EXISTS public.business_associate (
    uuid UUID NOT NULL PRIMARY KEY,
    long_name VARCHAR(255) CONSTRAINT idx_unique_business_associate_long_name UNIQUE,
    short_name VARCHAR(255),
    ba_type VARCHAR(255),
    stockholders VARCHAR(1000)
);

create table if not exists public.r_analytic (uuid uuid not null primary key, name varchar(255));

create table public.horizon (
    uuid uuid not null constraint horizon_pk primary key,
    name varchar(255) constraint idx_unique_horizon_name unique,
    field_uuid uuid
);

create table if not exists public.r_scenario (uuid uuid not null primary key, name varchar(255));

create table if not exists public.r_version (uuid uuid not null primary key, name varchar(255));

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

create table if not exists public.horizon_area (
    uuid uuid not null primary key,
    name varchar(255) constraint idx_unique_horizon_area_name unique,
    horizon_uuid uuid
);

create table if not exists public.header_development (
    uuid uuid not null constraint header_development_pk primary key,
    scenario uuid not null,
    year integer,
    version_uuid uuid,
    name varchar(255),
    created_date timestamp not null,
    updated_date timestamp not null,
    horizon_uuid uuid,
    model_date timestamp,
    field_uuid uuid,
    version_plan_uuid uuid,
    dop_scenario uuid
);

alter table header_development
add column fluid_type text,
add column pool_uuid uuid;

create table if not exists public.header_development_meta_inf (
    header_uuid uuid not null primary key,
    author text not null,
    fill_in_date date not null,
    sync_scenario uuid, -- FK references (r_scenario) -- ??версия КПР в рамках которой выполнен расчет эффектов
    sync_version uuid, -- FK references (r_version)
    sync_compared_scenario uuid, -- FK references (r_scenario)
    sync_compared_version uuid -- FK references (r_version)
);

create table if not exists public.pden_vol_summary_development (
    header_uuid uuid not null,
    project_step_uuid uuid,
    analytics_uuid uuid not null,
    period_type_uuid uuid not null,
    start_date timestamp not null,
    value numeric(34, 17) not null,
    horizon_area uuid,
    program varchar(255),
    equipment_qty numeric(10, 3),
    priority integer,
    investment numeric(10, 3),
    description varchar(4000)
);

alter table pden_vol_summary_development
add column equipment_category text,
add column project_event_uuid uuid,
add column project_step_group integer,
add column year integer;

--FK refereneces project_event(uuid)
CREATE TABLE project_event (
    uuid UUID NOT NULL PRIMARY KEY,
    project_step_uuid UUID, -- NOT NULL,
    name VARCHAR(255), -- NOT NULL constraint uc_project_event_name unique,
    number_event VARCHAR(255), -- NOT NULL,
    short_name VARCHAR(255), -- NOT NULL,
    type_event VARCHAR(255), -- NOT NULL,
    lvl_event VARCHAR(255), -- NOT NULL,
    prioritization_group VARCHAR(25) -- NOT NULL,
);

create table if not exists public.pool (
    uuid uuid not null constraint pool_pk primary key,
    name varchar(255),
    ba_uuid uuid,
    field_uuid uuid,
    horizon_uuid uuid,
    esg boolean,
    license boolean,
    drod integer,
    licenses varchar(500)
);

INSERT INTO
    public.pool (
        uuid,
        name,
        ba_uuid,
        field_uuid,
        horizon_uuid,
        esg,
        license,
        drod,
        licenses
    )
VALUES
    (
        'd0367fcb-3a33-406a-b35d-3efb936bccd1',
        'Заполярное.Сеноман..ПК1',
        null,
        '3b30f79e-2a27-4af1-a5a0-eca388f220a0',
        '3b30f79e-2a27-4af1-a5a0-eca388f220a2',
        true,
        null,
        null,
        null
    );

INSERT INTO
    public.business_associate (uuid, long_name, short_name, ba_type)
VALUES
    (
        '99f91cef-ea38-4aa2-8956-98762c406d9a',
        'ООО "Газпром добыча Астрахань"',
        'ООО "Газпром добыча Астрахань"',
        null
    );

INSERT INTO
    project_event (uuid, number_event, name)
VALUES
    (
        '932677aa-a18b-4876-bd07-86f67bf8debc',
        '16.1.2.15',
        '051-2001317.(16.1.2.15).Астраханское.Карбон.Р.С.Скважины.Прискважинное оборудование'
    );

INSERT INTO
    public.r_analytic (uuid, name)
VALUES
    (
        '759238ae-d6d9-49f4-805f-f674aefdf5fa',
        'Предотвращение снижения (прирост) максимальной суточной добычи газа, млн. м3/сут'
    );

INSERT INTO
    public.r_analytic (uuid, name)
VALUES
    (
        '759238ae-d6d9-49f4-805f-f674aefdf5fb',
        'Предотвращение снижения (прирост) максимальной суточной добычи ПНГ, млн. м3/сут'
    );

INSERT INTO
    public.r_analytic (uuid, name)
VALUES
    (
        '759238ae-d6d9-49f4-805f-f674aefdf5fc',
        'Предотвращение снижения (прирост) максимальной суточной добычи нестабильного конденсата, тыс. т/сут'
    );

INSERT INTO
    public.r_analytic (uuid, name)
VALUES
    (
        '759238ae-d6d9-49f4-805f-f674aefdf5fd',
        'Предотвращение снижения (прирост) максимальной суточной добычи стабильного конденсата, тыс. т/сут'
    );

INSERT INTO
    public.r_analytic (uuid, name)
VALUES
    (
        '759238ae-d6d9-49f4-805f-f674aefdf5fe',
        'Предотвращение снижения (прирост) максимальной суточной добычи нефти, тыс. т/сут'
    );

INSERT INTO
    public.r_analytic (uuid, name)
VALUES
    (
        '759238ae-d6d9-49f4-805f-f674aefdf5ff',
        'Технологический эффект по добыче газа от выполненной реконструкции, млн куб. м3'
    );

INSERT INTO
    public.r_analytic (uuid, name)
VALUES
    (
        '759238ae-d6d9-49f4-805f-f674aefdf600',
        'добыча газа, млн м3'
    );

INSERT INTO
    public.r_analytic (uuid, name)
VALUES
    (
        '5e78bb8c-64ce-401b-bbd3-cbc18cab3226',
        'Приросты максимальной добычи'
    ),
    (
        '0cbd7b04-00a8-45dd-91c8-3b7705e8f67f',
        'Предотвращение снижения (прирост) валовой годовой добычи Газ, млн. м3'
    ),
    (
        '5074249e-b83d-4941-b3f7-7147602bceca',
        'Предотвращение снижения (прирост) валовой годовой добычи ПНГ, млн. м3'
    ),
    (
        '77f8e16f-e466-4d3c-a8b2-a4e378cb62fe',
        'Предотвращение снижения (прирост) валовой годовой добычи Нестабильный конденсат, тыс. т'
    ),
    (
        '4cb306e2-98d4-413c-a3f9-7dfd225f4e0e',
        'Предотвращение снижения (прирост) валовой годовой добычи Стабильный конденсат, тыс. т'
    ),
    (
        'b662babd-844d-4556-a77e-66ba45051ffc',
        'Предотвращение снижения (прирост) валовой годовой добычи Нефть, тыс. т'
    ),
    (
        '34569f83-7988-495c-9c64-8eec1fa96870',
        'Предотвращение снижения (прирост) товарной годовой добычи Газ, млн. м3'
    ),
    (
        '0416896d-5591-42d5-af8e-e98f161790ec',
        'Предотвращение снижения (прирост) товарной годовой добычи ПНГ, млн. м3'
    ),
    (
        'a73a4b0b-5527-4f7d-9419-7d91d1557aba',
        'Предотвращение снижения (прирост) товарной годовой добычи Нестабильный конденсат, тыс. т'
    ),
    (
        'b87c2c47-70f0-4f6e-bf68-84d240d9182d',
        'Предотвращение снижения (прирост) товарной годовой добычи Стабильный конденсат, тыс. т'
    ),
    (
        'd3a21d55-657e-4251-a102-eeac21191a7c',
        'Предотвращение снижения (прирост) товарной годовой добычи Нефть, тыс. т'
    );

INSERT INTO
    public.r_scenario (uuid, name)
VALUES
    ('5e78bb8c-64ce-401b-bbd3-cbc18cab3227', 'Эффекты'),
    ('79bb8a66-8ddb-4ce4-b267-3edf609fc513', 'КПР'),
    (
        '73bb8a66-8ddb-4ce4-b267-3edf609fc513',
        'Приросты максимальной добычи'
    ),
    (
        '79bb8a66-8ddb-4ce4-b267-3edf609fc502',
        'Пиковый баланс газа'
    );

INSERT INTO
    public.r_scenario (uuid, name)
VALUES
    (
        '5e78bb8c-64ce-401b-bbd3-cbc18cab3228',
        'Максимальная суточная добыча'
    ),
    (
        '785a71d0-0d74-402b-9691-d8924038f4ae',
        'Уровни добычи из АН'
    );

INSERT INTO
    public.r_scenario (uuid, name)
VALUES
    (
        '5e78bb8c-64ce-401b-bbd3-cbc18cab3229',
        'План строительства'
    );

INSERT INTO
    public.r_period_type (uuid, name)
VALUES
    ('2123fe0a-33bd-4324-b312-696df40b0a7c', 'year');

INSERT INTO
    public.r_period_type (uuid, name)
VALUES
    ('d6bc16a4-391c-44e5-bf3d-571ebc82a22d', 'month');

INSERT INTO
    public.r_period_type (uuid, name)
VALUES
    ('157badbb-c88f-45bb-8eb7-1275b47970d5', 'quarter');

INSERT INTO
    public.r_version (uuid, name)
VALUES
    (
        '8354ea0c-3fc9-4cf2-8e7d-56db9f8b9ff9',
        'Утвержденная'
    ),
    ('79bb8a66-8ddb-4ce4-b267-3edf609fc516', '1.1'),
    ('79bb8a66-8ddb-4ce4-b267-3edf609fc512', '2.1');

INSERT INTO
    public.project_step (uuid, name, type, horizon_uuid, ba_uuid, code)
VALUES
    (
        '1cca9c73-a973-42c0-98f9-51f7087434e8',
        'Техническое перевооружение фонда скважин АГКМ',
        'Скважины',
        '6505cdd5-cf7f-413b-b969-e56dc2cb6305',
        '639601cc-2a47-4d1c-83b2-224a7bf8bb56',
        '051-2004395'
    );

INSERT INTO
    public.project_step (uuid, name, type, horizon_uuid, ba_uuid, code)
VALUES
    (
        '91c60f1b-61be-4153-8861-686a9160756f',
        'Реконструкция обвязки устья скважины газовой эксплуатационной № 263 УППГ-2',
        'Скважины',
        '6505cdd5-cf7f-413b-b969-e56dc2cb6305',
        '639601cc-2a47-4d1c-83b2-224a7bf8bb56',
        '051-2001317'
    );

INSERT INTO
    public.project_step (uuid, name, type, horizon_uuid, ba_uuid, code)
VALUES
    (
        '91c60f1b-61be-4153-8861-686a91607570',
        '051-2004023.Ямбургское.Сеноман.ГСС.Газопроводы шлейфов',
        'Скважины',
        '6505cdd5-cf7f-413b-b969-e56dc2cb6305',
        '639601cc-2a47-4d1c-83b2-224a7bf8bb56',
        'Прогнозное'
    );

INSERT INTO
    public.horizon (uuid, name, field_uuid)
VALUES
    (
        'cc2ca3c1-eeeb-4767-8bc6-28ed5c874330',
        'Бованенковское.Сеноман-апт',
        'effa730b-37cf-4bc7-8e19-118027d22d7b'
    ),
    (
        'cc2ca3c1-eeeb-4767-8bc6-28ed5c874335',
        'Астраханское.Карбон',
        'effa730b-37cf-4bc7-8e19-118027d22d6b'
    );

INSERT INTO
    public.horizon_area (uuid, name, horizon_uuid)
VALUES
    (
        'fc262b0c-fc34-443f-9df4-acb1b3bb66f1',
        'Уренгойское.Ачимовский.Участок 1А',
        'dbe3da4f-bc45-499c-a01f-cf78fcbc0fec'
    );

INSERT INTO
    public.field (
        uuid,
        name,
        ba_uuid,
        area_uuid,
        type_uuid,
        licenses,
        esg
    )
VALUES
    (
        '3986ea1c-ac29-4cf7-8a82-5e12a1063b80',
        'Астраханское',
        'f271eb01-0b47-45dd-843b-d05ee20dbe8c',
        '1a0b5abb-473d-42d3-8434-43ab5862da30',
        '20c668bf-e0de-4e41-897e-513db70be197',
        'ШОМ006641НР 19.08.2022',
        false
    );

INSERT INTO
    public.field (
        uuid,
        name,
        ba_uuid,
        area_uuid,
        type_uuid,
        licenses,
        esg
    )
VALUES
    (
        '3986ea1c-ac29-4cf7-8a82-5e12a1063b81',
        'Бованенковское',
        'f271eb01-0b47-45dd-843b-d05ee20dbe8c',
        '1a0b5abb-473d-42d3-8434-43ab5862da30',
        '20c668bf-e0de-4e41-897e-513db70be197',
        'ШОМ006641НР 19.08.2022',
        false
    );