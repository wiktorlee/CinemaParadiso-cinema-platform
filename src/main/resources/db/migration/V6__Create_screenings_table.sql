CREATE TABLE screenings (
    id BIGSERIAL PRIMARY KEY,
    movie_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    base_price NUMERIC(10, 2) NOT NULL,
    vip_price NUMERIC(10, 2),
    schedule_id BIGINT,
    CONSTRAINT fk_screenings_movie FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE,
    CONSTRAINT fk_screenings_room FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE,
    CONSTRAINT fk_screenings_schedule FOREIGN KEY (schedule_id) REFERENCES screening_schedules(id) ON DELETE SET NULL
);

CREATE INDEX idx_screenings_movie_id ON screenings(movie_id);
CREATE INDEX idx_screenings_room_id ON screenings(room_id);
CREATE INDEX idx_screenings_start_time ON screenings(start_time);
CREATE INDEX idx_screenings_schedule_id ON screenings(schedule_id);



