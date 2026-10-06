package com.srichaitanya.issuer.repository

import com.srichaitanya.issuer.model.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, String> {
    fun findByEmail(email: String): Optional<User>
    fun existsByEmail(email: String): Boolean
}

@Repository
interface IssuerRepository : JpaRepository<Issuer, String> {
    fun findByIssuerIdUri(issuerIdUri: String): Optional<Issuer>
}

@Repository
interface CertificateRepository : JpaRepository<Certificate, String> {
    fun findBySubjectId(subjectId: String): List<Certificate>
    fun findByIssuerId(issuerId: String): List<Certificate>
    fun findByUpdatedAtAfter(since: Instant): List<Certificate>
    fun findByStatus(status: CertificateStatus): List<Certificate>
}

@Repository
interface RevocationRepository : JpaRepository<Revocation, Long> {
    fun findByCertificateId(certificateId: String): Optional<Revocation>
    fun existsByCertificateId(certificateId: String): Boolean
}

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {
    fun findByTokenHashAndRevokedFalse(tokenHash: String): Optional<RefreshToken>
    fun deleteByUserId(userId: String)
}

@Repository
interface AuditLogRepository : JpaRepository<AuditLog, Long> {
    fun findTop100ByOrderByCreatedAtDesc(): List<AuditLog>
}
