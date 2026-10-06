package com.srichaitanya.wallet.core

import com.srichaitanya.wallet.core.model.*
import com.srichaitanya.wallet.core.usecase.CalculateExpiryUseCase
import com.srichaitanya.wallet.core.usecase.GeneratePresentationUseCase
import com.srichaitanya.wallet.core.usecase.AssessDeviceSecurityUseCase
import org.junit.Assert.*
import org.junit.Test

class CoreLogicTests {

    private val expiryUseCase = CalculateExpiryUseCase()
    private val presentationUseCase = GeneratePresentationUseCase()
    private val securityUseCase = AssessDeviceSecurityUseCase()

    @Test
    fun testCertificateExpiryCalculation() {
        val pastTime = System.currentTimeMillis() - 100000
        val futureTime = System.currentTimeMillis() + (10 * 24 * 60 * 60 * 1000L) // 10 days
        val farFutureTime = System.currentTimeMillis() + (60 * 24 * 60 * 60 * 1000L) // 60 days

        assertTrue(expiryUseCase.isExpired(pastTime))
        assertFalse(expiryUseCase.isExpired(futureTime))
        assertTrue(expiryUseCase.isExpiringSoon(futureTime, 30))
        assertFalse(expiryUseCase.isExpiringSoon(farFutureTime, 30))
    }

    @Test
    fun testSelectiveDisclosurePresentation() {
        val sampleCert = Certificate(
            id = "CERT-101",
            credentialType = CredentialType.NATIONAL_ID,
            issuerId = "VIDA-ID-01",
            issuerName = "VIDA Certificate Authority",
            holderDid = "did:vida:holder123",
            subjectName = "Jane Doe",
            subjectIdentifier = "ID-987654321",
            issuedAt = System.currentTimeMillis() - 86400000,
            expiresAt = System.currentTimeMillis() + 864000000,
            status = CertificateStatus.ACTIVE,
            signatureAlgorithm = "Ed25519",
            signatureValue = "sig123abc",
            canonicalHash = "hash123",
            claims = listOf(
                Claim(key = "fullName", label = "Full Name", value = "Jane Doe"),
                Claim(key = "birthDate", label = "Date of Birth", value = "1990-01-01", isSensitive = true),
                Claim(key = "nationality", label = "Nationality", value = "ID")
            )
        )

        // Select only fullName and nationality, omit birthDate
        val selectedKeys = setOf("fullName", "nationality")
        val presentation = presentationUseCase(sampleCert, selectedKeys, "test-nonce-999")

        assertEquals("CERT-101", presentation.certificateId)
        assertEquals("test-nonce-999", presentation.nonce)
        assertEquals(2, presentation.disclosedClaims.size)
        assertEquals("Jane Doe", presentation.disclosedClaims["fullName"])
        assertEquals("ID", presentation.disclosedClaims["nationality"])
        assertNull(presentation.disclosedClaims["birthDate"])
    }

    @Test
    fun testDeviceSecurityScoreCalculation() {
        val secureReport = securityUseCase(
            isHardwareBacked = true,
            isBiometricsAvailable = true,
            isScreenLockSet = true,
            isRooted = false
        )
        assertEquals(100, secureReport.score)
        assertEquals("HARDWARE_STRONG_BOX", secureReport.securityLevel)

        val compromisedReport = securityUseCase(
            isHardwareBacked = false,
            isBiometricsAvailable = false,
            isScreenLockSet = false,
            isRooted = true
        )
        assertTrue(compromisedReport.score <= 20)
        assertEquals("SOFTWARE_FALLBACK", compromisedReport.securityLevel)
    }
}
