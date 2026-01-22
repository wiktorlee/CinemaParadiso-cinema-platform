-- Dodaj opcjonalne pole description do tabeli rooms
ALTER TABLE rooms ADD COLUMN description TEXT;

-- Dodaj pole is_available do tabeli seats (domyślnie true)
ALTER TABLE seats ADD COLUMN is_available BOOLEAN NOT NULL DEFAULT true;

-- Indeks dla szybszego wyszukiwania dostępnych miejsc
CREATE INDEX idx_seats_is_available ON seats(is_available);

