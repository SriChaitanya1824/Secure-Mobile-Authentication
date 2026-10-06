package com.srichaitanya.verifier.model

import jakarta.persistence.*
import java.time.Instant

enum class VerificationStatus {
    VERIFIED,
    EXPIRED,
    REVOKED,
    SUSPENDED,
    INVALID_SIGNATURE,
    UNKNOWN_ISSUER,
    MALFORMED_CREDENTIAL,
    REPLAY_DETECTED,
    NETWORK_ERROR
}

@Entity
@Table(name = "verification_records")
data class VerificationRecord(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(name = "event_id", unique = true, nullable = false)
    val eventId: String,
    
    @Column(name = "certificate_id")
    val certificateId: String?,
    
    @Column(name = "credential_type")
    val credentialType: String?,
    
    @Column(name = "issuer_id")
    val issuerId: String?,
    
    @Column(name = "subject_name")
    val subjectName: String?,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    val status: VerificationStatus,
    
    @Column(name = "failure_reason")
    val failureReason: String? = null,
    
    @Column(name = "nonce")
    val nonce: String?,
    
    @Column(name = "disclosed_claims", columnDefinition = "TEXT")
    val disclosedClaims: String?,
    
    @Column(name = "verifier_name")
    val verifierName: String = "Demo Verifier System",
    
    @Column(name = "verified_at", nullable = false)
    val verifiedAt: Instant = Instant.now()
)

@Entity
@Table(name = "trusted_issuers_cache")
data class TrustedIssuer(
    @Id
    val issuerId: String,
    
    @Column(nullable = false)
    val name: String,
    
    @Column(name = "public_key", nullable = false, columnDefinition = "TEXT")
    val publicKey: String,
    
    @Column(name = "is_trusted", nullable = false)
    val isTrusted: Boolean = true
)
