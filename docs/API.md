# REST API

All JSON calls accept/return `application/json`. Clients may send `X-Request-ID`; every response returns it. Errors are `{ "code", "message", "requestId" }`.

| Method/path | Auth | Request | Success | Errors |
|---|---|---|---|---|
| POST `/api/auth/register` | No | `email,password,displayName` | 201 challenge | 400, 409, 429 |
| POST `/api/auth/login` | No | `email,password` | 200 challenge | 400, 401, 423, 429 |
| POST `/api/auth/otp/verify` | No | `challengeId,otp` | 200 tokens | 400, 423, 429 |
| POST `/api/auth/otp/resend` | No | `challengeId` | 200 new challenge | 404, 429 |
| POST `/api/auth/refresh` | No | `refreshToken` | 200 rotated tokens | 401, 429 |
| POST `/api/auth/logout` | No | optional `refreshToken` | 204 | 400 |
| GET `/api/users/me` | Bearer JWT | none | 200 user profile | 401 |
| POST `/api/auth/biometric/session` | Bearer JWT | none | 200 user profile | 401 |
| GET `/api/health` | No | none | 200 status | 500 |

A challenge contains `challengeId`, `expiresInSeconds`, and `resendAfterSeconds`. Tokens contain `accessToken`, `refreshToken`, `expiresInSeconds`, and `tokenType`. Swagger UI is at `/swagger-ui/index.html`; OpenAPI JSON is `/v3/api-docs`.
