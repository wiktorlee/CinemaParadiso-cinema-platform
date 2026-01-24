ALTER TABLE reservations_aud DROP CONSTRAINT IF EXISTS reservations_aud_status_check;

UPDATE reservations_aud 
SET status = 'PAID' 
WHERE status = 'ACTIVE';

ALTER TABLE reservations_aud 
    ADD CONSTRAINT reservations_aud_status_check 
    CHECK (status IN ('PENDING_PAYMENT', 'PAID', 'CANCELLED', 'PAYMENT_FAILED'));

