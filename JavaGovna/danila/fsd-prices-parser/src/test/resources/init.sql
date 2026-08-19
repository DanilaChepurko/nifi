CREATE TABLE IF NOT EXISTS public.r_analytic (uuid UUID NOT NULL PRIMARY KEY, name VARCHAR(255));

CREATE TABLE IF NOT EXISTS public.r_scenario (uuid UUID NOT NULL PRIMARY KEY, name VARCHAR(255));

CREATE TABLE IF NOT EXISTS public.business_associate (
    uuid UUID NOT NULL PRIMARY KEY,
    long_name VARCHAR(255) CONSTRAINT idx_unique_business_associate_long_name UNIQUE,
    short_name VARCHAR(255),
    ba_type VARCHAR(255),
    stockholders VARCHAR(1000)
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

CREATE TABLE IF NOT EXISTS public.header_econom (
    uuid UUID NOT NULL PRIMARY KEY,
    scenario UUID NOT NULL,
    ba_uuid UUID,
    fsd_source TEXT,
    year INTEGER,
    field_uuid UUID,
    created_date TIMESTAMP NOT NULL DEFAULT NOW ()
);

CREATE TABLE IF NOT EXISTS public.gdo_prices (
    uuid UUID not null PRIMARY KEY,
    header_uuid UUID NOT NULL,
    analytic_uuid UUID NOT NULL,
    value NUMERIC(34, 17) NOT NULL
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
        'Медвежье',
        'f271eb01-0b47-45dd-843b-d05ee20dbe8c',
        '1a0b5abb-473d-42d3-8434-43ab5862da30',
        '20c668bf-e0de-4e41-897e-513db70be197',
        'ШОМ006641НР 19.08.2022',
        false,
        null
    );

INSERT INTO
    public.r_scenario (uuid, name)
VALUES
    (
        '5e78bb8c-64ce-401b-bbd3-cbc18cab3226',
        'ПАО "Газпром"'
    );

INSERT INTO
    public.business_associate (uuid, long_name, short_name, ba_type)
VALUES
    (
        '99f91cef-ea38-4aa2-8956-98762c406d9a',
        'ООО "Газпром добыча Надым"',
        'ООО "Газпром добыча Надым"',
        null
    );

INSERT INTO
    public.r_analytic (uuid, name)
VALUES
    ('b1c3f8a9-1c2d-4e6b-9f13-97f8d9d4b1a2', 'НДС %'),
    (
        'd8040181-156e-4956-a944-4841844004d1',
        'Цена реализации на газ руб./1000 м³'
    ),
    (
        '324c0b45-8fb6-4567-be18-66eef7288124',
        'Цв (цена на газ на внутр.рынке в реальном исчисл.)(для НДПИ) руб./1000 м³'
    ),
    (
        'd5a48e72-97c0-4bb9-a17e-0b2e5c9d6f91',
        'Цена на нестабильный конденсат руб./т'
    ),
    (
        'a9e5b7c3-2d8f-47b2-9d5c-52a4e1b7c09f',
        'Цена на стабильный конденсат руб./т'
    ),
    (
        'f3d1a8b5-6e42-48b3-8e4b-91c7c9a5f72d',
        'Цена на нефть руб./т'
    ),
    (
        'c4b7e2a1-9f53-4e1a-8d6c-47f2a3e6b8d4',
        'Цена на ШФЛУ руб./т'
    ),
    (
        '2c0e339b-87b9-463c-b838-3451e23a5212',
        'Цена на СПБТ руб./т'
    ),
    (
        '7cac7861-0a95-47de-b039-fb88567176f8',
        'Норма дисконта %'
    ),
    (
        '6a9de3eb-d2cf-4635-9391-012af11c9a97',
        'Экологические платежи млн руб.'
    ),
    (
        'a2522152-0ded-4d44-9b61-40d193b0dfc3',
        'Прочие налоги и платежи'
    ),
    (
        '188a954d-7293-4f30-a386-48b12ff5377c',
        'Прочие налоги и платежи %'
    ),
    (
        'ade5592c-136a-425a-b19e-9924d41998df',
        'Автомобильный бензин АИ-92 ЦАБвр'
    ),
    (
        '894a1e94-fb5c-410e-a256-b0d589159341',
        'Автомобильный бензин АИ-92 ЦАБвр_С'
    ),
    (
        '7b85e273-431a-437b-86b9-641384b3e69e',
        'Автомобильный бензин АИ-92 ЦАБвр_2021'
    ),
    (
        '4ec8dc41-703c-4ccd-9e18-7e11bf4c12d4',
        'Дизельное топливо ЦДТвр'
    ),
    (
        'e0a65ece-8aab-4e8e-bd31-478d524bc925',
        'Дизельное топливо ЦДТвр_С'
    ),
    (
        'd2c4e7f1-8b3a-3c6d-9e5f-1a2b3c4d5e6f',
        'Дизельное топливо ЦДТвр_2021'
    );