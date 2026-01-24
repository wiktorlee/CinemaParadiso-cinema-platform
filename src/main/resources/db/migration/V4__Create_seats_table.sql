CREATE TABLE seats (
    id BIGSERIAL PRIMARY KEY,
    room_id BIGINT NOT NULL,
    row_number INTEGER NOT NULL,
    seat_number INTEGER NOT NULL,
    seat_type VARCHAR(50) NOT NULL CHECK (seat_type IN ('STANDARD', 'VIP')),
    CONSTRAINT fk_seats_room FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE,
    CONSTRAINT uk_seats_room_row_seat UNIQUE (room_id, row_number, seat_number)
);

CREATE INDEX idx_seats_room_id ON seats(room_id);
CREATE INDEX idx_seats_room_row_seat ON seats(room_id, row_number, seat_number);



