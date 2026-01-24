CREATE TABLE reservation_seats (
    id BIGSERIAL PRIMARY KEY,
    reservation_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL,
    ticket_type VARCHAR(50) NOT NULL CHECK (ticket_type IN ('NORMAL', 'REDUCED', 'STUDENT')),
    price NUMERIC(10, 2) NOT NULL,
    CONSTRAINT fk_reservation_seats_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE CASCADE,
    CONSTRAINT fk_reservation_seats_seat FOREIGN KEY (seat_id) REFERENCES seats(id) ON DELETE CASCADE
);

CREATE INDEX idx_reservation_seats_reservation_id ON reservation_seats(reservation_id);
CREATE INDEX idx_reservation_seats_seat_id ON reservation_seats(seat_id);



