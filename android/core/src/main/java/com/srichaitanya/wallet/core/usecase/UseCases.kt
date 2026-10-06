package com.srichaitanya.wallet.core.usecase

import com.srichaitanya.wallet.core.common.Resource
import com.srichaitanya.wallet.core.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface CertificateRepository {
    fun getCertificates(): Flow<List<Certificate>>
    suspend fun getCertificateById(id: String): Certificate?
    suspend fun saveCertificate(certificate: Certificate)
    suspend fun deleteCertificate(id: String)
    suspend fun updateCertificateStatus(id: String, status: CertificateStatus)
    suspend fun syncWithIssuer(token: String): Resource<List<Certificate>>
}

class GetCertificatesUseCase(
    private val repository: CertificateRepository
) {
    operator fun invoke(): Flow<List<Certificate>> = repository.getCertificates()
}

class GetCertificateDetailUseCase(
    private val repository: CertificateRepository
) {
    suspend operator fun invoke(id: String): Certificate? = repository.getCertificateById(id)
}

class GeneratePresentationUseCase {
    operator fun invoke(
        certificate: Certificate,
        selectedClaimKeys: Set<String>,
        nonce: String,
        holderSignature: String? = null
    ): PresentationPayload {
        val disclosed = certificate.claims
            .filter { it.key in selectedClaimKeys }
            .associate { it.key to it.value }

        return PresentationPayload(
            certificateId = certificate.id,
            credentialType = certificate.credentialType,
            issuerId = certificate.issuerId,
            issuerName = certificate.issuerName,
            holderDid = certificate.holderDid,
            subjectName = certificate.subjectName,
            issuedAt = certificate.issuedAt,
            expiresAt = certificate.expiresAt,
            disclosedClaims = disclosed,
            nonce = nonce,
            timestamp = System.currentTimeMillis(),
            originalSignature = certificate.signatureValue,
            holderSignature = holderSignature
        )
    }
}

class CalculateExpiryUseCase {
    fun isExpired(expiresAt: Long): Boolean = System.currentTimeMillis() > expiresAt

    fun getDaysUntilExpiry(expiresAt: Long): Long {
        val diff = expiresAt - System.currentTimeMillis()
        return if (diff <= 0) 0 else diff / (1000 * 60 * 60 * 24)
    }

    fun isExpiringSoon(expiresAt: Long, thresholdDays: Int = 30): Boolean {
        val days = getDaysUntilExpiry(expiresAt)
        return days in 1..thresholdDays
    }
}

class AssessDeviceSecurityUseCase {
    operator fun invoke(
        isHardwareBacked: Boolean,
        isBiometricsAvailable: Boolean,
        isScreenLockSet: Boolean,
        isRooted: Boolean
    ): DeviceSecurityScore {
        var score = 100
        if (!isHardwareBacked) score -= 25
        if (!isBiometricsAvailable) score -= 25
        if (!isScreenLockSet) score -= 30
        if (isRooted) score -= 50

        val clampedScore = score.coerceIn(0, 100)
        val level = when {
            clampedScore >= 80 -> "HARDWARE_STRONG_BOX"
            clampedScore >= 50 -> "HARDWARE_TEE"
            else -> "SOFTWARE_FALLBACK"
        }

        return DeviceSecurityScore(
            score = clampedScore,
            isKeystoreHardwareBacked = isHardwareBacked,
            isBiometricsEnrolled = isBiometricsAvailable,
            isScreenLockEnabled = isScreenLockSet,
            isDeviceRooted = isRooted,
            isEncryptionEnabled = true,
            securityLevel = level
        )
    }
}
