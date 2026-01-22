-- Dodanie indeksu dla szybszego sprawdzania dostępności miejsc
-- Unikalność jest zapewniana przez:
-- 1. Pessimistic locking w ReservationService (zapobiega race conditions)
-- 2. Sprawdzanie w logice biznesowej przed zapisem
-- 3. Ten indeks przyspiesza zapytania o dostępność miejsc

-- Indeks na seat_id + reservation_id dla szybszego wyszukiwania
CREATE INDEX IF NOT EXISTS idx_reservation_seats_seat_reservation 
ON reservation_seats(seat_id, reservation_id);

-- Indeks na reservation_id + seat_id (dla zapytań w drugą stronę)
CREATE INDEX IF NOT EXISTS idx_reservation_seats_reservation_seat 
ON reservation_seats(reservation_id, seat_id);

-- Uwaga: Unikalność miejsca w seansie jest zapewniana przez:
-- - Pessimistic locking (PESSIMISTIC_WRITE) w SeatRepository.findByIdWithLock()
-- - Sprawdzanie dostępności przed zapisem w ReservationService.createReservation()
-- - Transakcyjność (@Transactional) zapewnia atomowość operacji


