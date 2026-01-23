-- V22__Insert_sample_reviews.sql
-- Dodanie przykładowych recenzji filmów

DO $$
DECLARE
    user_record RECORD;
    random_movie_id BIGINT;
    review_texts TEXT[] := ARRAY[
        'Świetny film! Polecam każdemu. Fabuła wciągająca, aktorzy na wysokim poziomie. Zdecydowanie warto zobaczyć.',
        'Bardzo dobry film, choć nie bez wad. Wizualnie imponujący, ale niektóre wątki mogły być lepiej rozwinięte.',
        'Przeciętny film. Nie jest zły, ale też nie zachwyca. Można obejrzeć, ale nie jest to must-see.',
        'Fantastyczny film! Jeden z najlepszych jakie widziałem w tym roku. Reżyseria, scenariusz, aktorzy - wszystko na najwyższym poziomie.',
        'Rozczarowanie. Spodziewałem się więcej po takiej obsadzie. Fabuła przewidywalna, brakuje oryginalności.',
        'Doskonały film! Emocje, akcja, humor - wszystko w idealnych proporcjach. Zdecydowanie polecam!',
        'Ciekawy film, choć nie dla każdego. Wymaga skupienia, ale nagrodzi widza głęboką fabułą.',
        'Świetna rozrywka! Idealny film na wieczór. Nie trzeba myśleć, po prostu ciesz się oglądaniem.',
        'Bardzo dobry film, ale zakończenie mogło być lepsze. Mimo to warto zobaczyć.',
        'Absolutnie genialny film! Każda scena jest przemyślana, każdy dialog ma znaczenie. Arcydzieło.',
        'Niezły film, choć nie powala. Dobre wykonanie, ale brakuje tego "czegoś" co czyni film wyjątkowym.',
        'Rewelacyjny film! Zaskakujące zwroty akcji, świetna gra aktorska. Jeden z tych filmów, które zostają w pamięci.',
        'Dobry film, ale nie bez wad. Wizualnie piękny, ale fabuła czasami się dłuży.',
        'Świetny film dla całej rodziny. Humor, emocje i wartościowa treść. Polecam!',
        'Przeciętny film. Nie jest zły, ale też nie zachwyca. Można obejrzeć raz.',
        'Fantastyczny film! Wciągający od pierwszej minuty. Zdecydowanie jeden z lepszych filmów tego roku.',
        'Dobry film, choć nie dla każdego. Wymaga cierpliwości, ale nagrodzi widza.',
        'Świetna rozrywka! Idealny film na relaks. Nie trzeba myśleć, po prostu ciesz się.',
        'Bardzo dobry film z ciekawym przesłaniem. Warto zobaczyć i przemyśleć.',
        'Doskonały film! Emocje, akcja, humor - wszystko w idealnych proporcjach. Polecam każdemu!'
    ];
    review_text TEXT;
    user_count INTEGER;
    movie_count INTEGER;
    reviews_added INTEGER := 0;
BEGIN
    -- Sprawdź czy są użytkownicy i filmy
    SELECT COUNT(*) INTO user_count FROM users WHERE role = 'USER';
    SELECT COUNT(*) INTO movie_count FROM movies;
    
    IF user_count = 0 OR movie_count = 0 THEN
        RAISE NOTICE 'Brak użytkowników lub filmów. Pomijam dodawanie recenzji.';
        RETURN;
    END IF;
    
    -- Dla każdego użytkownika dodaj 1-3 recenzje
    FOR user_record IN SELECT id FROM users WHERE role = 'USER' ORDER BY id
    LOOP
        -- Każdy użytkownik napisze 1-3 recenzje
        FOR i IN 1..(1 + floor(random() * 3)::INTEGER)
        LOOP
            -- Wybierz losowy film, który użytkownik jeszcze nie zrecenzował
            SELECT m.id INTO random_movie_id
            FROM movies m
            WHERE NOT EXISTS (
                SELECT 1 FROM reviews 
                WHERE user_id = user_record.id AND movie_id = m.id
            )
            ORDER BY random()
            LIMIT 1;
            
            -- Jeśli znaleziono film, dodaj recenzję
            IF random_movie_id IS NOT NULL THEN
                -- Wybierz losowy tekst recenzji
                review_text := review_texts[1 + floor(random() * array_length(review_texts, 1))::INTEGER];
                
                INSERT INTO reviews (user_id, movie_id, content, created_at, updated_at)
                VALUES (
                    user_record.id,
                    random_movie_id,
                    review_text,
                    CURRENT_TIMESTAMP - (random() * INTERVAL '60 days'),
                    CURRENT_TIMESTAMP - (random() * INTERVAL '60 days')
                );
                
                reviews_added := reviews_added + 1;
            END IF;
        END LOOP;
    END LOOP;
    
    RAISE NOTICE 'Dodano % przykładowych recenzji.', reviews_added;
END $$;

