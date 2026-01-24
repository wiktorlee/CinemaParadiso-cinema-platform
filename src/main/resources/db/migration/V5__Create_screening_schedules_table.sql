CREATE TABLE screening_schedules (
    id BIGSERIAL PRIMARY KEY,
    movie_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    day_of_week VARCHAR(50) NOT NULL CHECK (day_of_week IN ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY')),
    start_time TIME NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    base_price NUMERIC(10, 2) NOT NULL,
    vip_price NUMERIC(10, 2),
    CONSTRAINT fk_schedules_movie FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE,
    CONSTRAINT fk_schedules_room FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE,
    CONSTRAINT chk_schedules_dates CHECK (end_date >= start_date)
);

CREATE INDEX idx_schedules_movie_id ON screening_schedules(movie_id);
CREATE INDEX idx_schedules_room_id ON screening_schedules(room_id);
CREATE INDEX idx_schedules_dates ON screening_schedules(start_date, end_date);



