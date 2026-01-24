ALTER TABLE rooms ADD COLUMN description TEXT;

ALTER TABLE seats ADD COLUMN is_available BOOLEAN NOT NULL DEFAULT true;

CREATE INDEX idx_seats_is_available ON seats(is_available);

