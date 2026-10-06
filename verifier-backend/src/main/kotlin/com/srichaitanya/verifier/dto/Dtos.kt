package com.srichaitanya.verifier.dto

import com.srichaitanya.verifier.model.VerificationStatus
import jakarta.validation.constraints.NotBlank
import java.time.Instant

data class VerifyPresentationRequest(
    @field:NotBlank val payloadJson: String,
    val verifierName: String = "Demo Enterprise Verifier"
)

data class SignedQrPayload(
    val credentialId: String,
    val credentialType: String,
    val issuerId: String,
    val issuerName: String,
    val subjectId: String,
    val subjectName: String,
    val issuedAt: String,
    val expiresAt: String,
    val disclosedClaims: Map<String, String>,
    val nonce: String,
    val presentationTimestamp: String,
    val signature: String
)

data class VerificationResultDto(
    val eventId: String,
    val status: VerificationStatus,
    val certificateId: String?,
    val credentialType: String?,
    val issuerId: String?,
    val issuerName: String?,
    val subjectName: String?,
    val verifiedClaims: Map<String, String>,
    val checksPassed: Map<String, Boolean>,
    val failureReason: String?,
    val verifiedAt: Instant = Instant.now()
)

data class VerificationHistoryDto(
    val eventId: String,
    val certificateId: String?,
    val credentialType: String?,
    val subjectName: String?,
    val status: VerificationStatus,
    val verifiedAt: Instant,
    val verifierName: String
)
