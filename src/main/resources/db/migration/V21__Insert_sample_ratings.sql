-- V21__Insert_sample_ratings.sql
-- Dodanie przykładowych ocen filmów
-- Oceny są przypisywane do losowych filmów i użytkowników

DO $$
DECLARE
    user_record RECORD;
    movie_record RECORD;
    rating_value INTEGER;
    user_count INTEGER;
    movie_count INTEGER;
    i INTEGER;
    random_movie_id BIGINT;
    random_user_id BIGINT;
BEGIN
    -- Sprawdź czy są użytkownicy i filmy
    SELECT COUNT(*) INTO user_count FROM users WHERE role = 'USER';
    SELECT COUNT(*) INTO movie_count FROM movies;
    
    IF user_count = 0 OR movie_count = 0 THEN
        RAISE NOTICE 'Brak użytkowników lub filmów. Pomijam dodawanie ocen.';
        RETURN;
    END IF;
    
    -- Dla każdego użytkownika dodaj kilka ocen
    FOR user_record IN SELECT id FROM users WHERE role = 'USER' ORDER BY id
    LOOP
        -- Każdy użytkownik oceni 3-7 losowych filmów
        FOR i IN 1..(3 + floor(random() * 5)::INTEGER)
        LOOP
            -- Wybierz losowy film
            SELECT id INTO random_movie_id 
            FROM movies 
            ORDER BY random() 
            LIMIT 1;
            
            -- Sprawdź czy użytkownik już ocenił ten film
            IF NOT EXISTS (
                SELECT 1 FROM movie_ratings 
                WHERE user_id = user_record.id AND movie_id = random_movie_id
            ) THEN
                -- Losowa ocena (1-5), ale z większym prawdopodobieństwem dla 3-5
                rating_value := CASE 
                    WHEN random() < 0.1 THEN 1  -- 10% szans na 1
                    WHEN random() < 0.2 THEN 2  -- 10% szans na 2
                    WHEN random() < 0.4 THEN 3  -- 20% szans na 3
                    WHEN random() < 0.7 THEN 4  -- 30% szans na 4
                    ELSE 5                       -- 30% szans na 5
                END;
                
                INSERT INTO movie_ratings (user_id, movie_id, rating, created_at, updated_at)
                VALUES (
                    user_record.id,
                    random_movie_id,
                    rating_value,
                    CURRENT_TIMESTAMP - (random() * INTERVAL '30 days'),
                    CURRENT_TIMESTAMP - (random() * INTERVAL '30 days')
                );
            END IF;
        END LOOP;
    END LOOP;
    
    RAISE NOTICE 'Dodano przykładowe oceny filmów.';
END $$;

