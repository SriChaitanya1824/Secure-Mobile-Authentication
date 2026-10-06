package com.srichaitanya.issuer.service

import com.srichaitanya.issuer.crypto.CryptoService
import com.srichaitanya.issuer.dto.*
import com.srichaitanya.issuer.model.*
import com.srichaitanya.issuer.repository.*
import com.srichaitanya.issuer.security.JwtService
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val auditService: AuditService
) {
    fun register(request: RegisterRequest): AuthResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("User with email " + request.email + " already exists")
        }
        val user = User(
            id = "usr-" + UUID.randomUUID().toString().take(12),
            email = request.email,
            passwordHash = passwordEncoder.encode(request.password),
            fullName = request.fullName,
            role = request.role
        )
        val saved = userRepository.save(user)
        auditService.log("USER_REGISTERED", user.id, user.id, "Registered new user: " + user.email)
        return createAuthResponse(saved)
    }

    fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByEmail(request.email)
            .orElseThrow { IllegalArgumentException("Invalid email or password") }
        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            auditService.log("LOGIN_FAILURE", user.id, user.id, "Failed password attempt")
            throw IllegalArgumentException("Invalid email or password")
        }
        auditService.log("LOGIN_SUCCESS", user.id, user.id, "User logged in")
        return createAuthResponse(user)
    }

    fun refresh(request: RefreshTokenRequest): AuthResponse {
        val tokenHash = hashToken(request.refreshToken)
        val storedToken = refreshTokenRepository.findByTokenHashAndRevokedFalse(tokenHash)
            .orElseThrow { IllegalStateException("Invalid or revoked refresh token") }
        
        if (storedToken.expiresAt.isBefore(Instant.now())) {
            throw IllegalStateException("Refresh token expired")
        }
        
        storedToken.revoked = true
        refreshTokenRepository.save(storedToken)
        
        val user = userRepository.findById(storedToken.userId)
            .orElseThrow { IllegalStateException("User not found") }
        
        return createAuthResponse(user)
    }

    fun logout(userId: String) {
        refreshTokenRepository.deleteByUserId(userId)
        auditService.log("USER_LOGOUT", userId, userId, "User logged out and tokens invalidated")
    }

    private fun createAuthResponse(user: User): AuthResponse {
        val accessToken = jwtService.generateAccessToken(user.id, user.email, user.role.name)
        val rawRefreshToken = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString()
        val tokenHash = hashToken(rawRefreshToken)
        
        val refreshTokenEntity = RefreshToken(
            userId = user.id,
            tokenHash = tokenHash,
            expiresAt = Instant.now().plus(7, ChronoUnit.DAYS)
        )
        refreshTokenRepository.save(refreshTokenEntity)

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = rawRefreshToken,
            expiresIn = jwtService.expirationMs / 1000,
            user = UserDto(user.id, user.email, user.fullName, user.role)
        )
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(token.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(hash)
    }
}

@Service
class CertificateService(
    private val certificateRepository: CertificateRepository,
    private val revocationRepository: RevocationRepository,
    private val cryptoService: CryptoService,
    private val auditService: AuditService,
    @Value("\${issuer.id}") private val defaultIssuerId: String,
    @Value("\${issuer.name}") private val defaultIssuerName: String
) {
    @Transactional
    fun issueCertificate(request: IssueCertificateRequest): CertificateDto {
        val certId = request.certificateId ?: ("cert-" + UUID.randomUUID().toString().take(12))
        
        if (certificateRepository.existsById(certId)) {
            throw IllegalArgumentException("Certificate with ID " + certId + " already exists")
        }

        val now = Instant.now()
        val validFrom = request.validFrom ?: now
        val expiresAt = request.expiresAt ?: now.plus(365 * 4, ChronoUnit.DAYS)

        val canonicalMap = linkedMapOf<String, Any>(
            "certificateId" to certId,
            "credentialType" to request.credentialType,
            "issuerId" to defaultIssuerId,
            "issuerName" to defaultIssuerName,
            "subjectId" to request.subjectId,
            "subjectName" to request.subjectName,
            "issuedAt" to now.toString(),
            "validFrom" to validFrom.toString(),
            "expiresAt" to expiresAt.toString(),
            "claims" to request.claims
        )

        val canonicalJson = cryptoService.canonicalize(canonicalMap)
        val signature = cryptoService.signCanonicalPayload(canonicalJson)

        val certificate = Certificate(
            certificateId = certId,
            credentialType = request.credentialType,
            issuerId = defaultIssuerId,
            issuerName = defaultIssuerName,
            subjectId = request.subjectId,
            subjectName = request.subjectName,
            issuedAt = now,
            validFrom = validFrom,
            expiresAt = expiresAt,
            status = CertificateStatus.ACTIVE,
            credentialVersion = "1.0",
            signatureAlgorithm = "Ed25519",
            signature = signature,
            rawCanonicalPayload = canonicalJson
        )

        val claimsList = request.claims.map { (k, v) ->
            CertificateClaim(
                certificate = certificate,
                claimKey = k,
                claimValue = v,
                dataType = "STRING",
                isSensitive = request.sensitiveClaimKeys.contains(k)
            )
        }.toMutableList()

        certificate.claims = claimsList
        val saved = certificateRepository.save(certificate)

        auditService.log(
            "CERTIFICATE_CREATED",
            defaultIssuerId,
            certId,
            "Issued certificate " + certId + " to " + request.subjectName
        )

        return mapToDto(saved)
    }

    fun getCertificateById(certificateId: String): CertificateDto {
        val cert = certificateRepository.findById(certificateId)
            .orElseThrow { NoSuchElementException("Certificate not found with ID: " + certificateId) }
        
        if (cert.status == CertificateStatus.ACTIVE && cert.expiresAt.isBefore(Instant.now())) {
            cert.status = CertificateStatus.EXPIRED
            certificateRepository.save(cert)
        }

        return mapToDto(cert)
    }

    fun getWalletCertificates(subjectId: String): List<CertificateDto> {
        val certs = certificateRepository.findBySubjectId(subjectId)
        val now = Instant.now()
        certs.forEach { cert ->
            if (cert.status == CertificateStatus.ACTIVE && cert.expiresAt.isBefore(now)) {
                cert.status = CertificateStatus.EXPIRED
                certificateRepository.save(cert)
            }
        }
        return certs.map { mapToDto(it) }
    }

    fun getAllIssuerCertificates(): List<CertificateDto> {
        return certificateRepository.findAll().map { mapToDto(it) }
    }

    @Transactional
    fun revokeCertificate(certificateId: String, reason: String, actor: String): RevocationResponse {
        val cert = certificateRepository.findById(certificateId)
            .orElseThrow { NoSuchElementException("Certificate not found: " + certificateId) }

        if (cert.status == CertificateStatus.REVOKED) {
            val existing = revocationRepository.findByCertificateId(certificateId).orElse(null)
            return RevocationResponse(
                certificateId = cert.certificateId,
                status = CertificateStatus.REVOKED,
                reason = existing?.reason ?: reason,
                revokedBy = existing?.revokedBy ?: actor,
                revokedAt = existing?.revokedAt ?: cert.updatedAt
            )
        }

        cert.status = CertificateStatus.REVOKED
        cert.updatedAt = Instant.now()
        certificateRepository.save(cert)

        val revocation = Revocation(
            certificateId = certificateId,
            reason = reason,
            revokedBy = actor,
            revokedAt = cert.updatedAt
        )
        revocationRepository.save(revocation)

        auditService.log("CERTIFICATE_REVOKED", actor, certificateId, "Revoked certificate: " + reason)

        return RevocationResponse(
            certificateId = certificateId,
            status = CertificateStatus.REVOKED,
            reason = reason,
            revokedBy = actor,
            revokedAt = revocation.revokedAt
        )
    }

    fun syncCertificates(since: Instant?): SyncResponse {
        val updated = if (since != null) {
            certificateRepository.findByUpdatedAtAfter(since)
        } else {
            certificateRepository.findAll()
        }
        val revokedIds = updated.filter { it.status == CertificateStatus.REVOKED }.map { it.certificateId }
        return SyncResponse(
            lastSyncTimestamp = Instant.now(),
            certificates = updated.map { mapToDto(it) },
            revokedCertificateIds = revokedIds
        )
    }

    fun mapToDto(cert: Certificate): CertificateDto {
        val claimsMap = cert.claims.associate { it.claimKey to it.claimValue }
        val sensitiveKeys = cert.claims.filter { it.isSensitive }.map { it.claimKey }.toSet()

        return CertificateDto(
            certificateId = cert.certificateId,
            credentialType = cert.credentialType,
            issuerId = cert.issuerId,
            issuerName = cert.issuerName,
            subjectId = cert.subjectId,
            subjectName = cert.subjectName,
            issuedAt = cert.issuedAt,
            validFrom = cert.validFrom,
            expiresAt = cert.expiresAt,
            status = cert.status,
            credentialVersion = cert.credentialVersion,
            signatureAlgorithm = cert.signatureAlgorithm,
            signature = cert.signature,
            claims = claimsMap,
            sensitiveClaimKeys = sensitiveKeys,
            proof = ProofDto(
                created = cert.issuedAt,
                verificationMethod = cert.issuerId + "#key-1",
                signatureValue = cert.signature
            ),
            createdAt = cert.createdAt,
            updatedAt = cert.updatedAt
        )
    }
}

@Service
class AuditService(private val auditLogRepository: AuditLogRepository) {
    fun log(eventType: String, actor: String, targetId: String? = null, details: String? = null, ip: String? = null) {
        val entry = AuditLog(
            eventType = eventType,
            actor = actor,
            targetId = targetId,
            details = details,
            ipAddress = ip
        )
        auditLogRepository.save(entry)
    }
}
