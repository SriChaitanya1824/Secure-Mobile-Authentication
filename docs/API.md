# REST API Specification & OpenAPI Contracts

Live Swagger UI available at: `http://localhost:8080/swagger-ui.html` and `http://localhost:8081/swagger-ui.html`

## 1. Authentication Endpoints (`/api/auth`)
- `POST /api/auth/register`: Register new user.
- `POST /api/auth/login`: Login with email and password, returning JWT access token & refresh token.
- `POST /api/auth/refresh`: Refresh expired access token.
- `POST /api/auth/logout`: Invalidate refresh token session.

## 2. Issuer Endpoints (`/api/issuer`)
- `POST /api/issuer/certificates`: Issue and digitally sign certificate with Ed25519.
- `GET /api/issuer/certificates`: List all issued certificates.

## 3. Certificate & Revocation Endpoints (`/api/certificates`)
- `GET /api/certificates/{id}`: Fetch certificate details.
- `POST /api/certificates/{id}/revoke`: Idempotently revoke certificate with reason.

## 4. Wallet & Sync Endpoints (`/api/wallet`, `/api/sync`)
- `GET /api/wallet/certificates?subjectId={id}`: Fetch all holder certificates.
- `GET /api/sync/certificates?since={isoTimestamp}`: Delta synchronization of updated and revoked credentials.

## 5. Verification Endpoints (`/api/verification` - Port 8081)
- `POST /api/verification/verify`: Cryptographically verify signed QR presentation payload.
- `GET /api/verification/history`: Retrieve recent verifier audit trail.
