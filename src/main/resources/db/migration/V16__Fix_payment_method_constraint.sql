ALTER TABLE reservations_aud DROP CONSTRAINT IF EXISTS reservations_aud_payment_method_check;

ALTER TABLE reservations_aud 
    ADD CONSTRAINT reservations_aud_payment_method_check 
    CHECK (payment_method IS NULL OR payment_method IN ('CREDIT_CARD', 'DEBIT_CARD', 'BLIK', 'PAYPAL', 'CASH', 'MOCK'));

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

