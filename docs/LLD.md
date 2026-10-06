# Low-Level Design (LLD) — Secure Digital Certificate Wallet

## 1. Android Client Architecture

The Android application is organized under a modular Clean Architecture pattern using Kotlin, Jetpack Compose, Material 3, Hilt, Room, and Retrofit.

### 1.1 Module Structure & Responsibilities

| Module | Namespace | Purpose |
| :--- | :--- | :--- |
| `:android:core` | `com.srichaitanya.wallet.core` | Pure domain models (`DigitalCertificate`, `Proof`, `Resource`), UseCases, and Repository Interfaces. |
| `:android:core-security` | `com.srichaitanya.wallet.security` | Android Keystore AES-256-GCM encryption manager, `BiometricAuthManager`, and Ed25519 verification. |
| `:android:core-network` | `com.srichaitanya.wallet.network` | Retrofit service interfaces, OkHttp interceptors, Network DTOs, and Serialization mappers. |
| `:android:core-database` | `com.srichaitanya.wallet.database` | Room database (`WalletDatabase`), DAOs, Room entities, and local caching. |
| `:android:core-ui` | `com.srichaitanya.wallet.ui` | Material 3 Theme, reusable components (`CertificateCard`, `StatusBadge`, `SyncStateBanner`). |
| `:android:feature-auth` | `com.srichaitanya.wallet.auth` | Login and Register Jetpack Compose screens and `AuthViewModel`. |
| `:android:feature-wallet` | `com.srichaitanya.wallet.wallet` | Home screen, Certificate list, Certificate detail screen, and `WalletViewModel`. |
| `:android:feature-certificate` | `com.srichaitanya.wallet.certificate` | Selective disclosure claim selection and dynamic QR presentation generator. |
| `:android:feature-scanner` | `com.srichaitanya.wallet.scanner` | CameraX QR Scanner analyzer and manual payload fallback. |
| `:android:feature-verification` | `com.srichaitanya.wallet.verification` | Verification result display and verification history audit log. |
| `:android:feature-profile` | `com.srichaitanya.wallet.profile` | User profile, security status, and logout screen. |
| `:android:app` | `com.srichaitanya.wallet` | Application entry point, Hilt dependency injection modules, Navigation Host. |
