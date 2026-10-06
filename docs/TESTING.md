# Testing Strategy & Automated Test Suites

## 1. Test Coverage Areas
- **Backend Unit & Integration Tests**: Ed25519 signing/verification, certificate issuance lifecycle, idempotent revocation, user authentication, and verification engine logic.
- **Android Domain Tests**: Expiry calculation logic, effective status state machine, and selective disclosure filtering.
- **Cryptographic Tamper Tests**: Verifies that altering any claim in a presentation payload causes immediate signature validation failure.
- **Replay Attack Tests**: Confirms reused presentation nonces are rejected.
