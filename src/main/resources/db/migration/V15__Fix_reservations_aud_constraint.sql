-- Naprawa constraint w tabeli audytowej reservations_aud (Hibernate Envers)
-- Tabela audytowa również potrzebuje zaktualizowanego constraintu, który pozwala na nowe statusy
-- Problem: V14 zaktualizowała constraint w reservations, ale nie w reservations_aud

-- KROK 1: Usunięcie starego constraint (najpierw, żeby móc aktualizować statusy)
ALTER TABLE reservations_aud DROP CONSTRAINT IF EXISTS reservations_aud_status_check;

-- KROK 2: Zaktualizuj istniejące wiersze w tabeli audytowej
-- Zmień 'ACTIVE' na 'PAID' (teraz możemy, bo nie ma constraintu)
UPDATE reservations_aud 
SET status = 'PAID' 
WHERE status = 'ACTIVE';

-- KROK 3: Dodanie nowego constraint z nowymi statusami
-- Teraz możemy to zrobić, bo wszystkie wiersze mają już poprawne statusy
ALTER TABLE reservations_aud 
    ADD CONSTRAINT reservations_aud_status_check 
    CHECK (status IN ('PENDING_PAYMENT', 'PAID', 'CANCELLED', 'PAYMENT_FAILED'));

