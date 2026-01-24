CREATE TABLE rooms (
    id BIGSERIAL PRIMARY KEY,
    room_number VARCHAR(50) NOT NULL UNIQUE,
    total_rows INTEGER NOT NULL,
    seats_per_row INTEGER NOT NULL
);

CREATE INDEX idx_rooms_room_number ON rooms(room_number);



