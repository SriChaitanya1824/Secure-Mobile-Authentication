# Threat model (STRIDE)

| Asset | Threat / attack | Risk | Mitigation | Remaining limitation |
|---|---|---:|---|---|
| Passwords | Information disclosure through logs/database | Critical | Never logged; BCrypt cost 12; TLS deployment requirement | A compromised app can capture typed input |
| Access tokens | Theft/replay | High | Five-minute expiry, Keystore-encrypted storage, authorization checks | Rooted devices/runtime instrumentation remain powerful |
| Refresh tokens | Database theft or replay | Critical | Random opaque value; server stores SHA-256 hash; rotate/revoke | Current token-family replay detection is limited |
| OTP | Brute force, replay, disclosure | High | Six digits, short expiry, BCrypt hash, max attempts, one-time consume, throttling | Development mode prints OTP and must be disabled |
| Accounts | Spoofing/enumeration | High | Generic login/registration errors, server validation, throttling | Timing normalization is not implemented |
| Requests | Tampering/replay | High | TLS requirement, signed JWTs, expirations, request IDs | No proof-of-possession or device attestation |
| Audit trail | Repudiation/tampering | Medium | Append-oriented schema and correlation IDs | Immutable external audit sink not implemented |
| Service | DoS / rate-limit bypass | High | Bounded in-process limits | Single-node counters reset and do not scale |
| Local storage | Ciphertext/key extraction | High | AES-GCM and non-exportable Keystore key, corruption cleanup | Hardware backing varies by device |
| Biometrics | Spoofing/SDK overclaim | High | OS `BiometricPrompt`; no raw biometric data | Security depends on device enrollment and OS |
| SDK host | Malicious host reads arguments/hooks process | Critical | Minimize retained secrets and narrow API | SDK cannot defend its process from its host |
| API | Elevation of privilege | Critical | Default-deny endpoint authorization and validated JWT | Roles/scopes are intentionally absent |

STRIDE categories covered are spoofing, tampering, repudiation, information disclosure, denial of service, and elevation of privilege. The SDK must be integrated only into trusted applications; obfuscation is not a security boundary.
