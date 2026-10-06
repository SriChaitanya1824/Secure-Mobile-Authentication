CREATE TABLE users (
    id VARCHAR(64) PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE issuers (
    id VARCHAR(64) PRIMARY KEY,
    issuer_id_uri VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    public_key TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'TRUSTED',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE certificates (
    certificate_id VARCHAR(64) PRIMARY KEY,
    credential_type VARCHAR(100) NOT NULL,
    issuer_id VARCHAR(255) NOT NULL,
    issuer_name VARCHAR(255) NOT NULL,
    subject_id VARCHAR(255) NOT NULL,
    subject_name VARCHAR(255) NOT NULL,
    issued_at TIMESTAMP WITH TIME ZONE NOT NULL,
    valid_from TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    credential_version VARCHAR(20) NOT NULL DEFAULT '1.0',
    signature_algorithm VARCHAR(50) NOT NULL DEFAULT 'Ed25519',
    signature TEXT NOT NULL,
    raw_canonical_payload TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE certificate_claims (
    id BIGSERIAL PRIMARY KEY,
    certificate_id VARCHAR(64) NOT NULL REFERENCES certificates(certificate_id) ON DELETE CASCADE,
    claim_key VARCHAR(100) NOT NULL,
    claim_value TEXT NOT NULL,
    data_type VARCHAR(50) NOT NULL DEFAULT 'STRING',
    is_sensitive BOOLEAN DEFAULT FALSE
);

CREATE TABLE certificate_events (
    id BIGSERIAL PRIMARY KEY,
    certificate_id VARCHAR(64) NOT NULL REFERENCES certificates(certificate_id) ON DELETE CASCADE,
    event_type VARCHAR(50) NOT NULL,
    actor VARCHAR(255) NOT NULL,
    details TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE revocations (
    id BIGSERIAL PRIMARY KEY,
    certificate_id VARCHAR(64) UNIQUE NOT NULL REFERENCES certificates(certificate_id) ON DELETE CASCADE,
    reason VARCHAR(255) NOT NULL,
    revoked_by VARCHAR(255) NOT NULL,
    revoked_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE verification_events (
    id BIGSERIAL PRIMARY KEY,
    certificate_id VARCHAR(64) NOT NULL,
    verifier_name VARCHAR(255) NOT NULL,
    verification_status VARCHAR(50) NOT NULL,
    checks_passed JSONB,
    ip_address VARCHAR(100),
    verified_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    actor VARCHAR(255) NOT NULL,
    target_id VARCHAR(255),
    details TEXT,
    ip_address VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_certificates_subject ON certificates(subject_id);
CREATE INDEX idx_certificates_issuer ON certificates(issuer_id);
CREATE INDEX idx_certificates_status ON certificates(status);
CREATE INDEX idx_claims_cert_id ON certificate_claims(certificate_id);
CREATE INDEX idx_audit_created_at ON audit_logs(created_at);
