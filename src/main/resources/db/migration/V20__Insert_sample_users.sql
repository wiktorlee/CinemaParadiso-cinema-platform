DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'anna.kowalska') THEN
        INSERT INTO users (username, password, first_name, last_name, role)
        VALUES ('anna.kowalska', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Anna', 'Kowalska', 'USER');
    END IF;

    IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'piotr.nowak') THEN
        INSERT INTO users (username, password, first_name, last_name, role)
        VALUES ('piotr.nowak', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Piotr', 'Nowak', 'USER');
    END IF;

    IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'maria.wisniewska') THEN
        INSERT INTO users (username, password, first_name, last_name, role)
        VALUES ('maria.wisniewska', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Maria', 'Wiśniewska', 'USER');
    END IF;

    IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'tomasz.wojcik') THEN
        INSERT INTO users (username, password, first_name, last_name, role)
        VALUES ('tomasz.wojcik', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Tomasz', 'Wójcik', 'USER');
    END IF;

    IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'katarzyna.kowalczyk') THEN
        INSERT INTO users (username, password, first_name, last_name, role)
        VALUES ('katarzyna.kowalczyk', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Katarzyna', 'Kowalczyk', 'USER');
    END IF;

    IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'michal.kaminski') THEN
        INSERT INTO users (username, password, first_name, last_name, role)
        VALUES ('michal.kaminski', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Michał', 'Kamiński', 'USER');
    END IF;

    IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'agnieszka.lewandowska') THEN
        INSERT INTO users (username, password, first_name, last_name, role)
        VALUES ('agnieszka.lewandowska', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Agnieszka', 'Lewandowska', 'USER');
    END IF;

    IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'jakub.zielinski') THEN
        INSERT INTO users (username, password, first_name, last_name, role)
        VALUES ('jakub.zielinski', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Jakub', 'Zieliński', 'USER');
    END IF;

    IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'natalia.szymanska') THEN
        INSERT INTO users (username, password, first_name, last_name, role)
        VALUES ('natalia.szymanska', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Natalia', 'Szymańska', 'USER');
    END IF;

    IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'lukasz.dabrowski') THEN
        INSERT INTO users (username, password, first_name, last_name, role)
        VALUES ('lukasz.dabrowski', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Łukasz', 'Dąbrowski', 'USER');
    END IF;
END $$;

