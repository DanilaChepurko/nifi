



create table if not exists public.field
(
    uuid      uuid not null
        constraint field_pk
            primary key,
    name      varchar(255)
        constraint uc_field_name
            unique,
    ba_uuid   uuid,
    area_uuid uuid,
    type_uuid uuid,
    licenses  varchar(255),
    esg       boolean,
    year_open       integer,
    product_sale_uuid uuid
);

create table if not exists public.r_field_type
(
    uuid uuid,
    name varchar(255)
);

create table if not exists public.area
(
    uuid uuid not null
        constraint area_pk
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

create table if not exists public.product_sale
(
    uuid uuid,
    name varchar(255)
);

INSERT INTO public.r_field_type (uuid, name) VALUES ('b74c10e5-fe11-45c9-a94a-5bb890a1b639', 'газоконденсатное');

INSERT INTO public.business_associate (uuid, long_name, short_name, ba_type, stockholders) VALUES ('f5bcc422-f5a5-43c2-8562-ea3472ef5e65', 'ООО "Газпром добыча Астрахань"', 'ГДТам', 'Группа Газпром', 'ООО "Газпром Недра" - 50%, АО "РУСГАЗДОБЫЧА" - 50 %');

INSERT INTO public.area (uuid, name) VALUES ('501f5d85-5533-465d-ad10-ce7d21fd7cfe', 'Астраханская обл.');
INSERT INTO public.product_sale (uuid, name) VALUES ('5d1f5d43-5533-465d-ad10-ce7d21fd7cfe', 'местное обеспечение');


