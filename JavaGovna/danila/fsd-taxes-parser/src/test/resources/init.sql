CREATE TABLE IF NOT EXISTS public.r_analytic (uuid UUID NOT NULL PRIMARY KEY, name VARCHAR(255));

CREATE TABLE IF NOT EXISTS public.r_scenario (uuid UUID NOT NULL PRIMARY KEY, name VARCHAR(255));

CREATE TABLE IF NOT EXISTS public.business_associate (
    uuid UUID NOT NULL PRIMARY KEY,
    long_name VARCHAR(255) CONSTRAINT idx_unique_business_associate_long_name UNIQUE,
    short_name VARCHAR(255),
    ba_type VARCHAR(255),
    stockholders VARCHAR(1000)
);

CREATE TABLE IF NOT EXISTS public.facility (
    uuid UUID NOT NULL PRIMARY KEY,
    name VARCHAR(255) CONSTRAINT idx_unique_facility_name UNIQUE,
    field_uuid UUID,
    horizon_uuid UUID,
    horizon_area_uuid UUID
);

CREATE TABLE IF NOT EXISTS public.header_econom (
    uuid UUID NOT NULL PRIMARY KEY,
    scenario UUID NOT NULL,
    fsd_source TEXT,
    ba_uuid UUID,
    year INTEGER,
    field_uuid UUID,
    created_date TIMESTAMP NOT NULL DEFAULT NOW ()
);

CREATE TABLE IF NOT EXISTS public.tax_indicator (
    uuid UUID NOT NULL PRIMARY KEY,
    header_uuid UUID NOT NULL,
    analytic_uuid UUID NOT NULL,
    year INTEGER,
    value NUMERIC(34, 17) NOT NULL
);

-- Единственный INSERT для r_analytic
INSERT INTO
    public.r_analytic (uuid, name)
VALUES
    ('b1c3f8a9-1c2d-4e6b-9f13-97f8d9d4b1a2', 'НДС %'),
     ('a1c3f8a9-1c2d-4e6b-9f13-97f8d9d4b1a2','Базовая ставка НДПИ газ'),
     ('c1c3f8a9-1c2d-4e6b-9f13-97f8d9d4b1a2','Базовая ставка НДПИ конденсат'),
     ('a2c3f8a9-1c2d-4e6b-9f13-97f8d9d4b1a2','Базовая ставка НДПИ нефть'),
     ('a3c3f8a9-1c2d-4e6b-9f13-97f8d9d4b1a2','Рдз'),
     ('a4c3f8a9-1c2d-4e6b-9f13-97f8d9d4b1a2','Кк'),
     ('a5c3f8a9-1c2d-4e6b-9f13-97f8d9d4b1a2','Ккг'),
     ('a7c3f8a9-1c2d-4e6b-9f13-97f8d9d4b1a2','Стп'),
     ('a6c3f8a9-1c2d-4e6b-9f13-97f8d9d4b1a2','Кгп'),
     ('a8c3f8a9-1c2d-4e6b-9f13-97f8d9d4b1a2','Тр'),
     ('a9c3f8a9-1c2d-4e6b-9f13-97f8d9d4b1a2','Рг'),
     ('d1c3f8a9-1c2d-4e6b-9f13-97f8d9d4b1a2','Ог'),
     ('c2c3f8a9-1c2d-4e6b-9f13-97f8d9d4b1a2','К кор'),
     ('b3c3f8a9-1c2d-4e6b-9f13-97f8d9d4b1a2','ФМ'),
     ('b4c3f8a9-1c2d-4e6b-9f13-97f8d9d4b1a2','Кндпи'),

    (
        'd8040181-156e-4956-a944-4841844004d1',
        'Налог на имущество %'
    ),
    (
        '324c0b45-8fb6-4567-be18-66eef7288124',
        'Налог на прибыль %'
    ),
    (
        'd5a48e72-97c0-4bb9-a17e-0b2e5c9d6f91',
        'Взносы на социальное страхование Предельная величина базы для исчисления страховых взносов тыс.руб./чел.'
    ),
    (
        'a9e5b7c3-2d8f-47b2-9d5c-52a4e1b7c09f',
        'Взносы на социальное страхование Ставка страховых взносов в пределах установленной единой предельной величины базы для исчисления страховых взносов %'
    ),
    (
        'f3d1a8b5-6e42-48b3-8e4b-91c7c9a5f72d',
        'Взносы на социальное страхование Ставка страховых взносов свыше установленной единой предельной величины базы для исчисления страховых взносов %'
    ),
    (
        'c4b7e2a1-9f53-4e1a-8d6c-47f2a3e6b8d4',
        'Страхование от несчастных случаев на производстве газ, газоконденсат %'
    ),
    (
        '2c0e339b-87b9-463c-b838-3451e23a5212',
        'Страхование от несчастных случаев на производстве нефть, ПНГ %'
    ),
    (
        '7cac7861-0a95-47de-b039-fb88567176f8',
        'Налог на землю тыс.руб./га'
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

-- INSERT для r_scenario
INSERT INTO
    public.r_scenario (uuid, name)
VALUES
    (
        'e8a9d3b1-7c25-4d9f-8b2a-31d6f7c9e1a4',
        'ПАО "Газпром"'
    );