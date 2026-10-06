package com.srichaitanya.wallet.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val passwordHash: String,
    val deviceFingerprint: String? = null
)

@Serializable
data class RegisterRequest(
    val email: String,
    val passwordHash: String,
    val fullName: String,
    val publicKey: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val refreshToken: String,
    val userId: String,
    val email: String,
    val holderDid: String
)

@Serializable
data class NetworkClaim(
    val key: String,
    val label: String,
    val value: String,
    val isSensitive: Boolean
)

@Serializable
data class NetworkCertificate(
    val id: String,
    val credentialType: String,
    val issuerId: String,
    val issuerName: String,
    val holderDid: String,
    val subjectName: String,
    val subjectIdentifier: String,
    val issuedAt: String,
    val expiresAt: String,
    val status: String,
    val signatureAlgorithm: String,
    val signatureValue: String,
    val canonicalHash: String,
    val claims: List<NetworkClaim>
)

@Serializable
data class VerifyPresentationRequest(
    val presentationPayload: String,
    val nonce: String,
    val verifierId: String = "MOBILE_VERIFIER_01"
)

@Serializable
data class NetworkVerificationResponse(
    val isValid: Boolean,
    val status: String,
    val certificateId: String?,
    val failureReason: String?,
    val disclosedClaims: Map<String, String>? = null,
    val verifiedAt: String
)

@Serializable
data class NetworkIssuer(
    val id: String,
    val name: String,
    val jurisdiction: String,
    val publicKey: String,
    val isTrusted: Boolean
)
