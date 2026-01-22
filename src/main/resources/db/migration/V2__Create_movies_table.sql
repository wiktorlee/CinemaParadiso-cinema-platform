-- Tabela filmów
CREATE TABLE movies (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    genre VARCHAR(100),
    director VARCHAR(255),
    duration_minutes INTEGER NOT NULL,
    release_date DATE,
    year INTEGER
);

-- Indeks na tytule dla szybszego wyszukiwania
CREATE INDEX idx_movies_title ON movies(title);



