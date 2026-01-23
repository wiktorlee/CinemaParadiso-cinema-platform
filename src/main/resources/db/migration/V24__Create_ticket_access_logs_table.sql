CREATE TABLE ticket_access_logs (
    id BIGSERIAL PRIMARY KEY,
    reservation_id BIGINT NOT NULL,
    user_id BIGINT,
    action_type VARCHAR(50) NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ticket_access_logs_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE CASCADE
);

CREATE INDEX idx_ticket_access_logs_reservation_id ON ticket_access_logs(reservation_id);
CREATE INDEX idx_ticket_access_logs_user_id ON ticket_access_logs(user_id);
CREATE INDEX idx_ticket_access_logs_action_type ON ticket_access_logs(action_type);
CREATE INDEX idx_ticket_access_logs_created_at ON ticket_access_logs(created_at DESC);

