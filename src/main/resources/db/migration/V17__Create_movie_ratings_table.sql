-- Tabela ocen filmów
CREATE TABLE movie_ratings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    movie_id BIGINT NOT NULL,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_movie_ratings_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_movie_ratings_movie FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE,
    CONSTRAINT uk_movie_ratings_user_movie UNIQUE (user_id, movie_id)
);

-- Indeksy dla szybszego wyszukiwania
CREATE INDEX idx_movie_ratings_movie_id ON movie_ratings(movie_id);
CREATE INDEX idx_movie_ratings_user_id ON movie_ratings(user_id);
CREATE INDEX idx_movie_ratings_rating ON movie_ratings(rating);


