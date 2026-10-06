CREATE TABLE users (id UUID PRIMARY KEY, email VARCHAR(320) NOT NULL UNIQUE, password_hash VARCHAR(100) NOT NULL, display_name VARCHAR(100) NOT NULL, active BOOLEAN NOT NULL DEFAULT FALSE, locked BOOLEAN NOT NULL DEFAULT FALSE, created_at TIMESTAMPTZ NOT NULL);
CREATE TABLE otp_challenges (id UUID PRIMARY KEY, user_id UUID NOT NULL REFERENCES users(id), purpose VARCHAR(30) NOT NULL, otp_hash VARCHAR(100) NOT NULL, expires_at TIMESTAMPTZ NOT NULL, attempts INT NOT NULL DEFAULT 0, max_attempts INT NOT NULL, consumed_at TIMESTAMPTZ, created_at TIMESTAMPTZ NOT NULL);
CREATE INDEX idx_otp_user ON otp_challenges(user_id);
CREATE TABLE refresh_tokens (id UUID PRIMARY KEY, user_id UUID NOT NULL REFERENCES users(id), token_hash VARCHAR(64) NOT NULL UNIQUE, expires_at TIMESTAMPTZ NOT NULL, revoked_at TIMESTAMPTZ, replaced_by UUID, created_at TIMESTAMPTZ NOT NULL);
CREATE INDEX idx_refresh_user ON refresh_tokens(user_id);
CREATE TABLE sessions (id UUID PRIMARY KEY, user_id UUID NOT NULL REFERENCES users(id), refresh_token_id UUID REFERENCES refresh_tokens(id), status VARCHAR(20) NOT NULL, created_at TIMESTAMPTZ NOT NULL, last_seen_at TIMESTAMPTZ NOT NULL);
CREATE INDEX idx_session_user ON sessions(user_id);
CREATE TABLE audit_logs (id UUID PRIMARY KEY, user_id UUID, event_type VARCHAR(40) NOT NULL, request_id VARCHAR(100) NOT NULL, success BOOLEAN NOT NULL, target VARCHAR(200), created_at TIMESTAMPTZ NOT NULL);
CREATE INDEX idx_audit_user_time ON audit_logs(user_id, created_at);
