ALTER TABLE reservations DROP CONSTRAINT IF EXISTS reservations_status_check;

UPDATE reservations 
SET status = 'PAID' 
WHERE status = 'ACTIVE';

ALTER TABLE reservations 
    ADD CONSTRAINT reservations_status_check 
    CHECK (status IN ('PENDING_PAYMENT', 'PAID', 'CANCELLED', 'PAYMENT_FAILED'));

ALTER TABLE reservations 
    ADD COLUMN payment_method VARCHAR(50),
    ADD COLUMN payment_date TIMESTAMP,
    ADD COLUMN payment_transaction_id VARCHAR(255);

ALTER TABLE reservations 
    ALTER COLUMN status SET DEFAULT 'PENDING_PAYMENT';

