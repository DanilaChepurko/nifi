
create table if not exists public.r_analytic
(
    uuid uuid not null
        primary key,
    name varchar(255)
);

create table if not exists public.horizon
(
    uuid       uuid not null
            primary key,
    name       varchar(255)
      ,
    field_uuid uuid
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

    CREATE TABLE if not exists public.horizon_econ_param (
       header_uuid UUID NOT NULL,
        analytic_uuid UUID NOT NULL,
        fluid varchar(50),
        created_date TIMESTAMP NOT NULL,
        value NUMERIC(34,17) NOT NULL--,
     --  CONSTRAINT fk_horizon_econ_param_analytics FOREIGN KEY (analytics_uuid) REFERENCES r_analytic(uuid) -- Исправлено: r_analytic вместо r_analytics
    );
--

CREATE TABLE if not exists public.horizon_fond (
    header_uuid UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    analytic_uuid UUID NOT NULL,
    created_date TIMESTAMP NOT NULL,
    value NUMERIC(34,17) NOT NULL--,
 --   CONSTRAINT fk_horizon_fond_analytics FOREIGN KEY (analytics_uuid) REFERENCES r_analytic(uuid) -- Исправлено: r_analytic вместо r_analytics
);

create table if not exists public.r_analytic
(
    uuid uuid not null
        primary key,
    name varchar(255)
);

create table if not exists public.field
(
    uuid       uuid ,
    name       varchar(255)
);

create table if not exists public.horizon
(
    uuid       uuid not null
        constraint horizon_pk
            primary key,
    name       varchar(255),
    field_uuid uuid
);

create table if not exists public.business_associate
(
    uuid       uuid ,
    long_name       varchar(255)
);


INSERT INTO public.horizon (uuid, name, field_uuid)
VALUES ('cc2ca3c1-eeeb-4767-8bc6-28ed5c874330', 'Уренгойское.Сеноман', 'effa730b-37cf-4bc7-8e19-118027d22d7b');

INSERT INTO public.field (uuid, name)
VALUES ('effa730b-37cf-4bc7-8e19-118027d22d7b', 'Уренгойское');


INSERT INTO public.business_associate (uuid, long_name)
VALUES ('affa730b-37cf-4bc7-8e19-118027d22d7b', 'ООО "Газпром добыча Уренгой"');

INSERT INTO public.r_analytic (uuid, name) VALUES
('5f5a4d94-bbbe-40d9-aa9f-c1d54210e36e', 'Объем валовой добычи млн м3, тыс. т');
INSERT INTO public.r_analytic (uuid, name) VALUES
('3ef1f021-c811-4733-bebc-cd80c6e6ae7f', 'Объем товарной добычи млн м3, тыс. т');
INSERT INTO public.r_analytic (uuid, name) VALUES
('4391eed6-395e-440a-9d9a-ee982210282f', 'Количество добычных скважин в эксплуатации шт');
INSERT INTO public.r_analytic (uuid, name) VALUES
('905ca774-b9ce-4e6c-a329-11c07ed24f13', 'Среднесписочная численность производственного персонала чел.');
INSERT INTO public.r_analytic (uuid, name) VALUES
('9cb42206-94da-4302-baa7-89dbf3d8bbfa', 'Цена товарной продукции руб. тыс. м3 руб. т');
INSERT INTO public.r_analytic (uuid, name) VALUES
('f86f4198-4111-40dc-a087-e4230963802f', 'Полная себестоимость на ед. отгруженной продукции руб. тыс. м3 руб. т');
INSERT INTO public.r_analytic (uuid, name) VALUES
('d4fc8c6a-fb0c-4e17-9b51-452f943f0976', 'Сырье и материалы млн руб.');
INSERT INTO public.r_analytic (uuid, name) VALUES
('743c72d5-9ea7-4c87-8d88-ad228ca0e378', 'Сырье и основные материалы млн руб.');
INSERT INTO public.r_analytic (uuid, name) VALUES
('5e460548-3e4e-4306-abcb-2a396ddb869e', 'Вспомогательные материалы млн руб.');
INSERT INTO public.r_analytic (uuid, name) VALUES
('28feed8e-7e00-4b24-99be-95c43c6d5296', 'Материалы и товары для перепродажи млн руб.');
INSERT INTO public.r_analytic (uuid, name) VALUES
('7ef22458-fd21-485b-bf4a-090f3d8bc939', 'Газ на собственные нужды млн руб.');
INSERT INTO public.r_analytic (uuid, name) VALUES
('219f0195-d58d-4cdb-9d16-c83f9e0a2a00', 'Газ на собственные технологические нужды млн руб.');
INSERT INTO public.r_analytic (uuid, name) VALUES
('3624d7d3-a495-4263-8b27-9a1978048f92', 'Затраты всего млн руб.');
INSERT INTO public.r_analytic (uuid, name) VALUES
('2303f399-6f8d-4e75-812b-3eaab1ed69f9', 'Затраты на ППД - газ руб./тыс. м3 руб./т');
--INSERT INTO public.r_analytic (uuid, name) VALUES
--('2303f399-6f8d-4e75-812b-3eabb1ed69f9', '- газ руб./тыс. м3 руб./т');
INSERT INTO public.r_analytic (uuid, name) VALUES
('2303f399-6f8d-4e75-812b-3eabb2ed69f9', 'Первоначальная стоимость, млн руб.');
INSERT INTO public.r_analytic (uuid, name) VALUES
('2303f399-6f8d-4e75-812b-3eabb2ed60f9', 'Остаточная стоимость, млн руб.');
INSERT INTO public.r_analytic (uuid, name) VALUES
('2303f399-6f8d-4e75-812b-3ebab2ed60f9', 'Затраты на кап.ремонт объектов обустройства в % от первоначальной стоимости ОФ');
INSERT INTO public.r_analytic (uuid, name) VALUES
('2303f399-6f8d-4e45-812b-3ebab2ed60f9', 'Затраты на ликвидацию, % от первоначальной стоимости');
INSERT INTO public.r_analytic (uuid, name) VALUES
('2303f399-6f8d-4e45-813b-3ebab2ed60f9', 'удельная численность работников чел./скв');
INSERT INTO public.r_analytic (uuid, name) VALUES
('2303f399-6f8d-4e45-813b-3ebab23d60f9', 'заработная плата на 1 работающего (по видам флюида) тыс. руб./чел.');

