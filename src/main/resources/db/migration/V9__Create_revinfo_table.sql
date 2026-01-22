-- Tabela informacji o rewizjach (dla Hibernate Envers Audit Trail)
-- Przechowuje informacje o każdej zmianie w systemie: kto, kiedy, z jakiego IP
CREATE TABLE revinfo (
    rev BIGSERIAL PRIMARY KEY,
    revtstmp BIGINT NOT NULL,
    user_id BIGINT,
    user_email VARCHAR(255),
    ip_address VARCHAR(45)
);

-- Indeksy dla szybszego wyszukiwania
CREATE INDEX idx_revinfo_revtstmp ON revinfo(revtstmp);
CREATE INDEX idx_revinfo_user_id ON revinfo(user_id);
CREATE INDEX idx_revinfo_user_email ON revinfo(user_email);

-- Komentarz do tabeli
COMMENT ON TABLE revinfo IS 'Tabela przechowująca informacje o rewizjach (zmianach) w systemie. Używana przez Hibernate Envers do audit trail.';

