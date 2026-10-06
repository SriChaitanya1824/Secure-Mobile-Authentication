# High-Level Design (HLD) — Secure Digital Certificate Wallet

## 1. Business Problem & System Context
Organizations, educational institutions, and enterprises issue credentials that are traditionally easily forged, hard to verify in real-time, and difficult to share with minimal privacy leakage. 

The **Secure Digital Certificate Wallet** system provides a verifiable digital identity architecture composed of three main actors:
1. **Issuer (Certificate Authority / University / Employer)**: Issues, cryptographically signs with Ed25519, and manages the lifecycle (issuance, revocation, suspension) of digital certificates.
2. **Holder (Wallet User)**: Stores certificates locally in an encrypted Room database protected by Android Keystore and BiometricPrompt, presenting selective disclosure claims via dynamic QR codes.
3. **Verifier (Relying Party / Employer)**: Scans QR presentation payloads, cryptographically validates digital signatures against trusted issuer public keys, verifies non-expiration, verifies live revocation status, and checks replay protections.

---

## 2. High-Level Architecture

```mermaid
flowchart TD
    subgraph Issuer["Issuer Authority (VIT Academic Credentials)"]
        IA[Admin / Issuer Portal] -->|Issue & Sign Credential| IS[Issuer Backend Service]
        IS -->|Store Metadata| IDB[(PostgreSQL / Flyway)]
        IS -->|Publish Ed25519 Public Key| TR[Trusted Issuer Registry]
    end

    subgraph Wallet["Android Digital Certificate Wallet (Holder)"]
        UI[Jetpack Compose UI] --> VM[MVVM ViewModels]
        VM --> UC[Clean Architecture Use Cases]
        UC --> REPO[Offline-First Repository]
        REPO --> DB[(Room Encrypted Cache)]
        REPO --> KS[Android Keystore & Biometrics]
        REPO <-->|HTTPS REST / Delta Sync| IS
    end

    subgraph Verifier["Relying Party / Verifier"]
        VUI[CameraX QR Scanner] --> VP[Verify Engine Service]
        VP -->|Fetch Public Keys| TR
        VP -->|Live Revocation Check| IS
        VP --> VDB[(Verification Audit DB)]
    end

    Wallet -->|Signed Presentation QR| Verifier
```

---

## 3. Sequence Diagrams

### 3.1 Certificate Issuance Flow

```mermaid
sequenceDiagram
    autonumber
    actor Issuer as Issuer Authority
    participant API as Issuer Backend API
    participant Crypto as Ed25519 Crypto Service
    participant DB as PostgreSQL DB
    actor Holder as Wallet Holder

    Issuer->>API: POST /api/issuer/certificates (Claims, Subject, Expiry)
    API->>Crypto: Canonicalize JSON Map
    Crypto-->>API: Deterministic Canonical String
    API->>Crypto: Sign(Canonical String, Issuer Private Key)
    Crypto-->>API: Ed25519 Base64 Signature
    API->>DB: Persist Certificate & Claims (ACTIVE)
    DB-->>API: Saved
    API-->>Issuer: 200 OK (CertificateDto)
    Holder->>API: GET /api/wallet/certificates?subjectId=usr-001
    API-->>Holder: Return Digital Certificates
```

### 3.2 Wallet Synchronization Flow

```mermaid
sequenceDiagram
    autonumber
    actor Holder as Wallet Holder
    participant App as Android Wallet
    participant Room as Local Room Database
    participant API as Issuer Backend Sync API

    Holder->>App: Open Wallet / Pull to Refresh
    App->>Room: Load cached credentials (Instant display)
    App->>API: GET /api/sync/certificates?since=2026-01-01T00:00:00Z
    API-->>App: SyncResponse (Updated certs, Revoked IDs)
    App->>Room: Update local cache & mark revocations
    App-->>Holder: Update UI with Synced State
```

### 3.3 QR Verification Flow

```mermaid
sequenceDiagram
    autonumber
    actor Holder as Wallet Holder
    actor Verifier as Verifier Agent
    participant Scanner as CameraX / QR Scanner
    participant Engine as Verifier Engine Service
    participant Trust as Trusted Issuer Registry
    participant Issuer as Issuer Service

    Holder->>Holder: Select claims & Generate Signed QR
    Verifier->>Scanner: Scan Presentation QR
    Scanner->>Engine: POST /api/verification/verify (Payload JSON)
    Engine->>Engine: Check Replay Nonce Cache
    Engine->>Engine: Validate Expiration Date
    Engine->>Trust: Validate Issuer ID & Get Public Key
    Engine->>Engine: Verify Ed25519 Signature
    Engine->>Issuer: GET /api/certificates/{id} (Check Revocation)
    Issuer-->>Engine: Status: ACTIVE / REVOKED / SUSPENDED
    Engine-->>Scanner: VerificationResult (VERIFIED / EXPIRED / REVOKED)
    Scanner-->>Verifier: Display Verification Result & Verified Claims
```

### 3.4 Certificate Revocation Flow

```mermaid
sequenceDiagram
    autonumber
    actor Issuer as Issuer Authority
    participant API as Issuer Backend API
    participant DB as PostgreSQL DB
    participant Audit as Audit Log

    Issuer->>API: POST /api/certificates/{id}/revoke (Reason: "Administrative error")
    API->>DB: Find certificate by ID
    API->>DB: Update status = 'REVOKED', updated_at = NOW()
    API->>DB: Insert Revocation Record
    API->>Audit: Record CERTIFICATE_REVOKED
    API-->>Issuer: 200 OK (RevocationResponse)
```
