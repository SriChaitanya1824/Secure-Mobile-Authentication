package com.srichaitanya.verifier

import com.fasterxml.jackson.databind.ObjectMapper
import com.srichaitanya.verifier.dto.SignedQrPayload
import com.srichaitanya.verifier.dto.VerifyPresentationRequest
import com.srichaitanya.verifier.model.VerificationStatus
import com.srichaitanya.verifier.service.VerificationEngineService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.Instant
import java.util.*

@SpringBootTest
@ActiveProfiles("test")
class VerifierBackendTests {

    @Autowired
    private lateinit var verificationEngineService: VerificationEngineService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `test verify valid QR presentation`() {
        val payload = SignedQrPayload(
            credentialId = "cert-vit-btech-2026-001",
            credentialType = "University Degree",
            issuerId = "did:vid:issuer:vit-university",
            issuerName = "VIT Academic Credentials",
            subjectId = "usr-student-001",
            subjectName = "John Doe",
            issuedAt = "2026-01-15T09:00:00Z",
            expiresAt = "2036-01-15T09:00:00Z",
            disclosedClaims = mapOf(
                "degree" to "Bachelor of Technology in Computer Science",
                "gpa" to "3.92"
            ),
            nonce = "nonce-" + UUID.randomUUID().toString(),
            presentationTimestamp = Instant.now().toString(),
            signature = "valid_ed25519_sig_test"
        )

        val req = VerifyPresentationRequest(
            payloadJson = objectMapper.writeValueAsString(payload),
            verifierName = "Demo Tech Employer"
        )

        val result = verificationEngineService.verifyPresentation(req)
        assertEquals(VerificationStatus.VERIFIED, result.status)
        assertEquals("Bachelor of Technology in Computer Science", result.verifiedClaims["degree"])
        assertTrue(result.checksPassed["NOT_EXPIRED"] == true)
        assertTrue(result.checksPassed["ISSUER_TRUSTED"] == true)
    }

    @Test
    fun `test expired certificate verification failure`() {
        val payload = SignedQrPayload(
            credentialId = "cert-vit-training-2023-002",
            credentialType = "Training Certificate",
            issuerId = "did:vid:issuer:vit-university",
            issuerName = "VIT Academic Credentials",
            subjectId = "usr-student-001",
            subjectName = "John Doe",
            issuedAt = "2023-01-01T00:00:00Z",
            expiresAt = "2024-01-01T00:00:00Z",
            disclosedClaims = mapOf("course" to "Cloud Architecture"),
            nonce = "nonce-" + UUID.randomUUID().toString(),
            presentationTimestamp = Instant.now().toString(),
            signature = "sig"
        )

        val req = VerifyPresentationRequest(payloadJson = objectMapper.writeValueAsString(payload))
        val result = verificationEngineService.verifyPresentation(req)
        assertEquals(VerificationStatus.EXPIRED, result.status)
    }

    @Test
    fun `test replay attack detection on reused nonce`() {
        val nonce = "fixed-replay-nonce-" + UUID.randomUUID().toString()
        val payload = SignedQrPayload(
            credentialId = "cert-vit-btech-2026-001",
            credentialType = "University Degree",
            issuerId = "did:vid:issuer:vit-university",
            issuerName = "VIT Academic Credentials",
            subjectId = "usr-student-001",
            subjectName = "John Doe",
            issuedAt = "2026-01-15T09:00:00Z",
            expiresAt = "2036-01-15T09:00:00Z",
            disclosedClaims = mapOf("degree" to "B.Tech"),
            nonce = nonce,
            presentationTimestamp = Instant.now().toString(),
            signature = "sig"
        )

        val req = VerifyPresentationRequest(payloadJson = objectMapper.writeValueAsString(payload))
        val res1 = verificationEngineService.verifyPresentation(req)
        assertEquals(VerificationStatus.VERIFIED, res1.status)

        val res2 = verificationEngineService.verifyPresentation(req)
        assertEquals(VerificationStatus.REPLAY_DETECTED, res2.status)
    }
}
