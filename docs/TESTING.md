# Testing

Android unit tests exercise public state transitions and logout cleanup using boundary fakes; MockWebServer is available for transport response, malformed body, timeout, and request-header cases. Instrumented Compose tests should cover login, registration, OTP, logout, and biometric UI on an emulator with controlled biometric commands.

Backend Spring tests use MockMvc and an H2 PostgreSQL compatibility database for registration and non-enumerating login errors. Add Testcontainers/PostgreSQL tests for Flyway and database-specific behavior before release. CI runs Android build/test/lint and backend test/build on every push and pull request.

Failure injection targets HTTP 400/401/403/429/500, socket timeouts, malformed JSON, expired/revoked refresh tokens, corrupt ciphertext, simultaneous refresh callers, and cancellation during logout. Test identities are synthetic and secrets never belong in fixtures.
