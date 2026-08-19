
create table if not exists public.r_analytic
(
    uuid uuid not null
        primary key,
    name varchar(255)
);


create table if not exists public.r_scenario
(
    uuid uuid not null
        primary key,
    name varchar(255)
);
CREATE TABLE IF NOT EXISTS public.macro_tax (
    uuid           UUID NOT NULL PRIMARY KEY,
    header_uuid UUID NOT NULL,
    analytic_uuid  UUID NOT NULL,
    value          NUMERIC(34, 17) NOT NULL,
    created_date   TIMESTAMP NOT NULL DEFAULT NOW()


);

create table if not exists public.header_econom
(
    uuid uuid not null,
        fsd_source text,
    year int,
    field_uuid uuid,
    ba_uuid uuid,
    horizon_uuid uuid,
    scenario uuid, -- Добавлена запятая
    name varchar(255),
    created_date TIMESTAMP
);


INSERT INTO public.r_analytic (uuid, name)
VALUES ('b1c3f8a9-1c2d-4e6b-9f13-97f8d9d4b1a2', 'Курс доллара ЦБ РФ руб./долл.');

INSERT INTO public.r_analytic (uuid, name)
VALUES ('d5a48e72-97c0-4bb9-a17e-0b2e5c9d6f91', 'Цена нефти Brent долл./барр');

INSERT INTO public.r_analytic (uuid, name)
VALUES ('a9e5b7c3-2d8f-47b2-9d5c-52a4e1b7c09f', 'Цена нефти Urals долл./барр');

INSERT INTO public.r_analytic (uuid, name)
VALUES ('f3d1a8b5-6e42-48b3-8e4b-91c7c9a5f72d', 'Цена газа на экспорт (Цдз) (письма ФАС) руб./тыс. м3');

INSERT INTO public.r_analytic (uuid, name)
VALUES ('c4b7e2a1-9f53-4e1a-8d6c-47f2a3e6b8d4', 'Ставка вывозной таможенной пошлины, долл./т (письма Минэконом развития) долл./т');


INSERT INTO public.r_scenario (uuid, name)
VALUES ('e8a9d3b1-7c25-4d9f-8b2a-31d6f7c9e1a4', 'ЦКР');
