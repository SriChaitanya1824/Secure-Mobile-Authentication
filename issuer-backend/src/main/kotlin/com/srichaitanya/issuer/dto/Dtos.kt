package com.srichaitanya.issuer.dto

import com.srichaitanya.issuer.model.CertificateStatus
import com.srichaitanya.issuer.model.IssuerStatus
import com.srichaitanya.issuer.model.UserRole
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import java.time.Instant

data class RegisterRequest(
    @field:NotBlank val fullName: String,
    @field:Email @field:NotBlank val email: String,
    @field:NotBlank val password: String,
    val role: UserRole = UserRole.HOLDER
)

data class LoginRequest(
    @field:Email @field:NotBlank val email: String,
    @field:NotBlank val password: String
)

data class RefreshTokenRequest(
    @field:NotBlank val refreshToken: String
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
    val user: UserDto
)

data class UserDto(
    val id: String,
    val email: String,
    val fullName: String,
    val role: UserRole
)

data class IssueCertificateRequest(
    val certificateId: String? = null,
    @field:NotBlank val credentialType: String,
    @field:NotBlank val subjectId: String,
    @field:NotBlank val subjectName: String,
    val validFrom: Instant? = null,
    val expiresAt: Instant? = null,
    val claims: Map<String, String> = emptyMap(),
    val sensitiveClaimKeys: Set<String> = emptySet()
)

data class CertificateDto(
    val certificateId: String,
    val credentialType: String,
    val issuerId: String,
    val issuerName: String,
    val subjectId: String,
    val subjectName: String,
    val issuedAt: Instant,
    val validFrom: Instant,
    val expiresAt: Instant,
    val status: CertificateStatus,
    val credentialVersion: String,
    val signatureAlgorithm: String,
    val signature: String,
    val claims: Map<String, String>,
    val sensitiveClaimKeys: Set<String>,
    val proof: ProofDto,
    val createdAt: Instant,
    val updatedAt: Instant
)

data class ProofDto(
    val type: String = "Ed25519Signature2020",
    val created: Instant,
    val verificationMethod: String,
    val proofPurpose: String = "assertionMethod",
    val signatureValue: String
)

data class RevokeCertificateRequest(
    @field:NotBlank val reason: String,
    val revokedBy: String? = null
)

data class RevocationResponse(
    val certificateId: String,
    val status: CertificateStatus,
    val reason: String,
    val revokedBy: String,
    val revokedAt: Instant
)

data class SyncResponse(
    val lastSyncTimestamp: Instant,
    val certificates: List<CertificateDto>,
    val revokedCertificateIds: List<String>
)

data class IssuerDto(
    val id: String,
    val issuerIdUri: String,
    val name: String,
    val publicKey: String,
    val status: IssuerStatus
)

data class ApiErrorResponse(
    val code: String,
    val message: String,
    val requestId: String,
    val timestamp: Instant = Instant.now(),
    val errors: List<String> = emptyList()
)
