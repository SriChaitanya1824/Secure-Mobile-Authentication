package com.srichaitanya.issuer.model

import jakarta.persistence.*
import java.time.Instant

enum class UserRole {
    ISSUER, HOLDER, VERIFIER, ADMIN
}

enum class CertificateStatus {
    ACTIVE, EXPIRED, REVOKED, SUSPENDED
}

enum class IssuerStatus {
    TRUSTED, SUSPENDED, REVOKED
}

@Entity
@Table(name = "users")
data class User(
    @Id
    val id: String,
    
    @Column(unique = true, nullable = false)
    val email: String,
    
    @Column(name = "password_hash", nullable = false)
    var passwordHash: String,
    
    @Column(name = "full_name", nullable = false)
    val fullName: String,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: UserRole,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
    
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
)

@Entity
@Table(name = "issuers")
data class Issuer(
    @Id
    val id: String,
    
    @Column(name = "issuer_id_uri", unique = true, nullable = false)
    val issuerIdUri: String,
    
    @Column(nullable = false)
    val name: String,
    
    @Column(name = "public_key", nullable = false, columnDefinition = "TEXT")
    val publicKey: String,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: IssuerStatus = IssuerStatus.TRUSTED,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
    
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
)

@Entity
@Table(name = "certificates")
data class Certificate(
    @Id
    @Column(name = "certificate_id")
    val certificateId: String,
    
    @Column(name = "credential_type", nullable = false)
    val credentialType: String,
    
    @Column(name = "issuer_id", nullable = false)
    val issuerId: String,
    
    @Column(name = "issuer_name", nullable = false)
    val issuerName: String,
    
    @Column(name = "subject_id", nullable = false)
    val subjectId: String,
    
    @Column(name = "subject_name", nullable = false)
    val subjectName: String,
    
    @Column(name = "issued_at", nullable = false)
    val issuedAt: Instant,
    
    @Column(name = "valid_from", nullable = false)
    val validFrom: Instant,
    
    @Column(name = "expires_at", nullable = false)
    val expiresAt: Instant,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: CertificateStatus = CertificateStatus.ACTIVE,
    
    @Column(name = "credential_version", nullable = false)
    val credentialVersion: String = "1.0",
    
    @Column(name = "signature_algorithm", nullable = false)
    val signatureAlgorithm: String = "Ed25519",
    
    @Column(nullable = false, columnDefinition = "TEXT")
    val signature: String,
    
    @Column(name = "raw_canonical_payload", nullable = false, columnDefinition = "TEXT")
    val rawCanonicalPayload: String,
    
    @OneToMany(mappedBy = "certificate", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    var claims: MutableList<CertificateClaim> = mutableListOf(),
    
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
    
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
)

@Entity
@Table(name = "certificate_claims")
data class CertificateClaim(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "certificate_id")
    var certificate: Certificate? = null,
    
    @Column(name = "claim_key", nullable = false)
    val claimKey: String,
    
    @Column(name = "claim_value", nullable = false, columnDefinition = "TEXT")
    val claimValue: String,
    
    @Column(name = "data_type", nullable = false)
    val dataType: String = "STRING",
    
    @Column(name = "is_sensitive")
    val isSensitive: Boolean = false
)

@Entity
@Table(name = "revocations")
data class Revocation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(name = "certificate_id", unique = true, nullable = false)
    val certificateId: String,
    
    @Column(nullable = false)
    val reason: String,
    
    @Column(name = "revoked_by", nullable = false)
    val revokedBy: String,
    
    @Column(name = "revoked_at", nullable = false)
    val revokedAt: Instant = Instant.now()
)

@Entity
@Table(name = "refresh_tokens")
data class RefreshToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(name = "user_id", nullable = false)
    val userId: String,
    
    @Column(name = "token_hash", nullable = false)
    val tokenHash: String,
    
    @Column(name = "expires_at", nullable = false)
    val expiresAt: Instant,
    
    @Column(nullable = false)
    var revoked: Boolean = false,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
)

@Entity
@Table(name = "audit_logs")
data class AuditLog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(name = "event_type", nullable = false)
    val eventType: String,
    
    @Column(nullable = false)
    val actor: String,
    
    @Column(name = "target_id")
    val targetId: String? = null,
    
    @Column(columnDefinition = "TEXT")
    val details: String? = null,
    
    @Column(name = "ip_address")
    val ipAddress: String? = null,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
)
