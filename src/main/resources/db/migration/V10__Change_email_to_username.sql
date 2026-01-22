-- Zmiana kolumny email na username w tabeli users
ALTER TABLE users RENAME COLUMN email TO username;

-- Zmiana nazwy indeksu
DROP INDEX IF EXISTS idx_users_email;
CREATE INDEX idx_users_username ON users(username);

-- Zmiana kolumny user_email na user_username w tabeli revinfo
ALTER TABLE revinfo RENAME COLUMN user_email TO user_username;

-- Zmiana nazwy indeksu w revinfo
DROP INDEX IF EXISTS idx_revinfo_user_email;
CREATE INDEX idx_revinfo_user_username ON revinfo(user_username);



