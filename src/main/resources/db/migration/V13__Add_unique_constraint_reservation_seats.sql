CREATE INDEX IF NOT EXISTS idx_reservation_seats_seat_reservation 
ON reservation_seats(seat_id, reservation_id);

CREATE INDEX IF NOT EXISTS idx_reservation_seats_reservation_seat 
ON reservation_seats(reservation_id, seat_id);


