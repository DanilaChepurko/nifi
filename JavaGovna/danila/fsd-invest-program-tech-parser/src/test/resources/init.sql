CREATE TABLE  if not exists public."field" (
  "uuid" uuid PRIMARY KEY NOT NULL,
  "name" varchar(255) UNIQUE,
  "ba_uuid" uuid,
  "region" varchar(255),
  "gts" varchar(100)
);

CREATE TABLE  if not exists public."horizon" (
  "uuid" uuid PRIMARY KEY NOT NULL,
  "name" varchar(255) UNIQUE,
  "field_uuid" uuid
);

CREATE TABLE  if not exists public."horizon_area" (
  "uuid" uuid PRIMARY KEY NOT NULL,
  "name" varchar(255) UNIQUE,
  "field_uuid" uuid,
  "horizon_uuid" uuid
);
CREATE TABLE  if not exists public."invest_program" (
  "uuid" uuid PRIMARY KEY NOT NULL,
  "field_uuid" UUID,
  "horizon_uuid" UUID,
  "horizon_area_uuid" UUID,
  "source_ip" varchar,
  "construction_namber" integer,
  "construction_cod" varchar,
  "equipment_name" varchar,
  "volume_equipment" numeric,
  "volume_unit" varchar,
  "start_date" timestamp
);


INSERT INTO public.field (uuid, name )
VALUES ('cc2ca3c1-eeeb-4767-8bc6-28ed5c874330', 'Уренгойское');
INSERT INTO public.horizon (uuid, name )
VALUES ('cc2ca3c1-eeeb-4764-8bc6-28ed5c874330', 'Уренгойское.Сеноман');
INSERT INTO public.horizon_area (uuid, name )
VALUES ('cc2ca3c1-eeeb-4757-8bc6-28ed5c874330', 'Уренгойское.Сеноман.Песцовая');
