-- Usunięcie starego CHECK constraint (najpierw, żeby móc aktualizować statusy)
ALTER TABLE reservations DROP CONSTRAINT IF EXISTS reservations_status_check;

-- Aktualizacja istniejących rezerwacji - zmiana statusu ACTIVE na PAID (już opłacone)
-- Musimy to zrobić PRZED dodaniem nowego constraintu, bo nowy nie pozwala na 'ACTIVE'
UPDATE reservations 
SET status = 'PAID' 
WHERE status = 'ACTIVE';

-- Dodanie nowego CHECK constraint z nowymi statusami
-- Teraz możemy to zrobić, bo wszystkie wiersze mają już poprawne statusy
ALTER TABLE reservations 
    ADD CONSTRAINT reservations_status_check 
    CHECK (status IN ('PENDING_PAYMENT', 'PAID', 'CANCELLED', 'PAYMENT_FAILED'));

-- Dodanie pól płatności do tabeli reservations
ALTER TABLE reservations 
    ADD COLUMN payment_method VARCHAR(50),
    ADD COLUMN payment_date TIMESTAMP,
    ADD COLUMN payment_transaction_id VARCHAR(255);

-- Zmiana domyślnego statusu na PENDING_PAYMENT dla nowych rezerwacji
ALTER TABLE reservations 
    ALTER COLUMN status SET DEFAULT 'PENDING_PAYMENT';

-- Dodanie komentarzy do kolumn
COMMENT ON COLUMN reservations.payment_method IS 'Metoda płatności (CREDIT_CARD, DEBIT_CARD, BLIK, PAYPAL, CASH, MOCK)';
COMMENT ON COLUMN reservations.payment_date IS 'Data i czas dokonania płatności';
COMMENT ON COLUMN reservations.payment_transaction_id IS 'ID transakcji płatności (używane w symulacji)';

