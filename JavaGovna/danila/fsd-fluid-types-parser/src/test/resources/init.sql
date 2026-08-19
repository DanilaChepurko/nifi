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

CREATE TABLE IF NOT EXISTS public.fluid_types (
    uuid UUID NOT NULL PRIMARY KEY,
    horizon_uuid UUID NOT NULL,
    ba_uuid UUID NOT NULL,
    field_uuid UUID NOT NULL,
    type VARCHAR(50) NOT NULL
);

create table if not exists public.horizon (
    uuid uuid not null constraint horizon_pk primary key,
    name varchar(255) constraint idx_unique_horizon_name unique,
    field_uuid uuid
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
    public.business_associate (uuid, long_name, short_name, ba_type)
VALUES
    (
        '99f91cef-ea38-4aa2-8956-98762c406d9a',
        'ООО "Газпром добыча Надым"',
        'ООО "Газпром добыча Надым"',
        null
    );

INSERT INTO
    public.horizon (uuid, name, field_uuid)
VALUES
    (
        'cc2ca3c1-eeeb-4767-8bc6-28ed5c874330',
        'Медвежье.Ачимовский',
        '3986ea1c-ac29-4cf7-8a82-5e12a1063b80'
    ),
    (
        'c040e431-70db-401c-9e27-f36eb40fee4f',
        'Медвежье.Неоком',
        '3986ea1c-ac29-4cf7-8a82-5e12a1063b80'
    ),
    (
        '3ce66fd8-60f5-44c6-84ac-46e7642d6f37',
        'Медвежье.Сеноман',
        '3986ea1c-ac29-4cf7-8a82-5e12a1063b80'
    );