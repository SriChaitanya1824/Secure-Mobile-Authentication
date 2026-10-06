# Business Rules & Validation Constraints

1. **Trusted Issuer Requirement**: Only organizations registered with status `TRUSTED` in the Trusted Issuer Registry can issue valid certificates.
2. **Revocation Precedence**: A certificate marked `REVOKED` must immediately fail verification, regardless of validity dates. Revocation cannot be undone.
3. **Suspension Handling**: A certificate marked `SUSPENDED` is temporarily invalid for verification until reactivated by the issuer.
4. **Automatic Expiry Logic**: If current timestamp `now >= expiresAt`, the system automatically treats the status as `EXPIRED`.
5. **Holder Share Sovereignty**: The wallet holder has full control over which claims are included in a presentation QR code.
6. **Replay Nonce Uniqueness**: A presentation QR code contains a single-use UUID nonce and presentation timestamp. Once verified, the nonce cannot be reused.
7. **Idempotent Operations**: Issuing a certificate with an existing ID or revoking an already revoked certificate must return clean, deterministic responses without corrupting data.
