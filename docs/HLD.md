# High-level design

## Goals and boundaries

The system centralizes registration, password login, OTP verification, short-lived access tokens, refresh rotation, device-protected persistence, biometric gating, and logout. It does not provide identity proofing, certificate issuance, SMS delivery, fraud scoring, or guaranteed hardware-backed keys.

```mermaid
flowchart LR
  U[User] --> A[Sample app]
  A --> S[SecureAuthSDK]
  S -->|TLS REST| B[Spring Boot API]
  B --> P[(PostgreSQL)]
  S --> K[Android Keystore]
```

```mermaid
flowchart TB
  API[Public API] --> UC[Use cases/session state]
  UC --> R[Repository client]
  R --> N[Retrofit + OkHttp]
  R --> ST[Encrypted token storage]
  ST --> KS[Android Keystore]
  N --> C[Controllers] --> SV[Auth services] --> DB[(PostgreSQL)]
```

```mermaid
sequenceDiagram
  participant App
  participant SDK
  participant API
  App->>SDK: login(email, password)
  SDK->>API: POST /login
  API-->>SDK: challengeId
  SDK-->>App: OtpRequired
```

```mermaid
sequenceDiagram
  App->>SDK: verifyOtp(challenge, code)
  SDK->>API: POST /otp/verify
  API->>API: expiry, attempt, one-use checks
  API-->>SDK: access + rotating refresh token
  SDK->>SDK: Keystore-encrypted persistence
```

```mermaid
sequenceDiagram
  participant A as Requests A/B/C
  participant S as SDK mutex
  participant API
  A->>S: expired access token
  S->>API: one refresh request
  API-->>S: rotated tokens
  S-->>A: resume with shared result
```

```mermaid
flowchart LR
  App --> Prompt[BiometricPrompt]
  Prompt --> OS[Android biometric service]
  OS -->|success/failure only| SDK
  SDK --> Session[Unlock local session policy]
```

Deployment is a containerized API plus PostgreSQL; the Android app reaches it over REST. Trust boundaries are the host app/SDK, device OS/Keystore, network/TLS edge, API, and database. Horizontal production scaling requires shared rate-limit state and revocation state.
