# SDK usage

## Install

Publish locally with `./gradlew :android-sdk:secureauth:publishToMavenLocal`, then add `mavenLocal()` and `implementation("com.srichaitanya.secureauth:secureauth:1.0.0")`. The SDK requires Android 8/API 26 or newer.

## Initialize and authenticate

```kotlin
val auth = SecureAuth.initialize(
    applicationContext,
    SecureAuthConfiguration(baseUrl = "https://auth.example.com/")
)

lifecycleScope.launch {
    when (val result = auth.login(email, password)) {
        is AuthResult.OtpRequired -> showOtp(result.challengeId)
        AuthResult.InvalidCredentials -> showInvalidCredentials()
        else -> showError(result)
    }
}

lifecycleScope.launch {
    when (auth.verifyOtp(challengeId, otp)) {
        is AuthResult.Success -> openHome()
        AuthResult.InvalidOtp -> showInvalidOtp()
        else -> showRetry()
    }
}
```

Collect `auth.sessionState` to react to `Unauthenticated`, `Authenticating`, `OtpRequired`, `Authenticated`, `Refreshing`, `Expired`, `Locked`, and `Error`. Call `refreshSession()` before a protected operation when required and `logout()` to clear local key material before best-effort server revocation. `BiometricAuthenticator.authenticate(FragmentActivity)` wraps `BiometricPrompt`; it never receives biometric material.

Authentication needs a network connection. Offline biometric use should only unlock cached, non-sensitive UI and must not imply server authorization. If storage authentication fails, the SDK deletes the corrupt ciphertext. Use HTTPS outside the emulator, never log SDK arguments, and avoid retaining passwords in a ViewModel.

For testing, inject `DefaultSecureAuthClient` with a fake `AuthApi` and `TokenStorage`. Common failures map to the closed `AuthResult` hierarchy. Ensure the base URL ends with `/`; confirm network permission, TLS trust, and that emulator localhost is `10.0.2.2` when troubleshooting.
