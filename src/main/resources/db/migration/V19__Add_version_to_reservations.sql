-- V19__Add_version_to_reservations.sql
-- Dodanie kolumny version dla optymistycznego blokowania (optimistic locking)

ALTER TABLE reservations 
    ADD COLUMN version INTEGER NOT NULL DEFAULT 0;

-- Komentarz do kolumny
COMMENT ON COLUMN reservations.version IS 'Wersja rekordu dla optymistycznego blokowania (Hibernate @Version)';

