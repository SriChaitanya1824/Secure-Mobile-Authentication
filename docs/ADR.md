# Architecture decisions

Each decision is accepted for this portfolio implementation.

| ADR | Context and decision | Alternatives | Consequences |
|---|---|---|---|
| 001 Kotlin | One null-safe language across Android/backend | Java, mixed languages | Coroutines and concise models; Kotlin toolchain required |
| 002 MVVM + Clean Architecture | UI must react to auth state; use public/domain/data boundaries | MVP, direct service calls | Testable boundaries; additional types |
| 003 Retrofit/OkHttp | Typed REST and interceptors are needed | Ktor, raw URLConnection | Mature ecosystem; Retrofit coupling stays internal |
| 004 Secure token storage | Encrypt the token envelope before preferences | Plain preferences, database | Confidentiality/integrity; corruption can end a session |
| 005 Android Keystore | Keep AES key non-exportable when supported | Bundled key, passphrase | Device-dependent hardware backing; lifecycle handling needed |
| 006 Biometric authentication | Delegate matching to BiometricPrompt | Custom biometric handling | Consistent OS security; never sees raw biometric data |
| 007 JWT + refresh tokens | Short stateless access plus revocable long session | Server session cookie, long JWT | Scalable access checks; rotation complexity |
| 008 SDK public API | Closed results and observable session state | Exceptions/callbacks | Exhaustive handling and coroutine-native integration |
| 009 Local rate limiting | Dependency-free local evaluation | Redis/Bucket4j gateway | Simple; unsuitable for multi-instance production |
| 010 PostgreSQL | Relational consistency for token lifecycle | Document DB/in-memory | Transactions and constraints; operational database required |
