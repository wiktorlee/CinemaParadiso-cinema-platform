-- Naprawa constraint na payment_method w tabeli audytowej reservations_aud
-- Hibernate Envers automatycznie utworzył constraint, który nie zawiera wartości CASH
-- Musimy zaktualizować constraint, aby pozwalał na wszystkie metody płatności

-- KROK 1: Usunięcie starego constraint w tabeli audytowej (jeśli istnieje)
ALTER TABLE reservations_aud DROP CONSTRAINT IF EXISTS reservations_aud_payment_method_check;

-- KROK 2: Dodanie nowego constraint z wszystkimi metodami płatności
-- Wszystkie wartości z enum PaymentMethod: CREDIT_CARD, DEBIT_CARD, BLIK, PAYPAL, CASH, MOCK
ALTER TABLE reservations_aud 
    ADD CONSTRAINT reservations_aud_payment_method_check 
    CHECK (payment_method IS NULL OR payment_method IN ('CREDIT_CARD', 'DEBIT_CARD', 'BLIK', 'PAYPAL', 'CASH', 'MOCK'));

-- KROK 3: Dodanie constraint do tabeli reservations (dla spójności, jeśli jeszcze nie istnieje)
-- Sprawdzamy czy constraint już istnieje - jeśli tak, to go pomijamy
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM pg_constraint 
        WHERE conname = 'reservations_payment_method_check'
    ) THEN
        ALTER TABLE reservations 
            ADD CONSTRAINT reservations_payment_method_check 
            CHECK (payment_method IS NULL OR payment_method IN ('CREDIT_CARD', 'DEBIT_CARD', 'BLIK', 'PAYPAL', 'CASH', 'MOCK'));
    END IF;
END $$;

