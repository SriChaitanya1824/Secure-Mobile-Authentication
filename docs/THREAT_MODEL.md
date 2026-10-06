# STRIDE Threat Model — Secure Digital Certificate Wallet

| Threat Category | Asset / Target | Threat Scenario | Mitigation in System | Residual Risk / Limitation |
| :--- | :--- | :--- | :--- | :--- |
| **Spoofing** | Issuer Authority | Attacker crafts a fake university degree certificate claiming to be VIT. | Asymmetric Ed25519 digital signature verified against Trusted Issuer Registry public keys. | Requires secure distribution of root CA / registry keys. |
| **Tampering** | Certificate Claims | Holder alters degree GPA or Name in the certificate payload. | Signature covers canonical JSON of all claims; altering any byte causes verification failure. | None; mathematically tamper-evident. |
| **Repudiation** | Revocation Event | Issuer claims they never revoked a disputed credential. | Immutable `revocations` table and signed audit trail logging actor, timestamp, and reason. | Database administrator privilege abuse. |
| **Information Disclosure** | Sensitive PII | Verifier sees Holder's Date of Birth, Student ID, or address when only age is needed. | Selective disclosure simulation allows holder to toggle unneeded claims off before signing QR. | Full zero-knowledge proofs (BBS+) required for mathematical unlinkability in production. |
| **Denial of Service** | Verification API | Attacker floods verification API with high-frequency scan requests. | In-memory token bucket rate limiting (200 req/min per IP) and short-lived nonce validation. | Distributed botnet attacks require cloud WAF/CDN. |
| **Elevation of Privilege** | Local App Storage | Malware on phone attempts to steal cached credentials or session tokens. | Android Keystore hardware-backed AES-256-GCM encryption and BiometricPrompt unlock. | Rooted devices with kernel-level instrumentation. |
