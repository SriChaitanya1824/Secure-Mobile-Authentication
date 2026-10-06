package com.srichaitanya.wallet.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class CertificateStatus {
    ACTIVE,
    REVOKED,
    EXPIRED,
    SUSPENDED
}

@Serializable
enum class CredentialType {
    NATIONAL_ID,
    DRIVING_LICENSE,
    DIGITAL_PASSPORT,
    PROFESSIONAL_LICENSE,
    EMPLOYMENT_CREDENTIAL,
    HEALTH_CERTIFICATE
}

@Serializable
data class Claim(
    val key: String,
    val label: String,
    val value: String,
    val isSensitive: Boolean = false,
    val isDisclosed: Boolean = true
)

@Serializable
data class Certificate(
    val id: String,
    val credentialType: CredentialType,
    val issuerId: String,
    val issuerName: String,
    val holderDid: String,
    val subjectName: String,
    val subjectIdentifier: String,
    val issuedAt: Long,
    val expiresAt: Long,
    val status: CertificateStatus,
    val signatureAlgorithm: String,
    val signatureValue: String,
    val canonicalHash: String,
    val claims: List<Claim> = emptyList(),
    val isStoredInHardwareKeystore: Boolean = true
)

@Serializable
data class IssuerInfo(
    val id: String,
    val name: String,
    val jurisdiction: String,
    val publicKeyBase64: String,
    val isTrusted: Boolean = true,
    val revocationEndpoint: String
)

@Serializable
data class PresentationPayload(
    val certificateId: String,
    val credentialType: CredentialType,
    val issuerId: String,
    val issuerName: String,
    val holderDid: String,
    val subjectName: String,
    val issuedAt: Long,
    val expiresAt: Long,
    val disclosedClaims: Map<String, String>,
    val nonce: String,
    val timestamp: Long,
    val originalSignature: String,
    val holderSignature: String? = null
)

@Serializable
data class VerificationCheck(
    val checkName: String,
    val passed: Boolean,
    val detail: String
)

@Serializable
data class VerificationResult(
    val isValid: Boolean,
    val certificateId: String?,
    val credentialType: CredentialType?,
    val issuerName: String?,
    val subjectName: String?,
    val failureReason: String? = null,
    val checks: List<VerificationCheck> = emptyList(),
    val verifiedAt: Long = System.currentTimeMillis()
)

@Serializable
data class DeviceSecurityScore(
    val score: Int, // 0 to 100
    val isKeystoreHardwareBacked: Boolean,
    val isBiometricsEnrolled: Boolean,
    val isScreenLockEnabled: Boolean,
    val isDeviceRooted: Boolean,
    val isEncryptionEnabled: Boolean,
    val securityLevel: String // "HARDWARE_TEE", "STRONG_BOX", "SOFTWARE"
)

@Serializable
data class AuditRecord(
    val id: String,
    val timestamp: Long,
    val eventType: String,
    val details: String,
    val status: String
)
