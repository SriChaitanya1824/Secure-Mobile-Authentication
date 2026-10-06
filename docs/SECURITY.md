# Security Architecture & Cryptographic Specifications

## 1. Cryptographic Algorithms
- **Digital Signatures**: Ed25519 (EdDSA over Curve25519) providing 128-bit security level with compact 64-byte signatures and 32-byte public keys.
- **Local Storage Encryption**: AES-256 in Galois/Counter Mode (GCM) with 128-bit authentication tags and random 96-bit initialization vectors (IVs).
- **Master Key Security**: Android Keystore hardware-backed KeyStore (`AndroidKeyStore`), ensuring cryptographic private keys cannot be extracted from application memory.
- **Password Hashing**: BCrypt with work factor 10.
- **Token Security**: HMAC-SHA256 signed JSON Web Tokens (JWT) for short-lived access and hashed refresh tokens.

## 2. Biometric Security
- Utilizes Android Jetpack `BiometricPrompt` with `BIOMETRIC_STRONG` or secure device PIN fallback.
- No biometric data (fingerprints, face vectors) ever leaves the device or is processed by the app.
