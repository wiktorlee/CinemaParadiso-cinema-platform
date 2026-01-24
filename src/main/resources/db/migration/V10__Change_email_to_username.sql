ALTER TABLE users RENAME COLUMN email TO username;

DROP INDEX IF EXISTS idx_users_email;
CREATE INDEX idx_users_username ON users(username);

ALTER TABLE revinfo RENAME COLUMN user_email TO user_username;

DROP INDEX IF EXISTS idx_revinfo_user_email;
CREATE INDEX idx_revinfo_user_username ON revinfo(user_username);



