-- Dodanie kolumny poster_path do tabeli movies
ALTER TABLE movies ADD COLUMN poster_path VARCHAR(500);

-- Indeks na poster_path (opcjonalnie, jeśli będziemy często wyszukiwać po okładce)
-- CREATE INDEX idx_movies_poster_path ON movies(poster_path);


