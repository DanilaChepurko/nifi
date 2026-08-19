CREATE TABLE if not exists public.equipment (
    uuid UUID NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    term_bu NUMERIC(34,17) NOT NULL,
    term_nu NUMERIC(34,17) NOT NULL,
    deprication_premium  NUMERIC(10,5) NOT NULL, -- Исправлено: NUMERIC(10,5)
    term_expenses NUMERIC(34,17) NOT NULL,
    commissioning VARCHAR(255), -- Добавлено для поддержки теста
    CONSTRAINT uc_equipment_name UNIQUE (name)
);

