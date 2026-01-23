ALTER TABLE reservations
ADD COLUMN qr_code_token VARCHAR(255) UNIQUE,
ADD COLUMN qr_code_generated_at TIMESTAMP;

CREATE INDEX idx_reservations_qr_code_token ON reservations(qr_code_token);

