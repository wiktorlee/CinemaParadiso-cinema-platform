CREATE TABLE revinfo (
    rev BIGSERIAL PRIMARY KEY,
    revtstmp BIGINT NOT NULL,
    user_id BIGINT,
    user_email VARCHAR(255),
    ip_address VARCHAR(45)
);

CREATE INDEX idx_revinfo_revtstmp ON revinfo(revtstmp);
CREATE INDEX idx_revinfo_user_id ON revinfo(user_id);
CREATE INDEX idx_revinfo_user_email ON revinfo(user_email);

