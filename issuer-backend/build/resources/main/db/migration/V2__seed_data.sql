INSERT INTO users (id, email, password_hash, full_name, role)
VALUES 
('usr-vit-issuer-001', 'issuer@vit.ac.in', '$2a$10$eO0V2g7/Zf9c0Vz6iW8Wd.k4s4gQ0LwB3g8D0q2w1Y0e5U9b8Q1Z.', 'VIT University Registrar', 'ISSUER'),
('usr-student-001', 'student@wallet.com', '$2a$10$eO0V2g7/Zf9c0Vz6iW8Wd.k4s4gQ0LwB3g8D0q2w1Y0e5U9b8Q1Z.', 'John Doe', 'HOLDER'),
('usr-verifier-001', 'verifier@enterprise.com', '$2a$10$eO0V2g7/Zf9c0Vz6iW8Wd.k4s4gQ0LwB3g8D0q2w1Y0e5U9b8Q1Z.', 'Demo Employer Inc', 'VERIFIER');

INSERT INTO issuers (id, issuer_id_uri, name, public_key, status)
VALUES 
('iss-vit-001', 'did:vid:issuer:vit-university', 'VIT Academic Credentials', 'MCowBQYDK2VwAyEALb31238912u91283u1283u129831928312983129831=', 'TRUSTED');

INSERT INTO certificates (
    certificate_id, credential_type, issuer_id, issuer_name, subject_id, subject_name,
    issued_at, valid_from, expires_at, status, credential_version, signature_algorithm, signature, raw_canonical_payload
) VALUES (
    'cert-vit-btech-2026-001',
    'University Degree',
    'did:vid:issuer:vit-university',
    'VIT Academic Credentials',
    'usr-student-001',
    'John Doe',
    '2026-01-15T09:00:00Z',
    '2026-01-15T09:00:00Z',
    '2036-01-15T09:00:00Z',
    'ACTIVE',
    '1.0',
    'Ed25519',
    'w5R8x5Q7+8mQ7L2v1z9w8e7r6t5y4u3i2o1p+A==',
    '{"certificateId":"cert-vit-btech-2026-001","credentialType":"University Degree","issuerId":"did:vid:issuer:vit-university","subjectId":"usr-student-001"}'
);

INSERT INTO certificate_claims (certificate_id, claim_key, claim_value, data_type, is_sensitive) VALUES
('cert-vit-btech-2026-001', 'degree', 'Bachelor of Technology in Computer Science', 'STRING', false),
('cert-vit-btech-2026-001', 'gpa', '3.92', 'STRING', false),
('cert-vit-btech-2026-001', 'honors', 'Summa Cum Laude', 'STRING', false),
('cert-vit-btech-2026-001', 'dateOfBirth', '2001-05-14', 'STRING', true),
('cert-vit-btech-2026-001', 'studentId', '21BCE1824', 'STRING', true);

INSERT INTO certificates (
    certificate_id, credential_type, issuer_id, issuer_name, subject_id, subject_name,
    issued_at, valid_from, expires_at, status, credential_version, signature_algorithm, signature, raw_canonical_payload
) VALUES (
    'cert-vit-training-2023-002',
    'Training Certificate',
    'did:vid:issuer:vit-university',
    'VIT Academic Credentials',
    'usr-student-001',
    'John Doe',
    '2023-01-01T00:00:00Z',
    '2023-01-01T00:00:00Z',
    '2024-01-01T00:00:00Z',
    'EXPIRED',
    '1.0',
    'Ed25519',
    'k8v2q1p4+8mQ7L2v1z9w8e7r6t5y4u3i2o1p+B==',
    '{"certificateId":"cert-vit-training-2023-002","credentialType":"Training Certificate","issuerId":"did:vid:issuer:vit-university","subjectId":"usr-student-001"}'
);

INSERT INTO certificate_claims (certificate_id, claim_key, claim_value, data_type, is_sensitive) VALUES
('cert-vit-training-2023-002', 'course', 'Software Engineering Fundamentals & Cloud Architecture', 'STRING', false),
('cert-vit-training-2023-002', 'grade', 'A+', 'STRING', false);

INSERT INTO certificates (
    certificate_id, credential_type, issuer_id, issuer_name, subject_id, subject_name,
    issued_at, valid_from, expires_at, status, credential_version, signature_algorithm, signature, raw_canonical_payload
) VALUES (
    'cert-vit-intern-2025-003',
    'Employment Certificate',
    'did:vid:issuer:vit-university',
    'VIT Academic Credentials',
    'usr-student-001',
    'John Doe',
    '2025-06-01T00:00:00Z',
    '2025-06-01T00:00:00Z',
    '2027-06-01T00:00:00Z',
    'REVOKED',
    '1.0',
    'Ed25519',
    'z9x8c7v6+8mQ7L2v1z9w8e7r6t5y4u3i2o1p+C==',
    '{"certificateId":"cert-vit-intern-2025-003","credentialType":"Employment Certificate","issuerId":"did:vid:issuer:vit-university","subjectId":"usr-student-001"}'
);

INSERT INTO certificate_claims (certificate_id, claim_key, claim_value, data_type, is_sensitive) VALUES
('cert-vit-intern-2025-003', 'role', 'Graduate Research Assistant', 'STRING', false),
('cert-vit-intern-2025-003', 'department', 'Cybersecurity Labs', 'STRING', false);

INSERT INTO revocations (certificate_id, reason, revoked_by, revoked_at)
VALUES ('cert-vit-intern-2025-003', 'Administrative error during issuance', 'VIT Academic Credentials Registrar', '2025-08-10T14:30:00Z');

INSERT INTO certificates (
    certificate_id, credential_type, issuer_id, issuer_name, subject_id, subject_name,
    issued_at, valid_from, expires_at, status, credential_version, signature_algorithm, signature, raw_canonical_payload
) VALUES (
    'cert-vit-cloud-2025-004',
    'Professional License',
    'did:vid:issuer:vit-university',
    'VIT Academic Credentials',
    'usr-student-001',
    'John Doe',
    '2025-03-10T00:00:00Z',
    '2025-03-10T00:00:00Z',
    '2028-03-10T00:00:00Z',
    'SUSPENDED',
    '1.0',
    'Ed25519',
    'a1s2d3f4+8mQ7L2v1z9w8e7r6t5y4u3i2o1p+D==',
    '{"certificateId":"cert-vit-cloud-2025-004","credentialType":"Professional License","issuerId":"did:vid:issuer:vit-university","subjectId":"usr-student-001"}'
);

INSERT INTO certificate_claims (certificate_id, claim_key, claim_value, data_type, is_sensitive) VALUES
('cert-vit-cloud-2025-004', 'certification', 'Advanced Cloud Security Architect', 'STRING', false),
('cert-vit-cloud-2025-004', 'level', 'Level 3 Expert', 'STRING', false);
